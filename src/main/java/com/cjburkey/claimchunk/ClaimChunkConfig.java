package com.cjburkey.claimchunk;

import lombok.Getter;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class ClaimChunkConfig {

    private final FileConfiguration config;

    /* Basic */

    @Getter private boolean checkForUpdates;
    @Getter private boolean disablePermissions;

    /* Colors */

    @Getter private ChatColor infoColor;

    /* Chunks */

    @Getter private boolean particlesWhenClaiming;
    @Getter private boolean hideAlertsForVanishedPlayers;
    @Getter private boolean displayNameOfOwner;
    @Getter private boolean defaultSendAlertsToOwner;
    @Getter private int unclaimCheckIntervalTicks;
    @Getter private int automaticUnclaimSeconds;
    @Getter private int maxPerListPage;
    @Getter private int defaultMaxChunksClaimed;
    @Getter private int nearChunkSearch;
    @Getter private int maxScanRange;

    /* Chunk Outlines */

    @Getter private String chunkOutlineParticle;
    @Getter private int chunkOutlineDurationSeconds;
    @Getter private int chunkOutlineSpawnPerSec;
    @Getter private int chunkOutlineParticlesPerSpawn;
    @Getter private int chunkOutlineHeightRadius;
    @Getter private boolean chunkOutlineUseNewEffect;

    /* Data */

    @Getter private int saveDataIntervalInMinutes;

    /* Database */

    @Getter private boolean useDatabase;
    @Getter private boolean groupRequests;
    @Getter private boolean useSsl;
    @Getter private boolean allowPublicKeyRetrieval;
    @Getter private boolean convertOldData;
    @Getter private boolean printDatabaseDebug;
    @Getter private int databasePort;
    @Getter private String databaseName;
    @Getter private String databaseHostname;
    @Getter private String databaseUsername;
    @Getter private String databasePassword;

    /* Economy */

    @Getter private boolean useEconomy;
    @Getter private int firstFreeChunks;
    @Getter private double claimPrice;
    @Getter private double unclaimReward;

    /* Log */

    @Getter private boolean anonymousMetrics;
    @Getter private boolean showExtraInfoOnAnonymousMetrics;
    @Getter private boolean debugSpam;

    /* GUI */
    @Getter private String guiMenuBackButtonItem;
    @Getter private String guiMainMenuCurrentChunkItem;
    @Getter private String guiMainMenuChunkMapItem;
    @Getter private String guiMainMenuPermFlagsItem;
    @Getter private boolean guiMapMenuAllowClaimOtherChunks;
    @Getter private String guiMapMenuUnclaimedItem;
    @Getter private String guiMapMenuSelfClaimedItem;
    @Getter private String guiMapMenuOtherClaimedItem;
    @Getter private String guiMapMenuCenterUnclaimedItem;
    @Getter private String guiMapMenuCenterSelfClaimedItem;
    @Getter private String guiMapMenuCenterOtherClaimedItem;
    @Getter private String guiPermSelectMenuItem;
    @Getter private String guiPermModifyAllowItem;
    @Getter private String guiPermModifyDenyItem;

    /* Titles */

    @Getter private boolean useTitlesInsteadOfChat;
    @Getter private boolean useActionBar;
    @Getter private int titleFadeInTime;
    @Getter private int titleStayTime;
    @Getter private int titleFadeOutTime;
    // Delay between "you have entered" or "you have exited" chunks to prevent chat spam, when
    // applicable.
    // This is in TICKS!
    @Getter private int chunkEnterExitSpamDelay;

    /* WorldGuard */

    @Getter private boolean allowWGAdminOverride;
    @Getter private boolean allowClaimsInWGRegionsByDefault;
    @Getter private boolean allowClaimingInNonWGWorlds;

    /* Floodclaim */

    @Getter private boolean floodClaimEnabled;
    @Getter private int floodClaimMaxIter;
    @Getter private int floodClaimMaxArea;

    public ClaimChunkConfig(FileConfiguration configFile) {
        config = configFile;
    }

    public void reload() {
        checkForUpdates = getBool("basic", "checkForUpdates");
        disablePermissions = getBool("basic", "disablePermissions");

        try {
            infoColor = ChatColor.valueOf(getString("colors", "infoColor"));
        } catch (Exception e) {
            Utils.err(
                    "Invalid config `colors.infoColor`: \"%s\" is not a value of ChatColor",
                    getString("colors", "infoColor"));
            infoColor = ChatColor.YELLOW;
        }

        particlesWhenClaiming = getBool("chunks", "particlesWhenClaiming");
        hideAlertsForVanishedPlayers = getBool("chunks", "hideAlertsForVanishedPlayers");
        displayNameOfOwner = getBool("chunks", "displayNameOfOwner");
        defaultSendAlertsToOwner = getBool("chunks", "defaultSendAlertsToOwner");
        unclaimCheckIntervalTicks = getInt("chunks", "unclaimCheckIntervalTicks");
        automaticUnclaimSeconds = getInt("chunks", "automaticUnclaimSeconds");
        maxPerListPage = getInt("chunks", "maxPerListPage");
        defaultMaxChunksClaimed = getInt("chunks", "maxChunksClaimed");
        nearChunkSearch = getInt("chunks", "nearChunkSearch");
        maxScanRange = getInt("chunks", "maxScanRange");

        chunkOutlineParticle = getString("chunkOutline", "name");
        chunkOutlineDurationSeconds = getInt("chunkOutline", "durationSeconds");
        chunkOutlineSpawnPerSec = getInt("chunkOutline", "spawnsPerSecond");
        chunkOutlineParticlesPerSpawn = getInt("chunkOutline", "particlesPerSpawn");
        chunkOutlineHeightRadius = getInt("chunkOutline", "heightRadius");
        chunkOutlineUseNewEffect = getBool("chunkOutline", "useNewEffect");

        saveDataIntervalInMinutes = getInt("data", "saveDataIntervalInMinutes");

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
        showExtraInfoOnAnonymousMetrics = getBool("log", "showExtraInfoOnAnonymousMetrics");
        debugSpam = getBool("log", "debugSpam");

        guiMenuBackButtonItem = getString("gui", "menuBackButtonItem");
        guiMainMenuCurrentChunkItem = getString("gui", "mainMenuCurrentChunkItem");
        guiMainMenuChunkMapItem = getString("gui", "mainMenuChunkMapItem");
        guiMainMenuPermFlagsItem = getString("gui", "mainMenuPermFlagsItem");
        guiMapMenuAllowClaimOtherChunks = getBool("gui", "mapMenuAllowClaimOtherChunks");
        guiMapMenuUnclaimedItem = getString("gui", "mapMenuUnclaimedItem");
        guiMapMenuSelfClaimedItem = getString("gui", "mapMenuSelfClaimedItem");
        guiMapMenuOtherClaimedItem = getString("gui", "mapMenuOtherClaimedItem");
        guiMapMenuCenterUnclaimedItem = getString("gui", "mapMenuCenterUnclaimedItem");
        guiMapMenuCenterSelfClaimedItem = getString("gui", "mapMenuCenterSelfClaimedItem");
        guiMapMenuCenterOtherClaimedItem = getString("gui", "mapMenuCenterOtherClaimedItem");
        guiPermSelectMenuItem = getString("gui", "permSelectMenuItem");
        guiPermModifyAllowItem = getString("gui", "permModifyAllowItem");
        guiPermModifyDenyItem = getString("gui", "permModifyDenyItem");

        useTitlesInsteadOfChat = getBool("titles", "useTitlesInsteadOfChat");
        useActionBar = getBool("titles", "useActionBar");
        titleFadeInTime = getInt("titles", "titleFadeInTime");
        titleStayTime = getInt("titles", "titleStayTime");
        titleFadeOutTime = getInt("titles", "titleFadeOutTime");
        chunkEnterExitSpamDelay = getInt("titles", "chunkEnterExitSpamDelay");

        allowWGAdminOverride = getBool("worldguard", "allowAdminOverride");
        allowClaimsInWGRegionsByDefault = getBool("worldguard", "allowClaimsInRegionsByDefault");
        allowClaimingInNonWGWorlds = getBool("worldguard", "allowClaimingInNonGuardedWorlds");

        floodClaimEnabled = getBool("floodclaim", "enabled");
        floodClaimMaxIter = getInt("floodclaim", "maximumIterations");
        floodClaimMaxArea = getInt("floodclaim", "maximumArea");
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
