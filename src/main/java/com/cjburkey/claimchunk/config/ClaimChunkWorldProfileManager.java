package com.cjburkey.claimchunk.config;

import com.cjburkey.claimchunk.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class ClaimChunkWorldProfileManager {

    // Default profile is initialized when it is needed first
    private ClaimChunkWorldProfile defaultProfile = null;

    // Config manager
    private final MultiJsonConfigWrapper<ClaimChunkWorldProfile> config;

    public ClaimChunkWorldProfileManager(File worldConfigDir) {
        this.config = new MultiJsonConfigWrapper<>(ClaimChunkWorldProfile[].class, worldConfigDir);
    }

    @SuppressWarnings("unused")
    public @Nullable ClaimChunkWorldProfile getProfile(String worldName) {
        try {
            // Get an instance of the profile for the given world. If it
            // doesn't exist, it will be created and saved into a file by the
            // `config.get()` method. This method does not reload the file if
            // it was already loaded; the profile can be reloaded by reloading
            // all profiles with the `reloadAllProfiles()` method in this class
            return config.get(worldName, name -> getDefaultProfile().copyForWorld(name), false);
        } catch (IOException e) {
            Utils.err("Failed to get world profile for world \"%s\"", worldName);
        }
        return null;
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
        config.lazyReloadAll();
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
            defaultProfile = new ClaimChunkWorldProfile(null,
                                                        true,
                                                        new HashMap<>(),
                                                        new HashMap<>());
        }
        return defaultProfile;
    }

}
