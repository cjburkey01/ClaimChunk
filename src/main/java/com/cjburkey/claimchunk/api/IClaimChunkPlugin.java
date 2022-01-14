package com.cjburkey.claimchunk.api;

import com.cjburkey.claimchunk.ClaimChunkConfig;
import com.cjburkey.claimchunk.config.ClaimChunkWorldProfileManager;
import com.cjburkey.claimchunk.impl.PrereqsInitLayer;
import com.cjburkey.claimchunk.update.SemVer;

import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public interface IClaimChunkPlugin {

    /**
     * Get an instance of the Bukkit server class.
     *
     * @return Get the server
     */
    Server getServer();

    /**
     * The version for ClaimChunk.
     *
     * @return The version for this installation of ClaimChunk.
     */
    SemVer getVersion();

    /**
     * The latest release of ClaimChunk online.
     *
     * @return The latest GitHub release for ClaimChunk.
     */
    SemVer getAvailableVersion();

    /**
     * Get an instance of the layer responsible for storing information regarding
     *
     * @return A non-null instance of the PrereqChecker for chunk claiming.
     */
    PrereqsInitLayer getPrereqLayer();

    /**
     * Get the instance of the ClaimChunk world profile handler.
     *
     * @return The instance for this given instance of ClaimChunk.
     */
    ClaimChunkWorldProfileManager getProfileManager();

    /**
     * Get the location for this plugins data. This should be {@code ./plugins/ClaimChunk}.
     *
     * @return The direction for this plugin's files.
     */
    @NotNull
    File getDataFolder();

    /**
     * Get the ClaimChunk config instance.
     *
     * @return The loaded config file for ClaimChunk
     */
    ClaimChunkConfig chConfig();

    /**
     * Get the raw {@code config.yml} YML handler for ClaimChunk.
     *
     * @return The FileConfiguration for ClaimChunk's config file.
     */
    @NotNull
    FileConfiguration getConfig();

    /** Save the modified FileConfiguration for ClaimChunk. */
    void saveConfig();

    /** Disable the ClaimChunk plugin. */
    void disable();
}
