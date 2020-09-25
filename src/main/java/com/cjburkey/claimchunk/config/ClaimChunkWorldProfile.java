package com.cjburkey.claimchunk.config;

import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.config.ccconfig.CCConfig;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

/**
 * A class that represents the permissions that players have in a given world.
 *
 * @since 0.0.23
 */
public class ClaimChunkWorldProfile {
    
    private static final String KEY = "^ (claimedChunks|unclaimedChunks) \\. (entityAccesses|blockAccesses) \\. ([a-zA-Z0-9\\-_]+) $";
    private static final Pattern KEY_PAT = Pattern.compile(KEY, Pattern.COMMENTS);

    public boolean enabled;

    public final Access claimedChunks;
    public final Access unclaimedChunks;

    public ClaimChunkWorldProfile(boolean enabled, @Nullable Access claimedChunks, @Nullable Access unclaimedChunks) {
        this.enabled = enabled;

        // Make sure the access storage isn't null
        if (Objects.isNull(claimedChunks)) {
            claimedChunks = new Access(new HashMap<>(), new HashMap<>());
        }
        if (Objects.isNull(unclaimedChunks)) {
            unclaimedChunks = new Access(new HashMap<>(), new HashMap<>());
        }

        this.claimedChunks = claimedChunks;
        this.unclaimedChunks = unclaimedChunks;
    }

    // Returns `true` if the player should be allowed to perform this action
    public boolean canAccessEntity(boolean isOwned,
                                   boolean isOwnerOrAccess,
                                   @Nonnull Entity entity,
                                   @Nonnull EntityAccessType accessType) {
        // If the chunk is claimed and the player has access, they can just
        // edit and interact with it as if it were their own. Then check
        // for the entity access and determine if the player is allowed to
        // access it.
        return isOwnerOrAccess || checkEntityAccess(isOwned,
                                                    entity.getWorld().getName(),
                                                    entity.getType(),
                                                    accessType);
    }

    // Returns `true` if the player should be allowed to perform this action
    private boolean checkEntityAccess(boolean isClaimed,
                                      String worldName,
                                      @Nonnull EntityType entityType,
                                      @Nonnull EntityAccessType accessType) {
        // Check for the type of access
        return accessType.getShouldAllow.apply(getEntityAccess(isClaimed, worldName, entityType));
    }

    public EntityAccess getEntityAccess(boolean isClaimed, String worldName, EntityType entityType) {
        // Get all of the entity access mappings
        HashMap<EntityType, EntityAccess> entityAccesses = (isClaimed ? claimedChunks : unclaimedChunks).entityAccesses;
        
        // Get the access for this entity, if one is present
        EntityAccess access = entityAccesses.get(entityType);
        
        // If one is not present, get the default
        if (access == null) access = entityAccesses.get(EntityType.UNKNOWN);
        
        // If there is no default, then there should be a console error and assume a value of allow
        if (access == null) {
            Utils.err("Entity \"%s\" doesn't have a specific protection profile for world \"%s\" for %s chunks!",
                    entityType,
                    worldName,
                    isClaimed ? "claimed" : "unclaimed");
            access = new EntityAccess(true, true, true);
        }

        return access;
    }

    // Returns `true` if the player should be allowed to perform this action
    public boolean canAccessBlock(boolean isOwned,
                                  boolean isOwnerOrAccess,
                                  String worldName,
                                  @Nonnull Material blockType,
                                  @Nonnull BlockAccessType accessType) {
        // If the chunk is claimed and the player has access, they can just
        // edit and interact with it as if it were their own. Then check
        // for the entity access and determine if the player is allowed to
        // access it.
        return isOwnerOrAccess || checkBlockAccess(isOwned, worldName, blockType, accessType);
    }

    // Returns `true` if the player should be allowed to perform this action
    private boolean checkBlockAccess(boolean isClaimed,
                                     String worldName,
                                     @Nonnull Material blockType,
                                     @Nonnull BlockAccessType accessType) {
        // Check for the type of access
        return accessType.shouldAllow.apply(getBlockAccess(isClaimed, worldName, blockType));
    }

    private BlockAccess getBlockAccess(boolean isClaimed, String worldName, Material blockType) {
        // Get all of the entity access mappings
        HashMap<Material, BlockAccess> blockAccesses = (isClaimed ? claimedChunks : unclaimedChunks).blockAccesses;
        
        // Get the access for this entity, if one is present
        BlockAccess access = blockAccesses.get(blockType);
        
        // If one is not present, get the default
        if (access == null) access = blockAccesses.get(Material.AIR);
        
        // If there is no default, then there should be a console error and assume a value of allow
        if (access == null) {
            Utils.err("Block \"%s\" doesn't have a specific protection profile for world \"%s\" for %s chunks!",
                    blockType,
                    worldName,
                    isClaimed ? "claimed" : "unclaimed");
            access = new BlockAccess(true, true, true, true);
        }
        
        return access;
    }
    
    public CCConfig toCCConfig() {
        final CCConfig config = new CCConfig("");
        
        // Write all the data to a config
        config.set("enabled", enabled);
        
        // Write entity accesses
        for (HashMap.Entry<EntityType, EntityAccess> entry : claimedChunks.entityAccesses.entrySet()) {
            config.set("claimedChunks.entityAccesses." + entry.getKey(),
                    String.format("%s%s%s",
                            entry.getValue().allowDamage ? "#" : "-",
                            entry.getValue().allowExplosion ? "#" : "-",
                            entry.getValue().allowInteract ? "#" : "-"));
        }
        for (HashMap.Entry<EntityType, EntityAccess> entry : unclaimedChunks.entityAccesses.entrySet()) {
            config.set("unclaimedChunks.entityAccesses." + entry.getKey(),
                    String.format("%s%s%s",
                            entry.getValue().allowDamage ? "#" : "-",
                            entry.getValue().allowExplosion ? "#" : "-",
                            entry.getValue().allowInteract ? "#" : "-"));
        }
        
        // Write block accesses
        for (HashMap.Entry<Material, BlockAccess> entry : claimedChunks.blockAccesses.entrySet()) {
            config.set("claimedChunks.blockAccesses." + entry.getKey(),
                    String.format("%s%s%s%s",
                            entry.getValue().allowBreak ? "#" : "-",
                            entry.getValue().allowExplosion ? "#" : "-",
                            entry.getValue().allowInteract ? "#" : "-",
                            entry.getValue().allowPlace ? "#" : "-"));
        }
        for (HashMap.Entry<Material, BlockAccess> entry : unclaimedChunks.blockAccesses.entrySet()) {
            config.set("unclaimedChunks.blockAccesses." + entry.getKey(),
                    String.format("%s%s%s%s",
                            entry.getValue().allowBreak ? "#" : "-",
                            entry.getValue().allowExplosion ? "#" : "-",
                            entry.getValue().allowInteract ? "#" : "-",
                            entry.getValue().allowPlace ? "#" : "-"));
        }
        
        return config;
    }
    
    public void fromCCConfig(@Nonnull CCConfig config) {
        for (HashMap.Entry<String, String> keyValue : config.values()) {
            // If it's the equals key, 
            if (keyValue.getKey().equals("enabled")) {
                enabled = config.getBool("enabled", enabled);
                continue;
            }
            
            // Try to match against the pattern for a key
            final Matcher matcher = KEY_PAT.matcher(keyValue.getKey());
            if (matcher.matches() && matcher.groupCount() >= 3) {
                // Get the access depending on claimed/unclaimed chunks
                Access access = matcher.group(1).equals("claimedChunks") ? claimedChunks : unclaimedChunks;
                
                // Check if to look in entity accesses or block accesses
                if (matcher.group(2).equals("entityAccesses")) {
                    // Get the info required to update the value in the config
                    String entityType = matcher.group(3);
                    char[] val = (keyValue.getValue() == null)
                            ? new char[0]
                            : keyValue.getValue().toCharArray();
                    
                    // Make sure that there are three control character
                    if (val.length != 3) {
                        Utils.err("Invalid value while parsing entity access: \"%s\"", Arrays.toString(val));
                        continue;
                    }
                    
                    // Load the provided values
                    try {
                        access.entityAccesses.get(EntityType.valueOf(entityType)).allowDamage = val[0] == '#';
                        access.entityAccesses.get(EntityType.valueOf(entityType)).allowExplosion = val[1] == '#';
                        access.entityAccesses.get(EntityType.valueOf(entityType)).allowInteract = val[2] == '#';
                    } catch (Exception ignored) {}
                } else if (matcher.group(2).equals("blockAccesses")) {
                    // Get the info required to update the value in the config
                    String blockType = matcher.group(3);
                    char[] val = (keyValue.getValue() == null)
                            ? new char[0]
                            : keyValue.getValue().toCharArray();
                    
                    // Make sure that there are three control character
                    if (val.length != 4) {
                        Utils.err("Invalid value while parsing block access: \"%s\"", Arrays.toString(val));
                        continue;
                    }
                    
                    // Load the provided values
                    try {
                        access.blockAccesses.get(Material.valueOf(blockType)).allowBreak = val[0] == '#';
                        access.blockAccesses.get(Material.valueOf(blockType)).allowExplosion = val[1] == '#';
                        access.blockAccesses.get(Material.valueOf(blockType)).allowInteract = val[2] == '#';
                        access.blockAccesses.get(Material.valueOf(blockType)).allowPlace = val[3] == '#';
                    } catch (Exception ignored) {}
                }
            }
            
            // Something went wrong
        }
    }

    public static class Access {

        public final HashMap<EntityType, EntityAccess> entityAccesses;
        public final HashMap<Material, BlockAccess> blockAccesses;

        protected Access(HashMap<EntityType, EntityAccess> entityAccesses, HashMap<Material, BlockAccess> blockAccesses) {
            this.entityAccesses = entityAccesses;
            this.blockAccesses = blockAccesses;
        }

    }

    @SuppressWarnings("unused")
    public enum EntityAccessType {

        INTERACT(access -> access.allowInteract),
        DAMAGE(access -> access.allowDamage),
        EXPLODE(access -> access.allowExplosion),

        ;

        private final Function<EntityAccess, Boolean> getShouldAllow;

        EntityAccessType(Function<EntityAccess, Boolean> getShouldAllow) {
            this.getShouldAllow = getShouldAllow;
        }

    }

    public static class EntityAccess {

        public boolean allowInteract;
        public boolean allowDamage;
        public boolean allowExplosion;

        public EntityAccess(boolean allowInteract, boolean allowDamage, boolean allowExplosion) {
            this.allowInteract = allowInteract;
            this.allowDamage = allowDamage;
            this.allowExplosion = allowExplosion;
        }

    }

    @SuppressWarnings("unused")
    public enum BlockAccessType {

        INTERACT(access -> access.allowInteract),
        BREAK(access -> access.allowBreak),
        PLACE(access -> access.allowPlace),
        EXPLODE(access -> access.allowExplosion),

        ;

        private final Function<BlockAccess, Boolean> shouldAllow;

        BlockAccessType(Function<BlockAccess, Boolean> shouldAllow) {
            this.shouldAllow = shouldAllow;
        }

    }

    public static class BlockAccess implements Cloneable {

        public boolean allowInteract;
        public boolean allowBreak;
        public boolean allowPlace;
        public boolean allowExplosion;

        public BlockAccess(boolean allowInteract, boolean allowBreak, boolean allowPlace, boolean  allowExplosion) {
            this.allowInteract = allowInteract;
            this.allowBreak = allowBreak;
            this.allowPlace = allowPlace;
            this.allowExplosion = allowExplosion;
        }

        @Override
        public BlockAccess clone() {
            try {
                return (BlockAccess) super.clone();
            } catch (CloneNotSupportedException ignored) {}
            
            return new BlockAccess(false, false, false, false);
        }

    }

}
