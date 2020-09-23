package com.cjburkey.claimchunk.config;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import javax.annotation.Nonnull;
import java.io.File;
import java.util.HashMap;

public class ClaimChunkWorldProfileManager {

    // Default profile is initialized when it is needed first
    private ClaimChunkWorldProfile defaultProfile = null;

    // Config management
    private final File worldConfigDir;
    private final HashMap<String, TomlFileHandler<ClaimChunkWorldProfile>> configs;

    public ClaimChunkWorldProfileManager(File worldConfigDir) {
        this.worldConfigDir = worldConfigDir;
        configs = new HashMap<>();
    }

    private TomlFileHandler<ClaimChunkWorldProfile> getWorldFile(String worldName) {
        // Try to get the config from the ones already loaded
        TomlFileHandler<ClaimChunkWorldProfile> config = configs.get(worldName);
        if (config == null) {
            config = new TomlFileHandler<>(new File(worldConfigDir, worldName + ".toml"),
                                           ClaimChunkWorldProfile.class, this::getDefaultProfile);
            // Try to load the file (which may have just been created if it
            // didn't already exist)
            if (config.load().isPresent()) {
                configs.put(worldName, config);
            }
        }
        return config;
    }

    public @Nonnull ClaimChunkWorldProfile getProfile(String worldName) {
        return getWorldFile(worldName).readData();
    }

    /*
        Normally, I'd add a save method, but these world profiles should be
        immutable. There are *certain* things that can be modified, such as
        the entities or blocks lists, but those changes shouldn't be saved as
        there shouldn't be any persistent changes made at runtime to prevent
        unexpected behavior when, for example, uninstalling an addon that
        modified that list and have to manually remove or re-add entries to the
        profile file.
    */

    public void reloadAllProfiles() {
        // Clearing all the worlds will require them to be loaded again
        configs.clear();
    }

    // API method
    @SuppressWarnings("unused")
    public void setDefaultProfile(ClaimChunkWorldProfile profile) {
        defaultProfile = profile;
    }

    public @Nonnull ClaimChunkWorldProfile getDefaultProfile() {
        // Lazy initialization; if the default profile hasn't been built yet,
        // build one
        if (defaultProfile == null) {
            HashMap<EntityType, ClaimChunkWorldProfile.Access<ClaimChunkWorldProfile.EntityAccess>> entityAccesses = new HashMap<>();
            HashMap<Material, ClaimChunkWorldProfile.Access<ClaimChunkWorldProfile.BlockAccess>> blockAccesses = new HashMap<>();

            final ClaimChunkWorldProfile.EntityAccess defaultUnclaimedEntityAccess = new ClaimChunkWorldProfile.EntityAccess();
            final ClaimChunkWorldProfile.EntityAccess defaultClaimedEntityAccess = new ClaimChunkWorldProfile.EntityAccess();

            defaultUnclaimedEntityAccess.allowExplosion = true;
            defaultUnclaimedEntityAccess.allowDamage = true;
            defaultUnclaimedEntityAccess.allowInteract = true;

            defaultClaimedEntityAccess.allowExplosion = false;
            defaultClaimedEntityAccess.allowDamage = false;
            defaultClaimedEntityAccess.allowInteract = false;

            entityAccesses.put(EntityType.UNKNOWN,
                               new ClaimChunkWorldProfile.Access<>(defaultClaimedEntityAccess,
                                                                   defaultUnclaimedEntityAccess));

            blockAccesses.put(Material.AIR,
                              new ClaimChunkWorldProfile.Access<>(new ClaimChunkWorldProfile.BlockAccess(true, true, true, true),
                                                                  new ClaimChunkWorldProfile.BlockAccess(false, false, false, false)));

            final ClaimChunkWorldProfile.BlockAccess defaultButtonClaimed = new ClaimChunkWorldProfile.BlockAccess(true, false, false, false);
            final ClaimChunkWorldProfile.BlockAccess defaultButtonUnclaimed = new ClaimChunkWorldProfile.BlockAccess(true, true, true, true);

            blockAccesses.put(Material.BIRCH_BUTTON, new ClaimChunkWorldProfile.Access<>(defaultButtonClaimed.copy(), defaultButtonUnclaimed.copy()));
            blockAccesses.put(Material.ACACIA_BUTTON, new ClaimChunkWorldProfile.Access<>(defaultButtonClaimed.copy(), defaultButtonUnclaimed.copy()));
            blockAccesses.put(Material.DARK_OAK_BUTTON, new ClaimChunkWorldProfile.Access<>(defaultButtonClaimed.copy(), defaultButtonUnclaimed.copy()));
            blockAccesses.put(Material.JUNGLE_BUTTON, new ClaimChunkWorldProfile.Access<>(defaultButtonClaimed.copy(), defaultButtonUnclaimed.copy()));
            blockAccesses.put(Material.OAK_BUTTON, new ClaimChunkWorldProfile.Access<>(defaultButtonClaimed.copy(), defaultButtonUnclaimed.copy()));
            blockAccesses.put(Material.SPRUCE_BUTTON, new ClaimChunkWorldProfile.Access<>(defaultButtonClaimed.copy(), defaultButtonUnclaimed.copy()));
            blockAccesses.put(Material.STONE_BUTTON, new ClaimChunkWorldProfile.Access<>(defaultButtonClaimed.copy(), defaultButtonUnclaimed.copy()));

            defaultProfile = new ClaimChunkWorldProfile(true, entityAccesses, blockAccesses);
        }
        return defaultProfile;
    }

}
