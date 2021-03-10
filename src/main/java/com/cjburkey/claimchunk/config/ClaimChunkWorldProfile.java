package com.cjburkey.claimchunk.config;

import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.config.access.Access;
import com.cjburkey.claimchunk.config.access.BlockAccess;
import com.cjburkey.claimchunk.config.access.EntityAccess;
import com.cjburkey.claimchunk.config.ccconfig.CCConfig;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class that represents the permissions that players have in a given world.
 *
 * @since 0.0.23
 */
public class ClaimChunkWorldProfile {

    private static final String DEFAULT = "__DEFAULT__";

    private static final String KEY = "^ (claimedChunks | unclaimedChunks) \\. (entityAccesses | blockAccesses) \\. ([a-zA-Z0-9\\-_]+) $";
    private static final Pattern KEY_PAT = Pattern.compile(KEY, Pattern.COMMENTS);
    protected static final String headerComment = "This config was last loaded with ClaimChunk version @PLUGIN_VERSION@\n\n"
                                                + "This is the per-world config file for the world \"%s\"\n\n"
                                                + "   _    _      _\n"
                                                + "  | |  | |    | |\n"
                                                + "  | |__| | ___| |_ __\n"
                                                + "  |  __  |/ _ \\ | '_ \\\n"
                                                + "  | |  | |  __/ | |_) |\n"
                                                + "  |_|  |_|\\___|_| .__/\n"
                                                + "                | |\n"
                                                + "                |_|\n"
                                                + " -----------------------\n\n"
                                                + "Each label has `claimedChunks` or `unclaimedChunks` and `blockAccesses` or `entitiesAccesses`\n"
                                                + "Under each label, the code name of either an entity or block appears, followed by the protections (order for protections does *NOT* matter).\n"
                                                + "Protections with a value of `true` will be allowed, those with a value of `false` will not."
                                                + "For blocks, the protections are: `B` for breaking, `P` for placing, `I` for interacting, and `E` for exploding.\n"
                                                + "For entities, the protections are: `D` for damaging, `I` for interacting, and `E` for exploding.\n"    // hehe, "DIE" lol
                                                + "Note: These protections (except for exploding) are purely player-based.\n"
                                                + "I.e. `D` for damaging entities, when set to `D:false` will prevent players from damaging the entity.\n\n"
                                                + "Examples:\n\n"
                                                + "To allow only interacting with all blocks in unclaimed chunks in this world:\n\n"
                                                + "unclaimedChunks.blockAccesses:\n"
                                                + "  " + DEFAULT + ":  I:true B:false P:false E:false ;\n\n"
                                                + "(Note: the key `" + DEFAULT + "` can be used to mean \"all blocks/entities will have this if they are not defined here\")\n\n"
                                                + "Finally, the `_` label is for world properties. These will not vary between unclaimed and claimed chunks.\n\n"
                                                // TODO: MAKE WIKI PAGE
                                                + "More information is available on the GitHub wiki: https://github.com/cjburkey01/ClaimChunk/wiki\n";

    // Whether ClaimChunk is enabled in this world
    public boolean enabled;

    // Fire protections
    public FullSpreadProfile fireSpread = new FullSpreadProfile("allow_spread.fire");

    // Water protections
    public FullSpreadProfile waterSpread = new FullSpreadProfile("allow_spread.water");

    // Water protections
    public FullSpreadProfile lavaSpread = new FullSpreadProfile("allow_spread.lava");

    // Piston protections
    public SpreadProfile pistonExtend = new SpreadProfile("allow_piston");

    // Prevent chest connections
    public HashSet<Material> preventAdjacent = new HashSet<>(Arrays.asList(Material.CHEST, Material.TRAPPED_CHEST));

    // Chunk accesses
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
                                   @Nonnull EntityAccess.EntityAccessType accessType) {
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
                                      @Nonnull EntityAccess.EntityAccessType accessType) {
        // Check for the type of access
        return accessType.getShouldAllow(getEntityAccess(isClaimed, worldName, entityType));
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
                                  @Nonnull BlockAccess.BlockAccessType accessType) {
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
                                     @Nonnull BlockAccess.BlockAccessType accessType) {
        // Check for the type of access
        return accessType.getShouldAllow(getBlockAccess(isClaimed, worldName, blockType));
    }

    public @Nonnull BlockAccess getBlockAccess(boolean isClaimed,
                                               @Nonnull String worldName,
                                               @Nonnull Material blockType) {
        // Get all of the entity access mappings
        HashMap<Material, BlockAccess> blockAccesses = (isClaimed ? claimedChunks : unclaimedChunks).blockAccesses;

        // Get the access for this entity, if one is present
        BlockAccess access = blockAccesses.getOrDefault(blockType, blockAccesses.get(Material.AIR));

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

        // Fire spread configs
        fireSpread.toCCConfig(config);

        // Water spread configs
        waterSpread.toCCConfig(config);

        // Lava spread configs
        lavaSpread.toCCConfig(config);

        // Piston protection configs
        pistonExtend.toCCConfig(config);

        // Write entity accesses
        for (HashMap.Entry<EntityType, EntityAccess> entry : claimedChunks.entityAccesses.entrySet()) {
            entry.getValue().toCCConfig(config, "claimedChunks.entityAccesses."
                    + (entry.getKey() == EntityType.UNKNOWN
                        ? DEFAULT
                        : entry.getKey()));
        }
        for (HashMap.Entry<EntityType, EntityAccess> entry : unclaimedChunks.entityAccesses.entrySet()) {
            entry.getValue().toCCConfig(config, "unclaimedChunks.entityAccesses."
                    + (entry.getKey() == EntityType.UNKNOWN
                        ? DEFAULT
                        : entry.getKey()));
        }

        // Write block accesses
        for (HashMap.Entry<Material, BlockAccess> entry : claimedChunks.blockAccesses.entrySet()) {
            entry.getValue().toCCConfig(config, "claimedChunks.blockAccesses."
                    + (entry.getKey() == Material.AIR
                        ? DEFAULT
                        : entry.getKey()));
        }
        for (HashMap.Entry<Material, BlockAccess> entry : unclaimedChunks.blockAccesses.entrySet()) {
            entry.getValue().toCCConfig(config, "unclaimedChunks.blockAccesses."
                    + (entry.getKey() == Material.AIR
                    ? DEFAULT
                    : entry.getKey()));
        }

        return config;
    }

    public void fromCCConfig(@Nonnull CCConfig config) {
        // Load enabled key
        enabled = config.getBool("_.enabled", enabled);

        // Load fire spread properties
        fireSpread.fromCCConfig(config);

        // Load water spread properties
        waterSpread.fromCCConfig(config);

        // Load lava spread properties
        lavaSpread.fromCCConfig(config);

        // Load piston protection properties
        pistonExtend.fromCCConfig(config);

        // Load permissions
        for (HashMap.Entry<String, String> keyValue : config.values()) {
            // Skip the other ones
            if (!keyValue.getKey().startsWith("claimedChunks")
                    && !keyValue.getKey().startsWith("unclaimedChunks")) {
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

                    // Get the value
                    String value = keyValue.getValue();
                    if (value == null) {
                        Utils.err("Invalid value while parsing entity access: \"%s\" \"%s\"", entityType, keyValue.getKey());
                        continue;
                    }

                    // Get the block type
                    final EntityType actualEntityType = entityType.equals(DEFAULT)
                            ? EntityType.UNKNOWN
                            : EntityType.valueOf(entityType);

                    // Get the entity access:
                    EntityAccess ea = access.entityAccesses.computeIfAbsent(actualEntityType, ignored -> new EntityAccess());
                    ea.fromCCConfig(config, keyValue.getKey());
                } else if (matcher.group(2).equals("blockAccesses")) {
                    // Get the info required to update the value in the config
                    String blockType = matcher.group(3);

                    // Get the value
                    String value = keyValue.getValue();
                    if (value == null) {
                        Utils.err("Invalid value while parsing block access: \"%s\" \"%s\"", blockType, keyValue.getKey());
                        continue;
                    }

                    // Get the block type
                    final Material actualBlockType = blockType.equals(DEFAULT)
                            ? Material.AIR
                            : Material.valueOf(blockType);

                    // Get the block access:
                    BlockAccess ba = access.blockAccesses.computeIfAbsent(actualBlockType, (ignored) -> new BlockAccess());
                    ba.fromCCConfig(config, keyValue.getKey());
                }
            }
        }
    }

}
