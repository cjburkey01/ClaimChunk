package com.cjburkey.claimchunk.config;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.config.access.Accesses;
import com.cjburkey.claimchunk.config.access.BlockAccess;
import com.cjburkey.claimchunk.config.access.EntityAccess;
import com.cjburkey.claimchunk.config.ccconfig.*;

import org.bukkit.Material;
import org.bukkit.entity.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

// TODO: ADD TONS OF COMMENTS THIS IS SO RIDICULOUS!
public class ClaimChunkWorldProfileHandler {

    private static final String HEADER_COMMENT =
            "This config was last loaded with ClaimChunk version %s\n\n"
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
                + "Each label has `claimedChunks` or `unclaimedChunks` and `blockAccesses` or"
                + " `entitiesAccesses`\n"
                + "Under each label, the code name of either an entity or block appears, followed"
                + " by the protections (order for protections does *NOT* matter).\n"
                + "Protections with a value of `true` will be allowed, those with a value of"
                + " `false` will not.For blocks, the protections are: `B` for breaking, `P` for"
                + " placing, `I` for interacting, and `E` for exploding.\n"
                + "For entities, the protections are: `D` for damaging, `I` for interacting, and"
                + " `E` for exploding.\n" // hehe, "DIE" lol
                    + "Note: These protections (except for exploding) are purely player-based.\n"
                    + "I.e. `D` for damaging entities, when set to `D:false` will prevent players"
                    + " from damaging the entity.\n\n"
                    + "Examples:\n\n"
                    + "To allow only interacting with all blocks in unclaimed chunks in this"
                    + " world:\n\n"
                    + "unclaimedChunks.blockAccesses:\n"
                    + "  "
                    + ClaimChunkWorldProfile.DEFAULT
                    + ":  I:true B:false P:false E:false ;\n\n"
                    + "(Note: the key `"
                    + ClaimChunkWorldProfile.DEFAULT
                    + "` can be used to mean \"all blocks/entities will have this if they are not"
                    + " defined here\")\n\n"
                    + "Finally, the `_` label is for world properties. These will not vary between"
                    + " unclaimed and claimed chunks.\n\n"
                    // TODO: MAKE WIKI PAGE
                    + "More information will be available on the website:"
                    + " https://claimchunk.cjburkey.com\n";

    private final ClaimChunk claimChunk;

    // Config management
    private final File worldConfigDir;
    private final HashMap<String, ClaimChunkWorldProfile> profiles;

    private final CCConfigParser parser;
    private final CCConfigWriter writer;

    public ClaimChunkWorldProfileHandler(
            @NotNull ClaimChunk claimChunk,
            @NotNull File worldConfigDir,
            @NotNull CCConfigParser parser,
            @NotNull CCConfigWriter writer) {
        this.claimChunk = claimChunk;

        this.worldConfigDir = worldConfigDir;
        profiles = new HashMap<>();

        this.parser = parser;
        this.writer = writer;
    }

    /**
     * Merges the profiles from the given HashMap into this world manager, overriding any existing
     * world profiles here.
     *
     * @param profiles The profiles to be merged.
     */
    public void mergeProfiles(HashMap<String, ClaimChunkWorldProfile> profiles) {
        this.profiles.putAll(profiles);
    }

    public @NotNull ClaimChunkWorldProfile getProfile(
            @NotNull String worldName, @Nullable ClaimChunkWorldProfile defaultProfile) {
        // Try to get the config from the ones already loaded
        return profiles.computeIfAbsent(
                worldName,
                n -> {
                    File file = new File(worldConfigDir, n + ".txt");

                    // Create a config handle for this file
                    CCConfigHandler<CCConfig> cfg =
                            new CCConfigHandler<>(
                                    file,
                                    new CCConfig(
                                            String.format(
                                                    HEADER_COMMENT,
                                                    claimChunk.getVersion(),
                                                    worldName),
                                            ""));

                    // Whether the file can be rewritten without losing data
                    boolean canReformat = true;

                    // Set the base config to the default template so new options will
                    // be forced into the config
                    ClaimChunkWorldProfile profile =
                            defaultProfile == null ? getDefaultProfile() : defaultProfile;
                    if (defaultProfile != null) {
                        Utils.debug("Using converted world profile for world \"%s\"", worldName);
                    }
                    profile.toCCConfig(cfg.config());

                    // Make sure the file exists duh (unless a default profile is used instead!)
                    if (defaultProfile == null && file.exists()) {
                        // Try to parse the config
                        if (cfg.load(
                                (input, ncgf) -> {
                                    List<CCConfigParseError> errors = parser.parse(ncgf, input);
                                    for (CCConfigParseError error : errors) {
                                        Utils.err("Error parsing file \"%s\"", file.getPath());
                                        Utils.err("Description: %s", error);
                                    }
                                })) {
                            Utils.debug("Loaded world config file \"%s\"", file.getPath());
                        } else {
                            canReformat = false;
                            Utils.err("Failed to load world config file \"%s\"", file.getPath());
                        }
                    }

                    // Create the new world profile and override defaults with the
                    // loaded values
                    profile.fromCCConfig(cfg.config());

                    // Save/reformat config files
                    if (canReformat) {
                        saveConfig(cfg);
                    }
                    return profile;
                });
    }

    public void removeProfile(String worldName) {
        profiles.remove(worldName);
    }

    /**
     * Gets the world profile for the given world name or an empty one if the profile hasn't been
     * accessed yet. It should be noted that even if this isn't a valid world, this method *will*
     * return a world profile for it (likely generating a new one).
     *
     * @param worldName The name of the world for which to retrieve the config profile.
     * @return A non-null profile with the protection config options for a given world.
     */
    public @NotNull ClaimChunkWorldProfile getProfile(@NotNull String worldName) {
        return getProfile(worldName, null);
    }

    private void saveConfig(CCConfigHandler<CCConfig> cfg) {
        // Save the config to make sure that any new options will be loaded in
        boolean existed = cfg.file().exists();
        if (cfg.save(writer::serialize)) {
            Utils.debug(
                    "%s world config file \"%s\"",
                    (existed ? "Updated" : "Created"), cfg.file().getPath());
        } else {
            Utils.err("Failed to save world config file at \"%s\"", cfg.file().getPath());
        }
    }

    /*
        Normally, I'd add a save method, but these world profiles should be
        immutable. There are *certain* things that can be modified, such as
        the entities or blocks lists, but those changes shouldn't be saved as
        there shouldn't be any persistent changes made at runtime to prevent
        unexpected behavior when, for example, uninstalling an addon that
        modified the list and having to manually remove or re-add entries to the
        profile file.
    */

    public void unloadAllProfiles() {
        // Clearing all the worlds will require them to be loaded again
        profiles.clear();
    }

    /**
     * Gets the standard protection config, used as the default config for initializing the world
     * profile files.
     *
     * @return The default world profile.
     */
    public static @NotNull ClaimChunkWorldProfile getDefaultProfile() {
        // Initialize the profile access components
        final Accesses claimedChunks =
                new Accesses(new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());
        final Accesses unclaimedChunks =
                new Accesses(new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());

        // Assign entity defaults
        claimedChunks.entityAccesses.put(EntityType.UNKNOWN, new EntityAccess(false, false, false));
        claimedChunks.entityAccessClassMapping.put(
                "MONSTERS", new EntityAccess(false, false, false));
        claimedChunks.entityAccessClassMapping.put(
                "VEHICLES", new EntityAccess(false, false, false));
        unclaimedChunks.entityAccesses.put(EntityType.UNKNOWN, new EntityAccess(true, true, true));

        // Assign block defaults
        claimedChunks.blockAccesses.put(Material.AIR, new BlockAccess(false, false, false, false));
        claimedChunks.blockAccessClassMapping.put(
                "REDSTONE", new BlockAccess(false, false, false, false));
        claimedChunks.blockAccessClassMapping.put(
                "DOOR", new BlockAccess(false, false, false, false));
        claimedChunks.blockAccessClassMapping.put(
                "CONTAINER", new BlockAccess(false, false, false, false));
        unclaimedChunks.blockAccesses.put(Material.AIR, new BlockAccess(true, true, true, true));

        // Create the profile
        ClaimChunkWorldProfile defaultProfile =
                new ClaimChunkWorldProfile(true, claimedChunks, unclaimedChunks);

        // Add default entity classes
        defaultProfile.entityClasses.putAll(getDefaultEntityAccessClasses());

        // Add default block classes
        defaultProfile.blockClasses.putAll(getDefaultBlockAccessClasses());

        return defaultProfile;
    }

    /**
     * Get the map of entity classes provided by default.
     *
     * @return A HashMap of entity type sets keyed by name.
     */
    public static @NotNull HashMap<String, HashSet<EntityType>> getDefaultEntityAccessClasses() {
        HashMap<String, HashSet<EntityType>> entityAccessMapping = new HashMap<>();

        // Add the ne'er-do-wells
        HashSet<EntityType> monsters = new HashSet<>();
        Arrays.stream(EntityType.values())
                .filter(Objects::nonNull)
                .filter(
                        entityType ->
                                entityType.getEntityClass() != null
                                        && Monster.class.isAssignableFrom(
                                                entityType.getEntityClass()))
                .forEach(monsters::add);

        // Add the hanging entities (item frames, leads, paintings)
        HashSet<EntityType> hangingEntities = new HashSet<>();
        Arrays.stream(EntityType.values())
                .filter(Objects::nonNull)
                .filter(
                        entityType ->
                                entityType.getEntityClass() != null
                                        && Hanging.class.isAssignableFrom(
                                                entityType.getEntityClass()))
                .forEach(hangingEntities::add);

        // Add all animals
        HashSet<EntityType> animals = new HashSet<>();
        Arrays.stream(EntityType.values())
                .filter(
                        entityType ->
                                entityType.getEntityClass() != null
                                        && Animals.class.isAssignableFrom(
                                                entityType.getEntityClass()))
                .forEach(animals::add);

        // Add mine-carts and boats
        HashSet<EntityType> vehicles = new HashSet<>();
        Arrays.stream(EntityType.values())
                .filter(
                        entityType ->
                                entityType.getEntityClass() != null
                                        && (Minecart.class.isAssignableFrom(
                                                        entityType.getEntityClass())
                                                || Boat.class.isAssignableFrom(
                                                        entityType.getEntityClass())))
                .forEach(vehicles::add);

        entityAccessMapping.put("MONSTERS", monsters);
        entityAccessMapping.put("HANGING_ENTITIES", hangingEntities);
        entityAccessMapping.put("ANIMALS", animals);
        entityAccessMapping.put("VEHICLES", vehicles);

        return entityAccessMapping;
    }

    /**
     * Get the map of block classes provided by default.
     *
     * @return A HashMap of block material sets keyed by name.
     */
    public static @NotNull HashMap<String, HashSet<Material>> getDefaultBlockAccessClasses() {
        HashMap<String, HashSet<Material>> blockAccessMapping = new HashMap<>();

        // Add redstone blocks
        HashSet<Material> redstone = new HashSet<>(List.of(Material.LEVER));
        // Add pressure plates and buttons
        Arrays.stream(Material.values())
                .filter(
                        mat ->
                                mat.name().endsWith("_PRESSURE_PLATE")
                                        || mat.name().endsWith("_BUTTON"))
                .forEach(redstone::add);
        blockAccessMapping.put("REDSTONE", redstone);

        // Add door blocks
        HashSet<Material> doors =
                Arrays.stream(Material.values())
                        .filter(
                                mat ->
                                        mat.name().endsWith("_DOOR")
                                                || mat.name().endsWith("_TRAPDOOR"))
                        .collect(Collectors.toCollection(HashSet::new));
        blockAccessMapping.put("DOOR", doors);

        // Add sign blocks
        HashSet<Material> signs =
                Arrays.stream(Material.values())
                        .filter(mat -> mat.name().endsWith("_SIGN"))
                        .collect(Collectors.toCollection(HashSet::new));
        blockAccessMapping.put("SIGN", signs);

        // Add container blocks  Annoyingly, there seems to be no way to generate a list of
        // materials that are containers so we have to write the list ourselves
        HashSet<Material> containers =
                new HashSet<>(
                        List.of(
                                Material.BARREL,
                                Material.BLAST_FURNACE,
                                Material.BREWING_STAND,
                                Material.CHEST,
                                Material.CHEST_MINECART,
                                Material.TRAPPED_CHEST,
                                Material.DISPENSER,
                                Material.DROPPER,
                                Material.FURNACE,
                                Material.FURNACE_MINECART,
                                Material.HOPPER,
                                Material.HOPPER_MINECART,
                                Material.SMOKER));
        containers.addAll(
                Arrays.stream(Material.values())
                        .filter(mat -> mat.name().endsWith("SHULKER_BOX"))
                        .toList());
        blockAccessMapping.put("CONTAINER", containers);

        return blockAccessMapping;
    }
}
