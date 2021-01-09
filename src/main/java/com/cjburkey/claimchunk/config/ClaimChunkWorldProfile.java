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

    private static final char YES = '#';
    private static final char NO = '.';
    private static final String DEFAULT = "__DEFAULT__";

    private static final String KEY = "^ (claimedChunks | unclaimedChunks) \\. (entityAccesses | blockAccesses) \\. ([a-zA-Z0-9\\-_]+) $";
    private static final Pattern KEY_PAT = Pattern.compile(KEY, Pattern.COMMENTS);
    private static final String headerComment = "This is the per-world config file for the world \"%s\"\n"
                                                + "Each label has `claimedChunks` or `unclaimedChunks` and `blockAccesses` or `entitiesAccesses`\n"
                                                + "Under each label, the code name of either entities or blocks appears, followed by a couple symbols.\n"
                                                + "These symbols are either `" + YES + "`, meaning YES, and `" + NO + "`, meaning NO.\n"
                                                + "For blocks, the order of these symbols is: allow breaking, allow exploding, allow interacting, and allow placing.\n"
                                                + "For entities, the order is: allow damaging, allow exploding, and allow interacting.\n\n"
                                                + "Examples:\n\n"
                                                + "To allow only interacting with all blocks in unclaimed chunks in this world:\n\n"
                                                + "unclaimedChunks.blockAccesses:\n"
                                                + "  " + DEFAULT + ": " + NO + NO + NO + YES + ";\n\n"
                                                + "(Note: the key `" + DEFAULT + "` can be used to mean \"all blocks/entities will have this if they are not defined here\")\n\n"
                                                + "Finally, the `_` key is for world properties. These will not vary between unclaimed and claimed chunks.\n"
                                                + "The `enabled` option will determine if ClaimChunk should be enabled for this world.";

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

    public @Nonnull EntityAccess getEntityAccess(boolean isClaimed, String worldName, EntityType entityType) {
        // Get all of the entity access mappings
        HashMap<EntityType, EntityAccess> entityAccesses = (isClaimed ? claimedChunks : unclaimedChunks).entityAccesses;

        // Get the access for this entity, if one is present
        EntityAccess access = entityAccesses.get(entityType);

        // If one is not present, get the default
        if (access == null) access = entityAccesses.get(EntityType.UNKNOWN);

        // If there is no default, then there should be a console error and assume a value of allow
        if (access == null) {
            Utils.err("Entity \"%s\" doesn't have a specific protection profile for world \"%s\" for %s chunks and a default could not be found!",
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
                                  @Nonnull String worldName,
                                  @Nonnull Material blockType,
                                  @Nonnull BlockAccessType accessType) {
        // If the chunk is claimed and the player has access, they can just
        // edit and interact with it as if it were their own. Then check
        // for the block access and determine if the player is allowed to
        // access it.
        return isOwnerOrAccess || checkBlockAccess(isOwned, worldName, blockType, accessType);
    }

    // Returns `true` if the player should be allowed to perform this action
    private boolean checkBlockAccess(boolean isClaimed,
                                     @Nonnull String worldName,
                                     @Nonnull Material blockType,
                                     @Nonnull BlockAccessType accessType) {
        // Check for the type of access
        return accessType.shouldAllow.apply(getBlockAccess(isClaimed, worldName, blockType));
    }

    private @Nonnull BlockAccess getBlockAccess(boolean isClaimed,
                                                @Nonnull String worldName,
                                                @Nonnull Material blockType) {
        // Get all of the entity access mappings
        HashMap<Material, BlockAccess> blockAccesses = (isClaimed ? claimedChunks : unclaimedChunks).blockAccesses;

        // Get the access for this entity, if one is present
        BlockAccess access = blockAccesses.getOrDefault(blockType, blockAccesses.get(Material.AIR));
        Utils.debug("Block access: %s", access);

        // If there is no default, then there should be a console error and assume a value of allow
        if (access == null) {
            Utils.err("Block \"%s\" doesn't have a specific protection profile for world \"%s\" for %s chunks and a default could not be found!",
                      blockType,
                      worldName,
                      isClaimed ? "claimed" : "unclaimed");
            access = new BlockAccess(true, true, true, true);
        }
        
        return access;
    }

    public CCConfig toCCConfig(String world) {
        final CCConfig config = new CCConfig(String.format(headerComment, world), "");

        // Write all the data to a config
        config.set("_.enabled", enabled);

        // Write entity accesses
        for (HashMap.Entry<EntityType, EntityAccess> entry : claimedChunks.entityAccesses.entrySet()) {
            config.set("claimedChunks.entityAccesses." + (entry.getKey() == EntityType.UNKNOWN ? DEFAULT : entry.getKey()),
                    String.format("%s%s%s",
                            entry.getValue().allowDamage ? YES : NO,
                            entry.getValue().allowExplosion ? YES : NO,
                            entry.getValue().allowInteract ? YES : NO));
        }
        for (HashMap.Entry<EntityType, EntityAccess> entry : unclaimedChunks.entityAccesses.entrySet()) {
            config.set("unclaimedChunks.entityAccesses." + (entry.getKey() == EntityType.UNKNOWN ? DEFAULT : entry.getKey()),
                    String.format("%s%s%s",
                            entry.getValue().allowDamage ? YES : NO,
                            entry.getValue().allowExplosion ? YES : NO,
                            entry.getValue().allowInteract ? YES : NO));
        }

        // Write block accesses
        for (HashMap.Entry<Material, BlockAccess> entry : claimedChunks.blockAccesses.entrySet()) {
            config.set("claimedChunks.blockAccesses." + (entry.getKey() == Material.AIR ? DEFAULT : entry.getKey()),
                    String.format("%s%s%s%s",
                            entry.getValue().allowBreak ? YES : NO,
                            entry.getValue().allowExplosion ? YES : NO,
                            entry.getValue().allowInteract ? YES : NO,
                            entry.getValue().allowPlace ? YES : NO));
        }
        for (HashMap.Entry<Material, BlockAccess> entry : unclaimedChunks.blockAccesses.entrySet()) {
            config.set("unclaimedChunks.blockAccesses." + (entry.getKey() == Material.AIR ? DEFAULT : entry.getKey()),
                    String.format("%s%s%s%s",
                            entry.getValue().allowBreak ? YES : NO,
                            entry.getValue().allowExplosion ? YES : NO,
                            entry.getValue().allowInteract ? YES : NO,
                            entry.getValue().allowPlace ? YES : NO));
        }

        return config;
    }

    public void fromCCConfig(@Nonnull CCConfig config) {
        for (HashMap.Entry<String, String> keyValue : config.values()) {
            // If it's the equals key, 
            if (keyValue.getKey().equals("_.enabled")) {
                enabled = config.getBool("_.enabled", enabled);
                continue;
            }

            Utils.debug("%s = %s", keyValue.getKey(), keyValue.getValue());

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
                        // Get the entity type
                        final EntityType actualEntityType = entityType.equals(DEFAULT)
                                                                    ? EntityType.UNKNOWN
                                                                    : EntityType.valueOf(entityType);
                        final boolean allowInteract = val[0] == YES;
                        final boolean allowDamage = val[1] == YES;
                        final boolean allowExplosion = val[2] == YES;

                        access.entityAccesses.computeIfAbsent(actualEntityType, (ignored) -> new EntityAccess())
                                             .update(allowInteract, allowDamage, allowExplosion);
                    } catch (Exception ignored) {
                        Utils.err("Invalid entity type: \"%s\" from key \"%s\"", entityType, keyValue.getKey());
                    }
                } else if (matcher.group(2).equals("blockAccesses")) {
                    // Get the info required to update the value in the config
                    String blockType = matcher.group(3);
                    Utils.debug("Type: %s", blockType);
                    char[] val = (keyValue.getValue() == null)
                            ? null
                            : keyValue.getValue().toCharArray();

                    // Make sure that there are three control character
                    if (val == null || val.length != 4) {
                        Utils.err("Invalid value while parsing block access: \"%s\"", Arrays.toString(val));
                        continue;
                    }

                    // Load the provided values
                    try {
                        // Get the entity type
                        final Material actualBlockType = blockType.equals(DEFAULT)
                                                                    ? Material.AIR
                                                                    : Material.valueOf(blockType);

                        boolean allowBreak = val[0] == YES;
                        boolean allowExplosion = val[1] == YES;
                        boolean allowInteract = val[2] == YES;
                        boolean allowPlace = val[3] == YES;

                        access.blockAccesses.computeIfAbsent(actualBlockType, (ignored) -> new BlockAccess())
                                            .update(allowBreak, allowExplosion, allowInteract, allowPlace);
                    } catch (Exception ignored) {
                        Utils.err("Invalid block type: \"%s\" from key \"%s\"", blockType, keyValue.getKey());
                    }
                }
            }
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
            update(allowInteract, allowDamage, allowExplosion);
        }

        private EntityAccess() {
            this(false, false, false);
        }

        public void update(boolean allowInteract, boolean allowDamage, boolean allowExplosion) {
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

        public BlockAccess(boolean allowInteract, boolean allowBreak, boolean allowPlace, boolean allowExplosion) {
            update(allowInteract, allowBreak, allowPlace, allowExplosion);
        }

        private BlockAccess() {
            this(false, false, false, false);
        }

        public void update(boolean allowInteract, boolean allowBreak, boolean allowPlace, boolean allowExplosion) {
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
