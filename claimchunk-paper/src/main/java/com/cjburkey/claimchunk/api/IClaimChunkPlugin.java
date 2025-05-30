package com.cjburkey.claimchunk.api;

import com.cjburkey.claimchunk.ClaimChunkConfig;
import com.cjburkey.claimchunk.chunk.ChunkHandler;
import com.cjburkey.claimchunk.config.ClaimChunkWorldProfileHandler;
import com.cjburkey.claimchunk.i18n.V2JsonMessages;
import com.cjburkey.claimchunk.layer.PrereqsInitLayer;
import com.cjburkey.claimchunk.player.AdminOverride;
import com.cjburkey.claimchunk.player.PlayerHandler;
import com.cjburkey.claimchunk.rank.RankHandler;
import com.cjburkey.claimchunk.update.SemVer;

import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public interface IClaimChunkPlugin {

    /* Info */

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

    /* Initialization layers */

    /**
     * Get an instance of the layer responsible for handling the prereq system
     *
     * @return A non-null instance of the PrereqChecker for chunk claiming.
     */
    PrereqsInitLayer getPrereqHandlerLayer();

    /* Handlers */

    /**
     * Get the ClaimChunk config handler.
     *
     * @return The loaded config file for ClaimChunk
     */
    ClaimChunkConfig getConfigHandler();

    /**
     * Get the translations to use for messages.
     *
     * @return The messages handler.
     */
    V2JsonMessages getMessages();

    /**
     * Get the instance of the ClaimChunk world profile handler.
     *
     * @return The instance for this given instance of ClaimChunk.
     */
    ClaimChunkWorldProfileHandler getProfileHandler();

    /**
     * Get the chunk handler for this instance.
     *
     * @return The ClaimChunk chunk handler.
     */
    ChunkHandler getChunkHandler();

    /**
     * Get the instance of the player information handler.
     *
     * @return The ClaimChunk player handler.
     */
    PlayerHandler getPlayerHandler();

    /**
     * Get the instance of the rank handler.
     *
     * @return The rank handler.
     */
    RankHandler getRankHandler();

    /**
     * Get the instance of the handler for admin override info.
     *
     * @return The list of players with admin override enabled.
     */
    AdminOverride getAdminOverrideHandler();

    /* Management */

    /** Disable the ClaimChunk plugin. */
    void disable();

    /* Spigot API */

    /**
     * Get the plugin descriptor file information.
     *
     * @return Information regarding the plugin.
     */
    PluginDescriptionFile getDescription();

    /**
     * Get the raw {@code config.yml} YML handler for ClaimChunk.
     *
     * @return The FileConfiguration for ClaimChunk's config file.
     */
    @NotNull FileConfiguration getConfig();

    /**
     * Get the location for this plugins data. This should be {@code ./plugins/ClaimChunk}.
     *
     * @return The direction for this plugin's files.
     */
    @NotNull File getDataFolder();

    /** Save the modified FileConfiguration for ClaimChunk. */
    void saveConfig();
}
