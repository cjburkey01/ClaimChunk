package com.cjburkey.claimchunk.transition;

import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.api.IClaimChunkPlugin;
import com.cjburkey.claimchunk.config.ClaimChunkWorldProfile;
import com.cjburkey.claimchunk.config.ClaimChunkWorldProfileHandler;
import com.cjburkey.claimchunk.config.access.BlockAccess;
import com.cjburkey.claimchunk.config.access.EntityAccess;

import lombok.Getter;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/** If you're looking here, it's not the right place. */
public class FromPre0023 {

    @Getter private final IClaimChunkPlugin claimChunk;
    private @Nullable HashMap<String, ClaimChunkWorldProfile> convertedConfigProfiles = null;

    public FromPre0023(IClaimChunkPlugin claimChunk) {
        this.claimChunk = claimChunk;

        // Create a default profile for the "world" and copy it whenever a new world is referenced
        // in the config. Note: If there is no world with the name "world", we should remove it
        // after obviously.
        HashMap<String, ClaimChunkWorldProfile> convertedProfiles = new HashMap<>();
        convertedProfiles.put("world", ClaimChunkWorldProfileHandler.getDefaultProfile());

        // I don't like doing this often, but oh well.
        boolean needsBackup = false;

        FileConfiguration config = claimChunk.getConfig();

        if (config.contains("protection.blockUnclaimedChunks")) {
            // Deny permissions in unclaimed chunks
            convertedProfiles.forEach(
                    (worldName, worldProfile) ->
                            worldProfile.unclaimedChunks.entityAccesses.put(
                                    EntityType.UNKNOWN, new EntityAccess(false, false, false)));
            convertedProfiles.forEach(
                    (worldName, worldProfile) ->
                            worldProfile.unclaimedChunks.blockAccesses.put(
                                    Material.AIR, new BlockAccess(false, false, false, false)));

            Utils.debug(
                    "Copied old `protection.blockUnclaimedChunks` setting from the config into"
                            + " world profiles");

            needsBackup = true;
        } else if (config.contains("protection.blockUnclaimedChunksInWorlds")) {
            // If `blockUnclaimedChunks` is false, set up worlds in this list to deny unclaimed
            // chunk interactions.
            @SuppressWarnings("unchecked")
            List<String> list =
                    (List<String>) config.getList("protection.blockUnclaimedChunksInWorlds");
            if (list != null) {
                for (String world : list) {
                    ClaimChunkWorldProfile profile =
                            convertedProfiles.computeIfAbsent(
                                    world,
                                    worldName ->
                                            new ClaimChunkWorldProfile(
                                                    Objects.requireNonNull(
                                                            convertedProfiles.get("world"))));

                    profile.unclaimedChunks.entityAccesses.put(
                            EntityType.UNKNOWN, new EntityAccess(false, false, false));
                    profile.unclaimedChunks.blockAccesses.put(
                            Material.AIR, new BlockAccess(false, false, false, false));
                }
            }

            Utils.debug(
                    "Copied old `protection.blockUnclaimedChunksInWorlds` setting from the config"
                            + " into world profiles");

            needsBackup = true;
        }
        if (config.contains("protection.blockPlayerChanges")
                && !config.getBoolean("protection.blockPlayerChanges")) {
            // If this is false, we DON'T need to stop players destroying/placing blocks in claimed
            // chunks in any worlds.
            convertedProfiles.forEach(
                    (world, profile) -> {
                        BlockAccess access = profile.claimedChunks.blockAccesses.get(Material.AIR);
                        if (access != null) {
                            access.allowBreak = true;
                            access.allowPlace = true;
                        }
                    });

            Utils.debug(
                    "Copied old `protection.blockPlayerChanges` setting from the config into world"
                            + " profiles");

            needsBackup = true;
        }
        if (config.contains("protection.blockInteractions")
                && !config.getBoolean("protection.blockInteractions")) {
            // If this is false, we DON'T need to prevent players interacting with blocks or
            // entities in claimed chunks.
            convertedProfiles.forEach(
                    (world, profile) -> {
                        EntityAccess entityAccess =
                                profile.claimedChunks.entityAccesses.get(EntityType.UNKNOWN);
                        if (entityAccess != null) {
                            entityAccess.allowInteract = true;
                        }
                        BlockAccess blockAccess =
                                profile.claimedChunks.blockAccesses.get(Material.AIR);
                        if (blockAccess != null) {
                            blockAccess.allowInteract = true;
                        }
                    });

            Utils.debug(
                    "Copied old `protection.blockInteractions` setting from the config into world"
                            + " profiles");

            needsBackup = true;
        }
        if (config.contains("protection.protectEntities")
                && !config.getBoolean("protection.protectEntities")) {
            // If this is false, entities DON'T need to be protected from other players in claimed
            // chunks.
            convertedProfiles.forEach(
                    (world, profile) -> {
                        EntityAccess entityAccess =
                                profile.claimedChunks.entityAccesses.get(EntityType.UNKNOWN);
                        if (entityAccess != null) {
                            entityAccess.allowDamage = true;
                        }
                    });

            Utils.debug(
                    "Copied old `protection.protectEntities` setting from the config into world"
                            + " profiles");

            needsBackup = true;
        }
        if (config.contains("protection.blockTnt") && !config.getBoolean("protection.blockTnt")) {
            // If this is false, disable explosion protection on entities and blocks
            convertedProfiles.forEach(
                    (world, profile) -> {
                        EntityAccess entityAccess =
                                profile.claimedChunks.entityAccesses.get(EntityType.UNKNOWN);
                        if (entityAccess != null) {
                            entityAccess.allowExplosion = true;
                        }
                        BlockAccess blockAccess =
                                profile.claimedChunks.blockAccesses.get(Material.AIR);
                        if (blockAccess != null) {
                            blockAccess.allowExplosion = true;
                        }
                    });

            Utils.debug(
                    "Copied old `protection.blockTnt` setting from the config into world profiles");

            needsBackup = true;
        }
        if (config.contains("protection.blockCreeper")
                || config.contains("protection.blockWither")) {
            // These both should be handled by explosion protection now!
            needsBackup = true;
        }
        if (config.contains("protection.blockFireSpread")
                && config.getBoolean("protection.blockFireSpread")) {
            // Block fire spread into claimed chunks in all worlds if this is true
            convertedProfiles.forEach(
                    (world, profile) -> {
                        profile.fireSpread.fromClaimedIntoDiffClaimed = false;
                        profile.fireSpread.fromUnclaimedIntoClaimed = false;
                    });

            Utils.debug(
                    "Copied old `protection.blockFireSpread` setting from the config into world"
                            + " profiles");

            needsBackup = true;
        }
        if (config.contains("protection.blockFluidSpreadIntoClaims")) {
            // If this is true, we need to enable fluid spread prevention from unclaimed chunks into
            // claimed ones.
            convertedProfiles.forEach(
                    (world, profile) -> {
                        profile.waterSpread.fromClaimedIntoDiffClaimed = false;
                        profile.waterSpread.fromUnclaimedIntoClaimed = false;
                    });

            Utils.debug(
                    "Copied old `protection.blockFluidSpreadIntoClaims` setting from the config"
                            + " into world profiles");

            needsBackup = true;
        }
        if (config.contains("protection.blockPistonsIntoClaims")) {
            // If this is true, we need to stop pistons extending from unclaimed chunks into claimed
            // chunks.
            convertedProfiles.forEach(
                    (world, profile) -> {
                        profile.pistonExtend.fromClaimedIntoDiffClaimed = false;
                        profile.pistonExtend.fromUnclaimedIntoClaimed = false;
                    });

            Utils.debug(
                    "Copied old `protection.blockPistonsIntoClaims` setting from the config into"
                            + " world profiles");

            needsBackup = true;
        }
        if (config.contains("protection.blockPvp")) {
            // TODO: If this is true, PvP needs to be disabled.
            // TODO: BEFORE THIS CAN HAPPEN, WE NEED TO GET PvP HANDLED SEPARATELY!!

            needsBackup = true;
        }
        if (config.contains("protection.blockedCmds")) {
            // Add blocked commands for each world
            convertedProfiles.forEach(
                    (world, profile) -> {
                        @SuppressWarnings("unchecked")
                        List<String> commands =
                                (List<String>) config.getList("protection.blockedCmds");
                        if (commands != null) {
                            profile.blockedCmdsInDiffClaimed.addAll(commands);
                        }
                    });

            Utils.debug(
                    "Copied old `protection.blockedCmds` setting from the config into world"
                            + " profiles");

            needsBackup = true;
        }
        if (config.getBoolean("protection.disableOfflineProtect")
                && config.contains("protection.disableOfflineProtect")) {
            // Set each world to deny protections to owned claimed chunks for offline players
            convertedProfiles.values().forEach(profile -> profile.protectOffline = false);
        }

        // Perform the backup if any old values are present.
        if (needsBackup) {
            backupConfigPost0_0_23();

            Utils.debug("Converted old config options to world profiles, removing from config.");

            // Unset all of the config values (if they're set)
            config.set("protection.blockUnclaimedChunks", null);
            config.set("protection.blockUnclaimedChunksInWorlds", null);
            config.set("protection.blockPlayerChanges", null);
            config.set("protection.blockInteractions", null);
            config.set("protection.blockTnt", null);
            config.set("protection.blockCreeper", null);
            config.set("protection.blockWither", null);
            config.set("protection.blockFireSpread", null);
            config.set("protection.blockFluidSpreadIntoClaims", null);
            config.set("protection.blockPistonsIntoClaims", null);
            config.set("protection.protectEntities", null);
            config.set("protection.blockPvp", null);
            config.set("protection.blockedCmds", null);
            config.set("protection.disableOfflineProtect", null);
            claimChunk.saveConfig();

            Utils.debug("Wrote the update config.");
            convertedConfigProfiles = convertedProfiles;
        }
    }

    public void saveConvertedProfiles() {
        if (convertedConfigProfiles != null) {
            // Debug
            Utils.debug("%s profiles to create", convertedConfigProfiles.size());

            // Load all the worlds to generate defaults
            // Note: If the config was just converted over, then those profiles will be used in
            // place of the defaults :)
            for (World world : claimChunk.getServer().getWorlds()) {
                // If we have converted profiles to load, check if this world is in them.
                ClaimChunkWorldProfile convertedProfile =
                        convertedConfigProfiles.get(world.getName());

                // If we don't have a converted file for this world, check if we have one for the
                // default "world"
                if (convertedProfile == null) {
                    Utils.debug("Loading world profile for world \"%s\"", world.getName());
                    convertedProfile =
                            new ClaimChunkWorldProfile(convertedConfigProfiles.get("world"));
                } else {
                    Utils.debug(
                            "Loading converted world profile for world \"%s\"", world.getName());
                }

                // The getProfile method makes a lookup to determine if this world has a profile. We
                // know it won't have a profile because we haven't added any to this handler yet. By
                // providing a default, the handler will save that default if the world profile
                // config file doesn't exist.
                claimChunk.getProfileHandler().getProfile(world.getName(), convertedProfile);
            }
        } else {
            // If we don't have any conversions to do, just load the profiles as the default and
            // create the files as necessary.
            claimChunk.getServer().getWorlds().stream()
                    .map(World::getName)
                    .forEach(claimChunk.getProfileHandler()::getProfile);
        }
    }

    private void backupConfigPost0_0_23() {
        File configFile = new File(claimChunk.getDataFolder(), "config.yml");
        if (configFile.exists()) {
            try {
                File backupConfig = new File(claimChunk.getDataFolder(), "config-pre-0.0.23.yml");
                if (!backupConfig.exists()) {
                    // Copy the config to a new file
                    Files.copy(
                            configFile.toPath(),
                            backupConfig.toPath(),
                            StandardCopyOption.COPY_ATTRIBUTES);
                } else {
                    Utils.log("Config already backed up.");
                }
            } catch (IOException e) {
                Utils.err("An error occurred while making a backup of the config file!");
                Utils.err("More information:");
                e.printStackTrace();
                Utils.err(
                        "Attempting to shut the server down because the plugin needs to"
                                + " convert the data to work (disabling the plugin would be"
                                + " even worse) and it's not safe to do so without a"
                                + " backup.");
                Utils.err(
                        "Note: you can also do this manually by removing all of the"
                                + " config values under the \"protections\" label except"
                                + " for \"disableOfflineProtect\"; you will, however, need"
                                + " to update the files within the"
                                + " \"plugins/ClaimChunk/worlds\" folder to match your"
                                + " desired configuration beyond the defaults.");
                claimChunk.disable();
                System.exit(0);
            }
        }
    }
}
