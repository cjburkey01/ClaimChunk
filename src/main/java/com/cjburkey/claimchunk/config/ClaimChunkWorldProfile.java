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
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

    private static final String CLASS_STARTING_CHARS = "_._";
    private static final String ENTITY_CLASS_KEY = CLASS_STARTING_CHARS + "@E_";
    private static final String BLOCK_CLASS_KEY = CLASS_STARTING_CHARS + "@B_";

    private static final String KEY = "^ (claimedChunks | unclaimedChunks) \\. (entityAccesses | blockAccesses) \\. ([a-zA-Z0-9\\-_]+) $";
    private static final Pattern KEY_PAT = Pattern.compile(KEY, Pattern.COMMENTS);

    // Whether ClaimChunk is enabled in this world
    public boolean enabled;

    // Classes for entities/blocks to make life so much easier later
    public final HashMap<String, HashSet<EntityType>> entityClasses = new HashMap<>();
    public final HashMap<String, HashSet<Material>> blockClasses = new HashMap<>();

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

    // Commands to be blocked while in another player's claimed chunk
    public HashSet<String> blockedCmdsInDiffClaimed = new HashSet<>();
    // Commands to be blocked while in a player's own claimed chunk
    public HashSet<String> blockedCmdsInOwnClaimed = new HashSet<>();
    // Commands to be blocked while in an unclaimed chunk
    public HashSet<String> blockedCmdsInUnclaimed = new HashSet<>();

    // Chunk accesses
    public final Accesses claimedChunks;
    public final Accesses unclaimedChunks;

    public ClaimChunkWorldProfile(boolean enabled, @Nullable Accesses claimedChunks, @Nullable Accesses unclaimedChunks) {
        this.enabled = enabled;

        // Make sure the access storage isn't null
        if (Objects.isNull(claimedChunks)) {
            claimedChunks = new Accesses(new HashMap<>(), new HashMap<>());
        }
        if (Objects.isNull(unclaimedChunks)) {
            unclaimedChunks = new Accesses(new HashMap<>(), new HashMap<>());
        }

        this.claimedChunks = claimedChunks;
        this.unclaimedChunks = unclaimedChunks;
    }

    // Clone
    public ClaimChunkWorldProfile(ClaimChunkWorldProfile original) {
        this.enabled = original.enabled;

        this.entityClasses.putAll(Utils.deepCloneMap(original.entityClasses, HashSet::new));
        this.blockClasses.putAll(Utils.deepCloneMap(original.blockClasses, HashSet::new));

        this.fireSpread = new FullSpreadProfile(original.fireSpread);
        this.waterSpread = new FullSpreadProfile(original.waterSpread);
        this.lavaSpread = new FullSpreadProfile(original.lavaSpread);
        this.pistonExtend = new SpreadProfile(original.pistonExtend);

        this.preventAdjacent = new HashSet<>(original.preventAdjacent);

        this.blockedCmdsInDiffClaimed.addAll(original.blockedCmdsInDiffClaimed);
        this.blockedCmdsInOwnClaimed.addAll(original.blockedCmdsInOwnClaimed);
        this.blockedCmdsInUnclaimed.addAll(original.blockedCmdsInUnclaimed);

        this.claimedChunks = new Accesses(original.claimedChunks);
        this.unclaimedChunks = new Accesses(original.unclaimedChunks);
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

    public void toCCConfig(@Nonnull CCConfig config) {
        // Write all the data to a config
        config.set("_.enabled", enabled);

        // Write entity and block classes
        entityClasses.forEach((name, entities)
                -> config.setList(ENTITY_CLASS_KEY + name, entities.stream()
                .map(EntityType::name)
                .collect(Collectors.toSet())));
        blockClasses.forEach((name, blocks)
                -> config.setList(BLOCK_CLASS_KEY + name, blocks.stream()
                .map(Material::name)
                .collect(Collectors.toSet())));

        // Fire spread configs
        fireSpread.toCCConfig(config);
        // Water spread configs
        waterSpread.toCCConfig(config);
        // Lava spread configs
        lavaSpread.toCCConfig(config);
        // Piston protection configs
        pistonExtend.toCCConfig(config);

        // Command blocking configs
        config.setList("claimedChunks.other.blockedCmds", blockedCmdsInDiffClaimed);
        config.setList("claimedChunks.owned.blockedCmds", blockedCmdsInOwnClaimed);
        config.setList("unclaimedChunks.blockedCmds", blockedCmdsInUnclaimed);

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
        config.values()
                .stream()
                // Make sure these are protections. This should ideally be made
                // a little more elegant than this particular solution.
                .filter(kv -> kv.getKey().startsWith("claimedChunks")
                        || kv.getKey().startsWith("unclaimedChunks"))
                // Load the permissions; permissions may be for classes, which
                // will return a new permission for each member of that class.
                .forEach(val -> this.loadPermissions(val, config));
    }

    // Generics make this method look a little more confusing than it has to,
    // I only did that so I didn't have to have two separate methods to handle
    // block accesses and entity classes.
    private @Nonnull <Type extends Enum<Type>> HashMap<String, HashSet<Type>> loadClasses(
            @Nonnull Class<Type> enumType,
            @Nonnull CCConfig config,
            @Nonnull String key,
            @Nonnull String debugName) {
        HashMap<String, HashSet<Type>> classes = new HashMap<>();

        // I think the streams API is fairly readable, but I'll add comments
        // just in case anyone is curious.
        config.values()
                .stream()
                // Only load config values that start with the given key
                .filter(val -> val.getKey().startsWith(key))
                .map(kv -> {
                    HashSet<Type> finishedSet = new HashSet<>();

                    // Get the entities/blocks within this class
                    // TODO: The format of the string list should probably be
                    //       made a little nicer.
                    for (String listedType : config.getStrList(kv.getKey())) {
                        try {
                            finishedSet.add(Type.valueOf(enumType, listedType));
                        } catch (Exception e) {
                            Utils.err("Failed to get %s by name \"%s\"", debugName, listedType);
                        }
                    }

                    // Debug (shrug)
                    Utils.debug("Loaded %s class %s with:", debugName, kv.getKey().substring(key.length()));
                    Utils.debug("    %s", finishedSet.stream()
                            .map(Enum::name)
                            .collect(Collectors.joining(", ")));

                    // Map to a map entry that can be inserted into a map
                    return new AbstractMap.SimpleEntry<>(kv.getKey(), finishedSet);
                })
                // Ignore empty classes
                .filter(kv -> !kv.getValue().isEmpty())
                // Move into the output map (minus starting 3 chars)
                .forEach(kv -> classes.put(kv.getKey().substring(3), kv.getValue()));

        return classes;
    }

    private void loadPermissions(@Nonnull Map.Entry<String, String> keyValue,
                                          @Nonnull CCConfig config) {
        // Use regex to check which keys are for chunk permissions
        final Matcher matcher = KEY_PAT.matcher(keyValue.getKey());
        if (!matcher.matches() || matcher.groupCount() < 3) {
            return;
        }

        // Get the access depending on whether this is for
        // claimed/unclaimed chunks.
        Accesses accesses = matcher.group(1).equals("claimedChunks")
                ? claimedChunks
                : unclaimedChunks;

        // Get the name of the protection
        String strType = matcher.group(3);
        if (strType == null) {
            return;
        }

        // Get the protection value
        String strValue = keyValue.getValue();
        if (strValue == null) {
            Utils.err("Failed to load protection for %s", strType);
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
                    EntityAccess::new,
                    entityClasses,
                    config
            );
        } else {
            // Block
            addPermissionsFromValue(
                    strType,
                    keyValue.getKey(),
                    "block",
                    () -> Material.AIR,
                    Material::valueOf,
                    accesses.blockAccesses,
                    BlockAccess::new,
                    blockClasses,
                    config
            );
        }
    }

    // THIS FUNCTION MUTATES. IT'S ANNOYINGLY COMPLICATED!
    // I'M SORRY!
    private <T, V extends ICCConfigSerializable>
    void addPermissionsFromValue(@Nonnull String strType,
                                 @Nonnull String key,
                                 String debugVal,
                                 Supplier<T> getDefaultType,
                                 Function<String, T> getByName,
                                 // This map will be mutated!
                                 HashMap<T, V> accessMap,
                                 Supplier<V> genNewAccess,
                                 HashMap<String, HashSet<T>> classMap,
                                 CCConfig config) {
        // Get the value of the access type, aka which protections to apply.
        V newAccess = genNewAccess.get();
        newAccess.fromCCConfig(config, key);

        // Get the type for this
        T actualType;
        if (key.equals(DEFAULT)) {
            // The default entity/block is set to these values internally. I
            // hope this choice doesn't come back to bite me in the ass.
            actualType = getDefaultType.get();
        } else {
            try {
                // This will throw an exception if the entity/block isn't found.
                actualType = getByName.apply(key);
            } catch (Exception ignored) {
                // Get the members of this particular class
                HashSet<T> classMembers = classMap.get(key);

                // Check if the given value wasn't a valid class
                if (classMembers == null) {
                    // This wasn't a particular entity/block or class
                    Utils.err("Invalid %s type or class: \"%s\" in world config from value: \"%s\"",
                            debugVal,
                            key,
                            strType);
                    return;
                }

                // Add the access for each member of the class
                classMembers.forEach(member -> accessMap.put(member, newAccess));
                return;
            }
        }

        // Add the new protection to the specified entity/block or the default
        // access
        accessMap.put(actualType, newAccess);
    }

    // Loads all the commands and double checks with Spigot that they exist
    private static Set<PluginCommand> getCommands(Collection<String> commandNames) {
        return commandNames.stream()
                .map(cmd -> {
                    PluginCommand pluginCmd = Bukkit.getPluginCommand(cmd);
                    if (pluginCmd == null) {
                        Utils.err("Invalid command \"%s\" in ClaimChunk blocked commands", cmd);
                    }
                    return pluginCmd;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

}
