package com.cjburkey.claimchunk.config;

import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.config.access.Access;
import com.cjburkey.claimchunk.config.access.AccessWrapper;
import com.cjburkey.claimchunk.config.access.BlockAccess;
import com.cjburkey.claimchunk.config.access.EntityAccess;
import com.cjburkey.claimchunk.config.ccconfig.CCConfig;
import com.cjburkey.claimchunk.config.spread.FullSpreadProfile;
import com.cjburkey.claimchunk.config.spread.SpreadProfile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
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

    private static final String ENTITY_CLASS_KEY = "_._@E_";
    private static final String BLOCK_CLASS_KEY = "_._@B_";

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
    public HashMap<String, Command> blockedCmdsInDiffClaimed = new HashMap<>();

    // Commands to be blocked while in a player's own claimed chunk
    public HashMap<String, Command> blockedCmdsInOwnClaimed = new HashMap<>();

    // Commands to be blocked while in an unclaimed chunk
    public HashMap<String, Command> blockedCmdsInUnclaimed = new HashMap<>();

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
        config.setList("claimedChunks.other.blockedCmds", blockedCmdsInDiffClaimed.values());
        config.setList("claimedChunks.owned.blockedCmds", blockedCmdsInOwnClaimed.values());
        config.setList("unclaimedChunks.blockedCmds", blockedCmdsInUnclaimed.values());

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

    @SuppressWarnings("Convert2MethodRef")
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
        blockedCmdsInDiffClaimed.clear();
        getCommands(config.getStrList("claimedChunks.other.blockedCmds"))
                .forEach(cmd -> blockedCmdsInDiffClaimed.put(cmd.getName(), cmd));

        // Load blocked commands for owned claimed chunks
        blockedCmdsInOwnClaimed.clear();
        getCommands(config.getStrList("claimedChunks.owned.blockedCmds"))
                .forEach(cmd -> blockedCmdsInOwnClaimed.put(cmd.getName(), cmd));

        // Load blocked commands for unclaimed chunks
        blockedCmdsInUnclaimed.clear();
        getCommands(config.getStrList("unclaimedChunks.blockedCmds"))
                .forEach(cmd -> blockedCmdsInUnclaimed.put(cmd.getName(), cmd));

        // Load block and entity classes
        entityClasses.clear();
        loadClasses(EntityType.class, config, ENTITY_CLASS_KEY, "entity")
                .forEach((k, v) -> entityClasses.put(k, v));
        blockClasses.clear();
        loadClasses(Material.class, config, BLOCK_CLASS_KEY, "block")
                .forEach((k, v) -> blockClasses.put(k, v));

        // Load permissions
        config.values()
                .stream()
                .filter(kv -> kv.getKey().startsWith("claimedChunks")
                        || kv.getKey().startsWith("unclaimedChunks"))
                .map(this::loadPermission)
                .filter(access -> !access.isNull())
                .forEach(access -> access.fromCCConfig(config, access.key));
    }

    private @Nonnull <Type extends Enum<Type>> HashMap<String, HashSet<Type>> loadClasses(
            @Nonnull Class<Type> enumType,
            @Nonnull CCConfig config,
            @Nonnull String key,
            @Nonnull String debugName) {
        // Read entity classes
        HashMap<String, HashSet<Type>> classes = new HashMap<>();

        config.values()
                .stream()
                .filter(val -> val.getKey().startsWith(key))
                .map(kv -> {
                    HashSet<Type> finishedSet = new HashSet<>();
                    for (String listedType : config.getStrList(kv.getKey())) {
                        try {
                            finishedSet.add(Type.valueOf(enumType, listedType));
                        } catch (Exception e) {
                            Utils.err("Failed to get %s by name \"%s\"", debugName, listedType);
                        }
                    }
                    Utils.debug("Read %s class %s with:", debugName, kv.getKey().substring(key.length()));
                    // WTF? why did I do this for debug?
                    Utils.debug("    %s", finishedSet.stream().collect(
                            (Supplier<StringBuilder>) StringBuilder::new,
                            (s, e) -> {
                                s.append(e.name());
                                s.append(", ");
                            },
                            StringBuilder::append));
                    return new AbstractMap.SimpleEntry<>(kv.getKey(), finishedSet);
                }).filter(kv -> !kv.getValue().isEmpty())
                .forEach(kv -> classes.put(kv.getKey().substring(3), kv.getValue()));

        return classes;
    }

    private @Nonnull AccessWrapper loadPermission(@Nonnull Map.Entry<String, String> keyValue) {
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
                    Utils.err("Invalid value while parsing entity access %s: \"%s\"",
                            entityType,
                            keyValue.getKey());
                    return new AccessWrapper(keyValue.getKey());
                }

                // Get the entity type
                EntityType actualEntityType = null;
                if (entityType.equals(DEFAULT)) {
                    actualEntityType = EntityType.UNKNOWN;
                } else {
                    try {
                        actualEntityType = EntityType.valueOf(entityType);
                    } catch (Exception ignored) {
                        // TODO: CLASSES
                        Utils.err("Invalid entity type or class: \"%s\" in world config: \"%s\"=\"%s\"",
                                entityType,
                                keyValue.getKey(),
                                keyValue.getValue());
                        return new AccessWrapper(keyValue.getKey());
                    }
                }

                // Get the entity access (or create a new one)
                return new AccessWrapper(keyValue.getKey(), access.entityAccesses
                        .computeIfAbsent(actualEntityType, ignored -> new EntityAccess()));
            } else if (matcher.group(2).equals("blockAccesses")) {
                // Get the info required to update the value in the config
                String blockType = matcher.group(3);

                // Get the value
                String value = keyValue.getValue();
                if (value == null) {
                    Utils.err("Invalid value while parsing block access %s: \"%s\"",
                            blockType,
                            keyValue.getKey());
                    return new AccessWrapper(keyValue.getKey());
                }

                // Get the block type
                final Material actualBlockType;
                if (blockType.equals(DEFAULT)) {
                    actualBlockType = Material.AIR;
                } else {
                    try {
                        actualBlockType = Material.valueOf(blockType);
                    } catch (Exception ignored) {
                        // TODO: CLASSES
                        Utils.err("Invalid block type: \"%s\" in world config line: \"%s\"=\"%s\"",
                                blockType,
                                keyValue.getKey(),
                                keyValue.getValue());
                        return new AccessWrapper(keyValue.getKey());
                    }
                }

                // Get the block access (or create a new one)
                return new AccessWrapper(keyValue.getKey(), access.blockAccesses
                        .computeIfAbsent(actualBlockType, ignored -> new BlockAccess()));
            } else {
                Utils.err("Invalid access target: \"%s\" in world config property: \"%s\"=\"%s\"",
                        matcher.group(2),
                        keyValue.getKey(),
                        keyValue.getValue());
            }
        }

        // Error
        return new AccessWrapper(keyValue.getKey());
    }

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
