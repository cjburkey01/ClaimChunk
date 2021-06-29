package com.cjburkey.claimchunk;

import java.util.List;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public class ClaimChunkConfig {

    private final FileConfiguration config;

    /* Basic */

    @Getter
    private boolean checkForUpdates;
    @Getter
    private boolean disablePermissions;

    /* Colors */

    @Getter
    private ChatColor infoColor;

    /* Chunks */

    @Getter
    private boolean particlesWhenClaiming;
    @Getter
    private boolean allowChunkGive;
    @Getter
    private boolean hideAlertsForVanishedPlayers;
    @Getter
    private boolean displayNameOfOwner;
    @Getter
    private boolean defaultSendAlertsToOwner;
    @Getter
    private int unclaimCheckIntervalTicks;
    @Getter
    private int automaticUnclaimSeconds;
    @Getter
    private int maxPerListPage;
    @Getter
    private int defaultMaxChunksClaimed;

    /* Chunk Outlines */

    @Getter
    private String chunkOutlineParticle;
    @Getter
    private int chunkOutlineDurationSeconds;
    @Getter
    private int chunkOutlineSpawnPerSec;
    @Getter
    private int chunkOutlineParticlesPerSpawn;

    /* Data */

    @Getter
    private boolean keepJsonBackups;
    @Getter
    private int saveDataIntervalInMinutes;
    @Getter
    private int deleteOldBackupsAfterMinutes;
    @Getter
    private int minBackupIntervalInMinutes;

    /* Database */

    @Getter
    private boolean useDatabase;
    @Getter
    private boolean groupRequests;
    @Getter
    private boolean useSsl;
    @Getter
    private boolean allowPublicKeyRetrieval;
    @Getter
    private boolean convertOldData;
    @Getter
    private boolean printDatabaseDebug;
    @Getter
    private int databasePort;
    @Getter
    private String databaseName;
    @Getter
    private String databaseHostname;
    @Getter
    private String databaseUsername;
    @Getter
    private String databasePassword;

    /* Economy */

    @Getter
    private boolean useEconomy;
    @Getter
    private int firstFreeChunks;
    @Getter
    private double claimPrice;
    @Getter
    private double unclaimReward;

    /* Log */

    @Getter
    private boolean anonymousMetrics;
    @Getter
    private boolean debugSpam;
    @Getter
    private boolean debug;

    /* Titles */

    @Getter
    private boolean useTitlesInsteadOfChat;
    @Getter
    private boolean useActionBar;
    @Getter
    private int titleFadeInTime;
    @Getter
    private int titleStayTime;
    @Getter
    private int titleFadeOutTime;

    /* WorldGuard */

    @Getter
    private boolean allowWGAdminOverride;
    @Getter
    private boolean allowClaimsInWGRegionsByDefault;
    @Getter
    private boolean allowClaimingInNonWGWorlds;

    public ClaimChunkConfig(FileConfiguration configFile) {
        config = configFile;

        // Load the config values
        reload();
    }

    public void reload() {
        checkForUpdates = getBool("basic", "checkForUpdates");
        disablePermissions = getBool("basic", "disablePermissions");

        try {
            infoColor = ChatColor.valueOf(getString("colors", "infoColor"));
        } catch (Exception e) {
            Utils.err("Invalid config `colors.infoColor`: \"%s\" is not a value of ChatColor",
                    getString("colors", "infoColor"));
            infoColor = ChatColor.YELLOW;
        }

        particlesWhenClaiming = getBool("chunks", "particlesWhenClaiming");
        allowChunkGive = getBool("chunks", "allowChunkGive");
        hideAlertsForVanishedPlayers = getBool("chunks", "hideAlertsForVanishedPlayers");
        displayNameOfOwner = getBool("chunks", "displayNameOfOwner");
        defaultSendAlertsToOwner = getBool("chunks", "defaultSendAlertsToOwner");
        unclaimCheckIntervalTicks = getInt("chunks", "unclaimCheckIntervalTicks");
        automaticUnclaimSeconds = getInt("chunks", "automaticUnclaimSeconds");
        maxPerListPage = getInt("chunks", "maxPerListPage");
        defaultMaxChunksClaimed = getInt("chunks", "maxChunksClaimed");

        chunkOutlineParticle = getString("chunkOutline", "name");
        chunkOutlineDurationSeconds = getInt("chunkOutline", "durationSeconds");
        chunkOutlineSpawnPerSec = getInt("chunkOutline", "spawnsPerSecond");
        chunkOutlineParticlesPerSpawn = getInt("chunkOutline", "particlesPerSpawn");

        keepJsonBackups = getBool("data", "keepJsonBackups");
        saveDataIntervalInMinutes = getInt("data", "saveDataIntervalInMinutes");
        deleteOldBackupsAfterMinutes = getInt("data", "deleteOldBackupsAfterMinutes");
        minBackupIntervalInMinutes = getInt("data", "minBackupIntervalInMinutes");

        useDatabase = getBool("database", "useDatabase");
        groupRequests = getBool("database", "groupRequests");
        useSsl = getBool("database", "useSsl");
        allowPublicKeyRetrieval = getBool("database", "allowPublicKeyRetrieval");
        convertOldData = getBool("database", "convertOldData");
        printDatabaseDebug = getBool("database", "printDebug");
        databasePort = getInt("database", "port");
        databaseName = getString("database", "database");
        databaseHostname = getString("database", "hostname");
        databaseUsername = getString("database", "username");
        databasePassword = getString("database", "password");

        useEconomy = getBool("economy", "useEconomy");
        firstFreeChunks = getInt("economy", "firstFreeChunks");
        claimPrice = getDouble("economy", "claimPrice");
        unclaimReward = getDouble("economy", "unclaimReward");

        anonymousMetrics = getBool("log", "anonymousMetrics");
        debugSpam = getBool("log", "debugSpam");
        debug = getBool("log", "debug");

        useTitlesInsteadOfChat = getBool("titles", "useTitlesInsteadOfChat");
        useActionBar = getBool("titles", "useActionBar");
        titleFadeInTime = getInt("titles", "titleFadeInTime");
        titleStayTime = getInt("titles", "titleStayTime");
        titleFadeOutTime = getInt("titles", "titleFadeOutTime");

        allowWGAdminOverride = getBool("worldguard", "allowAdminOverride");
        allowClaimsInWGRegionsByDefault = getBool("worldguard", "allowClaimsInRegionsByDefault");
        allowClaimingInNonWGWorlds = getBool("worldguard", "allowClaimingInNonGuardedWorlds");
    }

    public FileConfiguration getFileConfig() {
        return config;
    }

    private boolean getBool(String section, String name) {
        return config.getBoolean(full(section, name));
    }

    private int getInt(String section, String name) {
        return config.getInt(full(section, name));
    }

    private double getDouble(@SuppressWarnings("SameParameterValue") String section, String name) {
        return config.getDouble(full(section, name));
    }

    private String getString(String section, String name) {
        return config.getString(full(section, name));
    }

    @SuppressWarnings("unused")
    private List<String> getList(String section, String name) {
        return config.getStringList(full(section, name));
    }

    private static String full(String section, String name) {
        // Format the section and name into a single YAML location for the config option
        return String.format("%s.%s", section, name);
    }

}
