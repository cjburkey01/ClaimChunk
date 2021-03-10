package com.cjburkey.claimchunk.config;

import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.config.access.Access;
import com.cjburkey.claimchunk.config.access.BlockAccess;
import com.cjburkey.claimchunk.config.access.EntityAccess;
import com.cjburkey.claimchunk.config.ccconfig.*;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import javax.annotation.Nonnull;
import java.io.File;
import java.util.HashMap;
import java.util.List;

// TODO: ADD TONS OF COMMENTS THIS IS SO RIDICULOUS!
public class ClaimChunkWorldProfileManager {

    // Default profile is initialized when it is needed first
    private ClaimChunkWorldProfile defaultProfile = null;

    // Config management
    private final File worldConfigDir;
    private final HashMap<String, ClaimChunkWorldProfile> profiles;

    private final CCConfigParser parser;
    private final CCConfigWriter writer;

    public ClaimChunkWorldProfileManager(@Nonnull File worldConfigDir,
                                         @Nonnull CCConfigParser parser,
                                         @Nonnull CCConfigWriter writer) {
        this.worldConfigDir = worldConfigDir;
        profiles = new HashMap<>();

        this.parser = parser;
        this.writer = writer;
    }

    public @Nonnull ClaimChunkWorldProfile getProfile(String worldName) {
        // Try to get the config from the ones already loaded
        return profiles.computeIfAbsent(worldName, n -> {
            File file = new File(worldConfigDir, n + ".txt");

            // Create a config handle for this file
            CCConfigHandler<CCConfig> cfg = new CCConfigHandler<>(
                    file,
                    new CCConfig(String.format(ClaimChunkWorldProfile.headerComment, worldName), "")
            );

            // Set the base config to the default template so new options will
            // be forced into the config
            cfg.config().union(getDefaultProfile().toCCConfig(n));

            if (file.exists()) {
                // Try to parse the config
                if (cfg.load((input, ncgf) -> {
                    List<CCConfigParseError> errors = parser.parse(ncgf, input);
                    for (CCConfigParseError error : errors) {
                        Utils.err("Error parsing file \"%s\"", file.getPath());
                        Utils.err("Description: %s", error);
                    }
                })) {
                    Utils.debug("Loaded world config file \"%s\"", file.getPath());


                } else {
                    Utils.err("Failed to load world config file \"%s\"", file.getPath());
                }
            }

            // Create the new world profile and override defaults with the
            // loaded values
            ClaimChunkWorldProfile profile = new ClaimChunkWorldProfile(false,
                                                                        null,
                                                                        null);
            profile.fromCCConfig(cfg.config());

            // Save the config to make sure that any new options will be loaded in
            boolean existed = file.exists();
            if (cfg.save(writer::serialize)) {
                Utils.debug((existed ? "Updated" : "Created") + " world config file \"%s\"", file.getPath());
            } else {
                Utils.err("Failed to save world config file at \"%s\"", file.getPath());
            }
            return profile;
        });
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
        profiles.clear();
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
            final Access claimedChunks = new Access(new HashMap<>(), new HashMap<>());
            final Access unclaimedChunks = new Access(new HashMap<>(), new HashMap<>());

            // Assign entity defaults
            claimedChunks.entityAccesses.put(EntityType.UNKNOWN,
                    new EntityAccess(false, false, false));
            unclaimedChunks.entityAccesses.put(EntityType.UNKNOWN,
                    new EntityAccess(true, true, true));

            // Assign block defaults
            claimedChunks.blockAccesses.put(Material.AIR,
                    new BlockAccess(false, false, false, false));
            unclaimedChunks.blockAccesses.put(Material.AIR,
                    new BlockAccess(true, true, true, true));

            // Create the profile
            defaultProfile = new ClaimChunkWorldProfile(true, claimedChunks, unclaimedChunks);
        }
        return defaultProfile;
    }

}
