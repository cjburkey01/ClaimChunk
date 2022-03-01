package com.cjburkey.claimchunk.config;

import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.config.access.Accesses;
import com.cjburkey.claimchunk.config.access.BlockAccess;
import com.cjburkey.claimchunk.config.access.EntityAccess;
import com.cjburkey.claimchunk.config.ccconfig.CCConfig;
import com.cjburkey.claimchunk.config.ccconfig.ICCConfigSerializable;
import com.cjburkey.claimchunk.config.spread.FullSpreadProfile;
import com.cjburkey.claimchunk.config.spread.SpreadProfile;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A class that represents the permissions that players have in a given world.
 *
 * @since 0.0.23
 */
public class ClaimChunkWorldProfile {

    static final String DEFAULT = "__DEFAULT__";

    private static final String CLASS_STARTING_CHARS = "_.";
    private static final String ENTITY_CLASS_KEY = CLASS_STARTING_CHARS + "_@E_.";
    private static final String BLOCK_CLASS_KEY = CLASS_STARTING_CHARS + "_@B_.";

    private static final String KEY =
            "^ (claimedChunks | unclaimedChunks) \\. (entityAccesses | blockAccesses) \\."
                    + " (@? [a-zA-Z0-9\\-_]+) $";
    private static final Pattern KEY_PAT = Pattern.compile(KEY, Pattern.COMMENTS);

    /**
     * Whether ClaimChunk will be enabled in this world. If ClaimChunk is disabled for a world, no
     * one will be able to make any claims, including admins.
     */
    public boolean enabled;
    /** Whether players' claims should be protected when they are offline. */
    public boolean protectOffline = true;
    /** Whether players' claims should be protected when they are online. */
    public boolean protectOnline = true;
    /** If this is true, non-owner or access players won't be able to use ender pearls within claimed chunks. */
    public boolean preventPearlFromClaims = false;

    /** Mapping from entity config class names to a set of entities for that class. */
    public final HashMap<String, HashSet<EntityType>> entityClasses = new HashMap<>();
    /** Mapping from block config class names to a set of blocks (materials) for that class. */
    public final HashMap<String, HashSet<Material>> blockClasses = new HashMap<>();

    private static final String FIRE_SPREAD_KEY = "allow_spread.fire";
    private static final String WATER_SPREAD_KEY = "allow_spread.water";
    private static final String LAVA_SPREAD_KEY = "allow_spread.lava";
    private static final String PISTON_EXTEND_KEY = "allow_piston";

    /** The fire spread config protection profile. */
    public FullSpreadProfile fireSpread = new FullSpreadProfile();
    /** The water spread config protection profile. */
    public FullSpreadProfile waterSpread = new FullSpreadProfile();
    /** The lava spread config protection profile. */
    public FullSpreadProfile lavaSpread = new FullSpreadProfile();
    /** The piston extension config protection profile. */
    public SpreadProfile pistonExtend = new SpreadProfile();

    /** A set of blocks for which to deny neighboring (same) block placement. */
    public final HashSet<Material> preventAdjacent =
            new HashSet<>(Arrays.asList(Material.CHEST, Material.TRAPPED_CHEST));

    /** A set of commands that should be denied for un-owning players in claimed chunks. */
    public HashSet<String> blockedCmdsInDiffClaimed = new HashSet<>();
    /** A set of commands that should be denied for players in their own claimed chunks. */
    public HashSet<String> blockedCmdsInOwnClaimed = new HashSet<>();
    /** A set of commands that should be denied for players in unclaimed chunks. */
    public HashSet<String> blockedCmdsInUnclaimed = new HashSet<>();

    /** The access storage for claimed chunks. */
    public final Accesses claimedChunks;
    /** The access storage for unclaimed chunks. */
    public final Accesses unclaimedChunks;

    public ClaimChunkWorldProfile(
            boolean enabled, @Nullable Accesses claimedChunks, @Nullable Accesses unclaimedChunks) {
        this.enabled = enabled;

        // Make sure the access storage isn't null
        if (Objects.isNull(claimedChunks)) {
            claimedChunks =
                    new Accesses(
                            new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());
        }
        if (Objects.isNull(unclaimedChunks)) {
            unclaimedChunks =
                    new Accesses(
                            new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());
        }

        this.claimedChunks = claimedChunks;
        this.unclaimedChunks = unclaimedChunks;
    }

    /**
     * Copies the provided profile to make a new one.
     *
     * @param old The old world profile.
     */
    public ClaimChunkWorldProfile(@NotNull ClaimChunkWorldProfile old) {
        // Copy basic settings
        this.enabled = old.enabled;
        this.protectOffline = old.protectOffline;
        this.protectOnline = old.protectOnline;
        this.preventPearlFromClaims = old.preventPearlFromClaims;

        // Copy entity/block classes
        old.entityClasses.forEach(
                (className, members) -> this.entityClasses.put(className, new HashSet<>(members)));
        old.blockClasses.forEach(
                (className, members) -> this.blockClasses.put(className, new HashSet<>(members)));

        // Copy spread profiles
        this.fireSpread = old.fireSpread;
        this.waterSpread = old.waterSpread;
        this.lavaSpread = old.lavaSpread;
        this.pistonExtend = old.pistonExtend;

        // Copy adjacent prevention
        this.preventAdjacent.addAll(old.preventAdjacent);

        // Copy blocked commands
        this.blockedCmdsInDiffClaimed.addAll(old.blockedCmdsInDiffClaimed);
        this.blockedCmdsInOwnClaimed.addAll(old.blockedCmdsInOwnClaimed);
        this.blockedCmdsInUnclaimed.addAll(old.blockedCmdsInUnclaimed);

        // Block access mappings
        this.claimedChunks = new Accesses(old.claimedChunks);
        this.unclaimedChunks = new Accesses(old.unclaimedChunks);
    }

    // Returns `true` if the player should be allowed to perform this action
    public boolean canAccessEntity(
            boolean isOwned,
            boolean isOwnerOrAccess,
            @NotNull Entity entity,
            @NotNull EntityAccess.EntityAccessType accessType) {
        // If the chunk is claimed and the player has access, they can just
        // edit and interact with it as if it were their own. Then check
        // for the entity access and determine if the player is allowed to
        // access it.
        return isOwnerOrAccess
                || checkEntityAccess(
                        isOwned, entity.getWorld().getName(), entity.getType(), accessType);
    }

    // Returns `true` if the player should be allowed to perform this action
    private boolean checkEntityAccess(
            boolean isClaimed,
            String worldName,
            @NotNull EntityType entityType,
            @NotNull EntityAccess.EntityAccessType accessType) {
        // Check for the type of access
        return accessType.getShouldAllow(getEntityAccess(isClaimed, worldName, entityType));
    }

    public @NotNull EntityAccess getEntityAccess(
            boolean isClaimed, String worldName, EntityType entityType) {
        // Get all of the entity access mappings
        HashMap<EntityType, EntityAccess> entityAccesses =
                (isClaimed ? claimedChunks : unclaimedChunks).liveEntityAccesses;

        // Get the access for this entity, if one is present
        EntityAccess access = entityAccesses.get(entityType);

        // If one is not present, get the default
        if (access == null) access = entityAccesses.get(EntityType.UNKNOWN);

        // If there is no default, then there should be a console error and assume a value of allow
        if (access == null) {
            Utils.err(
                    "Entity \"%s\" doesn't have a specific protection profile for world \"%s\" for"
                            + " %s chunks and a default could not be found!",
                    entityType, worldName, isClaimed ? "claimed" : "unclaimed");
            access = new EntityAccess(true, true, true);
        }

        return access;
    }

    // Returns `true` if the player should be allowed to perform this action
    public boolean canAccessBlock(
            boolean isOwned,
            boolean isOwnerOrAccess,
            @NotNull String worldName,
            @NotNull Material blockType,
            @NotNull BlockAccess.BlockAccessType accessType) {
        // If the chunk is claimed and the player has access, they can just
        // edit and interact with it as if it were their own. Then check
        // for the block access and determine if the player is allowed to
        // access it.
        return isOwnerOrAccess || checkBlockAccess(isOwned, worldName, blockType, accessType);
    }

    // Returns `true` if the player should be allowed to perform this action
    public boolean canAccessBlock(
            boolean isOwned,
            boolean isOwnerOrAccess,
            @NotNull World world,
            @NotNull Material blockType,
            @NotNull BlockAccess.BlockAccessType accessType) {
        // If the chunk is claimed and the player has access, they can just
        // edit and interact with it as if it were their own. Then check
        // for the block access and determine if the player is allowed to
        // access it.
        return canAccessBlock(isOwned, isOwnerOrAccess, world.getName(), blockType, accessType);
    }

    // Returns `true` if the player should be allowed to perform this action
    private boolean checkBlockAccess(
            boolean isClaimed,
            @NotNull String worldName,
            @NotNull Material blockType,
            @NotNull BlockAccess.BlockAccessType accessType) {
        // Check for the type of access
        return accessType.getShouldAllow(getBlockAccess(isClaimed, worldName, blockType));
    }

    public @NotNull BlockAccess getBlockAccess(
            boolean isClaimed, @NotNull String worldName, @NotNull Material blockType) {
        // Get all of the block access mappings
        HashMap<Material, BlockAccess> blockAccesses =
                (isClaimed ? claimedChunks : unclaimedChunks).liveBlockAccesses;

        // Get the access for this block, if one is present
        BlockAccess access = blockAccesses.getOrDefault(blockType, blockAccesses.get(Material.AIR));

        // If there is no default, then there should be a console error and assume a value of allow
        if (access == null) {
            Utils.err(
                    "Block \"%s\" doesn't have a specific protection profile for world \"%s\" for"
                            + " %s chunks and a default could not be found!",
                    blockType, worldName, isClaimed ? "claimed" : "unclaimed");
            access = new BlockAccess(true, true, true, true);
        }

        return access;
    }

    // Generics make this method look a little more confusing than it has to,
    // I only did that so I didn't have to have two separate methods to handle
    // block accesses and entity classes.
    private @NotNull <Type extends Enum<Type>> HashMap<String, HashSet<Type>> loadClasses(
            @NotNull Class<Type> enumType,
            @NotNull CCConfig config,
            @NotNull String key,
            @NotNull String debugName) {
        HashMap<String, HashSet<Type>> classes = new HashMap<>();

        // I think the streams API is fairly readable, but I'll add comments
        // just in case anyone is curious.
        config.values().stream()
                // Only load config values that start with the given key
                .filter(val -> val.getKey().startsWith(key))
                .map(
                        kv -> {
                            HashSet<Type> finishedSet = new HashSet<>();

                            // Get the entities/blocks within this class
                            for (String listedType : config.getStrList(kv.getKey())) {
                                try {
                                    finishedSet.add(Type.valueOf(enumType, listedType));
                                } catch (Exception e) {
                                    Utils.err(
                                            "Failed to get %s by name \"%s\"",
                                            debugName, listedType);
                                }
                            }

                            // Map to a map entry that can be inserted into a map
                            return new AbstractMap.SimpleEntry<>(kv.getKey(), finishedSet);
                        })
                // Ignore empty classes
                .filter(kv -> !kv.getValue().isEmpty())
                // Move into the output map (minus starting 3 chars)
                .forEach(kv -> classes.put(kv.getKey().substring(key.length()), kv.getValue()));

        return classes;
    }

    private void loadPermissions(
            @NotNull Map.Entry<String, String> keyValue, @NotNull CCConfig config) {
        // Use regex to check which keys are for chunk permissions
        final Matcher matcher = KEY_PAT.matcher(keyValue.getKey());
        if (!matcher.matches() || matcher.groupCount() < 3) {
            return;
        }

        // Get the access depending on whether this is for
        // claimed/unclaimed chunks.
        Accesses accesses =
                matcher.group(1).equals("claimedChunks") ? claimedChunks : unclaimedChunks;

        // Get the name of the protection
        String strType = matcher.group(3);
        if (strType == null) {
            return;
        }

        // Load the permissions from this value
        if (matcher.group(2).equals("entityAccesses")) {
            // Entity
            addPermissionsFromValue(
                    strType,
                    keyValue.getKey(),
                    "entity",
                    () -> EntityType.UNKNOWN,
                    EntityType::valueOf,
                    accesses.entityAccesses,
                    accesses.liveEntityAccesses,
                    accesses.entityAccessClassMapping,
                    EntityAccess::new,
                    entityClasses,
                    config);
        } else {
            // Block
            addPermissionsFromValue(
                    strType,
                    keyValue.getKey(),
                    "block",
                    () -> Material.AIR,
                    Material::valueOf,
                    accesses.blockAccesses,
                    accesses.liveBlockAccesses,
                    accesses.blockAccessClassMapping,
                    BlockAccess::new,
                    blockClasses,
                    config);
        }
    }

    // THIS FUNCTION MUTATES. IT'S ANNOYINGLY COMPLICATED!
    // I'M SORRY!
    private <T, V extends ICCConfigSerializable> void addPermissionsFromValue(
            @NotNull String strType,
            @NotNull String key,
            String debugVal,
            Supplier<T> getDefaultType,
            Function<String, T> getByName,
            // These map will be mutated!
            HashMap<T, V> accessMap,
            HashMap<T, V> liveAccessMap,
            HashMap<String, V> accessClassMapping,
            Supplier<V> genNewAccess,
            HashMap<String, HashSet<T>> classMap,
            CCConfig config) {
        // Get the value of the access type, aka which protections to apply.
        V newAccess = genNewAccess.get();
        newAccess.fromCCConfig(config, key);

        // Get the type for this
        T actualType;
        if (strType.equals(DEFAULT)) {
            // The default entity/block is set to these values internally. I
            // hope this choice doesn't come back to bite me in the ass.
            actualType = getDefaultType.get();
        } else {
            try {
                // This will throw an exception if the entity/block isn't found.
                actualType = getByName.apply(strType);
            } catch (Exception ignored) {
                // Remove the '@'
                String className = strType.substring(1);

                // Get the members of this particular class
                HashSet<T> classMembers = classMap.get(className);

                // Check if the given value wasn't a valid class either
                if (classMembers == null) {
                    // This wasn't a particular entity/block or class
                    Utils.err(
                            "Invalid %s type or class: \"%s\" in world config from \"%s\"",
                            debugVal, strType, key);
                    return;
                }

                // Add the access for each member of the class
                classMembers.forEach(member -> liveAccessMap.put(member, newAccess));
                accessClassMapping.put(className, newAccess);
                return;
            }
        }

        // Add the new protection to the specified entity/block or the default
        // access
        accessMap.put(actualType, newAccess);
        liveAccessMap.put(actualType, newAccess);
    }

    // Loads all the commands and double checks with Spigot that they exist
    private static Set<PluginCommand> getCommands(Collection<String> commandNames) {
        return commandNames.stream()
                .map(
                        cmd -> {
                            PluginCommand pluginCmd = Bukkit.getPluginCommand(cmd);
                            if (pluginCmd == null) {
                                Utils.err(
                                        "Invalid command \"%s\" in ClaimChunk blocked commands",
                                        cmd);
                            }
                            return pluginCmd;
                        })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public void toCCConfig(@NotNull CCConfig config) {
        // Write basic boolean config values
        config.set("_.enabled", enabled);
        config.set("_.protectOffline", protectOffline);
        config.set("_.protectOnline", protectOnline);
        config.set("_.preventPearlFromClaims", preventPearlFromClaims);

        // Write entity and block classes
        entityClasses.forEach(
                (name, entities) ->
                        config.setList(
                                ENTITY_CLASS_KEY + name,
                                entities.stream()
                                        .map(EntityType::name)
                                        .collect(Collectors.toSet())));
        blockClasses.forEach(
                (name, blocks) ->
                        config.setList(
                                BLOCK_CLASS_KEY + name,
                                blocks.stream().map(Material::name).collect(Collectors.toSet())));

        // Fire spread configs
        fireSpread.toCCConfig(config, FIRE_SPREAD_KEY);
        // Water spread configs
        waterSpread.toCCConfig(config, WATER_SPREAD_KEY);
        // Lava spread configs
        lavaSpread.toCCConfig(config, LAVA_SPREAD_KEY);
        // Piston protection configs
        pistonExtend.toCCConfig(config, PISTON_EXTEND_KEY);

        // Change types of adjacent blocks to check, empty this list to stop checking
        config.setList("_.preventAdjacent", preventAdjacent);

        // Command blocking configs
        config.setList("claimedChunks.other.blockedCmds", blockedCmdsInDiffClaimed);
        config.setList("claimedChunks.owned.blockedCmds", blockedCmdsInOwnClaimed);
        config.setList("unclaimedChunks.blockedCmds", blockedCmdsInUnclaimed);

        // Write entity accesses
        for (HashMap.Entry<EntityType, EntityAccess> entry :
                claimedChunks.entityAccesses.entrySet()) {
            entry.getValue()
                    .toCCConfig(
                            config,
                            "claimedChunks.entityAccesses."
                                    + (entry.getKey() == EntityType.UNKNOWN
                                            ? DEFAULT
                                            : entry.getKey()));
        }
        for (HashMap.Entry<EntityType, EntityAccess> entry :
                unclaimedChunks.entityAccesses.entrySet()) {
            entry.getValue()
                    .toCCConfig(
                            config,
                            "unclaimedChunks.entityAccesses."
                                    + (entry.getKey() == EntityType.UNKNOWN
                                            ? DEFAULT
                                            : entry.getKey()));
        }

        // Write entity accesses classes
        for (HashMap.Entry<String, EntityAccess> entry :
                claimedChunks.entityAccessClassMapping.entrySet()) {
            entry.getValue().toCCConfig(config, "claimedChunks.entityAccesses.@" + entry.getKey());
        }
        for (HashMap.Entry<String, EntityAccess> entry :
                unclaimedChunks.entityAccessClassMapping.entrySet()) {
            entry.getValue()
                    .toCCConfig(config, "unclaimedChunks.entityAccesses.@" + entry.getKey());
        }

        // Write block accesses
        for (HashMap.Entry<Material, BlockAccess> entry : claimedChunks.blockAccesses.entrySet()) {
            entry.getValue()
                    .toCCConfig(
                            config,
                            "claimedChunks.blockAccesses."
                                    + (entry.getKey() == Material.AIR ? DEFAULT : entry.getKey()));
        }
        for (HashMap.Entry<Material, BlockAccess> entry :
                unclaimedChunks.blockAccesses.entrySet()) {
            entry.getValue()
                    .toCCConfig(
                            config,
                            "unclaimedChunks.blockAccesses."
                                    + (entry.getKey() == Material.AIR ? DEFAULT : entry.getKey()));
        }

        // Write block accesses classes
        for (HashMap.Entry<String, BlockAccess> entry :
                claimedChunks.blockAccessClassMapping.entrySet()) {
            entry.getValue().toCCConfig(config, "claimedChunks.blockAccesses.@" + entry.getKey());
        }
        for (HashMap.Entry<String, BlockAccess> entry :
                unclaimedChunks.blockAccessClassMapping.entrySet()) {
            entry.getValue().toCCConfig(config, "unclaimedChunks.blockAccesses.@" + entry.getKey());
        }
    }

    public void fromCCConfig(@NotNull CCConfig config) {
        // Load basic boolean config options
        enabled = config.getBool("_.enabled", enabled);
        protectOffline = config.getBool("_.protectOffline", protectOffline);
        protectOnline = config.getBool("_.protectOnline", protectOnline);
        preventPearlFromClaims = config.getBool("_.preventPearlFromClaims", preventPearlFromClaims);

        // Load fire spread properties
        fireSpread.fromCCConfig(config, FIRE_SPREAD_KEY);
        // Load water spread properties
        waterSpread.fromCCConfig(config, WATER_SPREAD_KEY);
        // Load lava spread properties
        lavaSpread.fromCCConfig(config, LAVA_SPREAD_KEY);
        // Load piston protection properties
        pistonExtend.fromCCConfig(config, PISTON_EXTEND_KEY);

        // Load list of adjacent block types to check
        preventAdjacent.clear();
        config.getStrList("_.preventAdjacent").stream()
                .map(
                        blockType -> {
                            Material material = Material.getMaterial(blockType);
                            if (material == null) {
                                Utils.warn(
                                        "Material type \"%s\" not found when loading from"
                                                + " preventAdjacent, this one will be removed!",
                                        blockType);
                            }
                            return material;
                        })
                .filter(Objects::nonNull)
                .forEach(preventAdjacent::add);

        // Load blocked commands for unowned claimed chunks
        // (`getCommands()` will verify the commands actually exist)
        blockedCmdsInDiffClaimed.clear();
        getCommands(config.getStrList("claimedChunks.other.blockedCmds"))
                .forEach(cmd -> blockedCmdsInDiffClaimed.add(cmd.getName()));

        // Load blocked commands for owned claimed chunks
        // (getCommands()` will verify the commands actually exist)
        blockedCmdsInOwnClaimed.clear();
        getCommands(config.getStrList("claimedChunks.owned.blockedCmds"))
                .forEach(cmd -> blockedCmdsInOwnClaimed.add(cmd.getName()));

        // Load blocked commands for unclaimed chunks
        // (getCommands()` will verify the commands actually exist)
        blockedCmdsInUnclaimed.clear();
        getCommands(config.getStrList("unclaimedChunks.blockedCmds"))
                .forEach(cmd -> blockedCmdsInUnclaimed.add(cmd.getName()));

        // Load block and entity classes
        entityClasses.clear();
        entityClasses.putAll(loadClasses(EntityType.class, config, ENTITY_CLASS_KEY, "entity"));
        blockClasses.clear();
        blockClasses.putAll(loadClasses(Material.class, config, BLOCK_CLASS_KEY, "block"));

        // Load permissions
        config.values().stream()
                // Make sure these are protections. This should ideally be made
                // a little more elegant than this particular solution.
                .filter(
                        kv ->
                                kv.getKey().startsWith("claimedChunks")
                                        || kv.getKey().startsWith("unclaimedChunks"))
                // Load the permissions; permissions may be for classes, which
                // will return a new permission for each member of that class.
                .forEach(val -> this.loadPermissions(val, config));
    }
}
