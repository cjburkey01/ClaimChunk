package com.cjburkey.claimchunk.config;

import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.config.ccconfig.CCConfig;
import com.cjburkey.claimchunk.config.ccconfig.CCConfigHandler;
import com.cjburkey.claimchunk.config.ccconfig.CCConfigParseError;
import com.cjburkey.claimchunk.config.ccconfig.CCConfigParser;
import com.cjburkey.claimchunk.config.ccconfig.CCConfigWriter;
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
    private final HashMap<String, CCConfigHandler<CCConfig>> configs;

    public ClaimChunkWorldProfileManager(File worldConfigDir) {
        this.worldConfigDir = worldConfigDir;
        configs = new HashMap<>();
    }

    private @Nonnull CCConfigHandler<CCConfig> getWorldFile(String worldName) {
        // Try to get the config from the ones already loaded
        CCConfigHandler<CCConfig> config = configs.computeIfAbsent(worldName, n -> {
                    CCConfigHandler<CCConfig> newConfig
                            = new CCConfigHandler<>(new File(worldConfigDir, worldName + ".txt"),
                                    getDefaultProfile().toCCConfig());
                
                    // Save the new config
                    newConfig.save(cfg -> {
                        return new CCConfigWriter().serialize();
                    });
                    
                    // Save the config in the places it needs to be
                    configs.put(worldName, newConfig);
                    
                    return newConfig;
                });

        // Try to load the file (which may have just been created if it
        // didn't already exist)
        {
            final CCConfigHandler<CCConfig> cfg = config;
            config.load(input -> {
                new CCConfigParser().parse(cfg.config(), input/*, true*/);
                return cfg.config();
            });
        }
        
        return config;
    }

    public @Nonnull ClaimChunkWorldProfile getProfile(String worldName) {
        ClaimChunkWorldProfile profile = new ClaimChunkWorldProfile(false, null, null);
        profile.fromCCConfig(getWorldFile(worldName).config());
        return profile;
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
            // Initialize the profile access components
            final ClaimChunkWorldProfile.Access claimedChunks
                    = new ClaimChunkWorldProfile.Access(new HashMap<>(), new HashMap<>());
            final ClaimChunkWorldProfile.Access unclaimedChunks 
                    = new ClaimChunkWorldProfile.Access(new HashMap<>(), new HashMap<>());
            
            // Assign entity defaults
            claimedChunks.entityAccesses.put(EntityType.UNKNOWN,
                    new ClaimChunkWorldProfile.EntityAccess(false, false, false));
            unclaimedChunks.entityAccesses.put(EntityType.UNKNOWN, 
                    new ClaimChunkWorldProfile.EntityAccess(true, true, true));
            
            // Assign block defaults
            claimedChunks.blockAccesses.put(Material.AIR, 
                    new ClaimChunkWorldProfile.BlockAccess(false, false, false, false));
            unclaimedChunks.blockAccesses.put(Material.AIR, 
                    new ClaimChunkWorldProfile.BlockAccess(true, true, true, true));
            
            // Create the profile
            defaultProfile = new ClaimChunkWorldProfile(true, claimedChunks, unclaimedChunks);
        }
        return defaultProfile;
    }

}
