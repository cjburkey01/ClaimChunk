package com.cjburkey.claimchunk.i18n;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.config.access.BlockAccess;
import com.cjburkey.claimchunk.config.access.EntityAccess;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;

import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

/** This class will soon be deprecated in favor of Goldmensch's JALL library :) */
public final class V2JsonMessages {

    // Global localization
    public String errEnterValidNum = "&cPlease enter a valid number";
    public String noPlayer = "&cThat player has not joined the server before";

    // CMD localization
    public String ingameOnly = "Only in-game players may use that subcommand.";
    public String consoleOnly = "&cOnly the console may use that subcommand.";
    public String invalidCommand = "&cInvalid command. See: &6/%%CMD%% help&c.";
    public String errorDisplayUsage = "&cUsage: &6/%%CMD%% %%SUB_CMD%% %%ARGS%%&c.";
    public String commandNoPermission =
            "&cYou do not have permission to use that ClaimChunk command.";

    // Claim localization
    public String claimNoPerm = "&cYou do not have permission to claim chunks";
    public String claimWorldDisabled = "&cClaimChunk is not enabled in this world";
    public String claimLocationBlock = "&cYou cannot claim chunks here";
    public String claimAlreadyOwned = "&cThis chunk is already claimed";
    public String claimTooMany = "&cYou own the maximum number of chunks";
    public String claimSuccess = "&aChunk claimed for %%PRICE%%!";
    public String claimNoCost = "Free";
    public String claimFree1 = "&aFirst chunk is free!";
    public String claimFrees = "&aFirst %%COUNT%% chunks are free!";
    public String claimNotEnoughMoney = "&cYou do not have enough money";
    public String nearChunkSearch = "&cYou are too close to another chunk";

    // Give localization
    public String giveNoPerm = "&cYou do not have permission to give chunks";
    public String giveNotYourChunk = "&cYou do not own this chunk";
    public String giveNoPlayer = "&c%%PLAYER%% was not found, they may be offline";
    public String giveNotYourself = "&cYou already own this chunk";
    public String giveChunksFull = "&c%%PLAYER can't own any more chunks";
    public String giveError = "&cUnable to claim chunk for new owner";
    public String gaveChunk = "&aChunk given to %%PLAYER%%";
    public String givenChunk = "&aChunk received from %%PLAYER%%";

    // Unclaim localization
    public String unclaimNoPerm = "&cYou do not have permission to unclaim chunks";
    public String unclaimNotOwned = "&cThis chunk is not owned";
    public String unclaimNotOwner = "&cYou do not own this chunk";
    public String unclaimSuccess = "&aChunk unclaimed!";
    public String unclaimRefund = "&aYou have been refunded %%AMT%%";
    public String unclaimAll = "&aUnclaimed %%CHUNKS%% chunks";

    // Admin unclaim localization
    public String adminUnclaimAll = "&aAdmin unclaimed %%CHUNKS%% chunks";

    // Access localization
    public String accessNoPerm =
            "&cYou do not have permission to change or view chunks access info";
    public String accessHas = "&a%%PLAYER%% now has access to your chunk";
    public String accessHasMultiple = "&aThe provided players'' now have access to your chunk";
    public String accessOneself = "&cYou already have access to your own chunk";

    // CheckAccess localization
    public String checkAccessPlayerNoAccess = "%%PLAYER%% has no permissions on this chunk";
    public String checkAccessPlayerHasAccess =
            "%%PLAYER%% permissions on this chunk: break: %%break%%, place: %%place%%, doors:"
                    + " %%doors%%, redstone: %%redstone%%, interactVehicles: %%interactVehicles%%,"
                    + " interactEntities: %%interactEntities%%, interactBlocks: %%interactBlocks%%,"
                    + " useContainers: %%useContainers%%";
    public String checkAccessNoPlayersHaveAccess =
            "No other players have permissions on this chunk";
    public String checkAccessPlayerIsOwner = "%%PLAYER%% owns this chunk";

    // RevokeAccess localization
    public String revokeAccessAllChunks =
            "The specified players no longer have permissions on your chunks";
    public String revokeAccessCurrentChunk =
            "The specified players no longer have permissions in this chunk";

    // TNT localization
    public String tntNoPerm = "&cYou do not have permission to toggle TNT in this chunk";
    public String tntEnabled = "&aTNT has been enabled in this chunk";
    public String tntDisabled = "&aTNT has been disabled in this chunk";

    // Name localization
    public String nameClear = "&aYour name has been cleared";
    public String nameNotSet = "&cYou do not have a custom name set";
    public String nameSet = "&aYour name has been set: %%NAME%%";

    // Auto localization
    public String autoEnabled = "&aAutomatic claiming enabled";
    public String autoDisabled = "&aAutomatic claiming disabled";

    // Reload localization
    public String reloadComplete = "&aReload complete";

    // Alert localization
    public String playerEnterChunk = "&6%%PLAYER%% has entered your claimed chunk";
    public String enabledAlerts = "&aEnabled alerts";
    public String disabledAlerts = "&aDisabled alerts";

    // Scan localization
    public String scanInputTooBig = "&6Scan area too large, max area is %%MAXAREA%%";
    public String claimsFound = "&6%%NEARBY%% chunks found within a %%RADIUS%% chunk radius";

    // Help localization
    public String helpHeader = "&6--- [ &lClaimChunk Help&r&6 ] ---";
    public String helpCmdHeader = "&6--- [ &e/%%USED%% %%CMD%% &l&6Help ] ---";

    public String helpCmdNotFound = "&cCommand &e/%%USED%% %%CMD%% &cnot found.";

    public String helpCmd = "&e/%%USED%% %%CMD%% %%ARGS%%\n  &6%%DESC%%";

    // Info localization
    public String infoTitle = "Chunk Information";
    public String infoHeader = "%s&l--- [ %s ] ---";
    public String infoPosition = "Chunk position: &l%%X%%, %%Z%% in %%WORLD%%";
    public String infoOwnerUnknown = "&7Unknown";
    public String infoOwner = "Chunk owner: &l%%PLAYER%%";
    public String infoNameNone = "&7None";
    public String infoName = "Chunk name: &l%%NAME%%";

    // List localization
    public String claimsTitle = "Claims for %%NAME%% in %%WORLD%%";
    public String claimsChunk = "%%X%%, %%Z%%";
    public String claimsPagination = "Page %%PAGE%% of %%MAXPAGE%%";

    // Movement localization
    public String chunkOwner = "&6Entering the territory of %%PLAYER%%";
    public String unknownChunkOwner = "&6Entering claimed territory";
    public String chunkSelf = "&6Entering your own territory";
    public String chunkLeave = "&6Exiting the territory of %%PLAYER%%";
    public String chunkLeaveUnknown = "&6Entering unclaimed territory";
    public String chunkLeaveSelf = "&6Exiting your territory";

    // Protection localization
    public String chunkCancelAdjacentPlace =
            "&cYou can't place &e%%BLOCK%%&c next to &e%%BLOCK%%&c in %%OWNER%%&c's chunk";
    public String chunkCancelClaimedEntityInteract =
            "&cYou can't interact with &e%%ENTITY%%&c in &e%%OWNER%%&c's chunk";
    public String chunkCancelUnclaimedEntityInteract =
            "&cYou can't interact with &e%%ENTITY%%&c in unclaimed chunks";
    public String chunkCancelClaimedEntityDamage =
            "&cYou can't damage &e%%ENTITY%%&c in &e%%OWNER%%&c's chunk";
    public String chunkCancelUnclaimedEntityDamage =
            "&cYou can't damage &e%%ENTITY%%&c in unclaimed chunks";
    public String chunkCancelClaimedBlockInteract =
            "&cYou can't interact with &e%%BLOCK%%&c in &e%%OWNER%%&c's chunk";
    public String chunkCancelUnclaimedBlockInteract =
            "&cYou can't interact with &e%%BLOCK%%&c in unclaimed chunks";
    public String chunkCancelClaimedBlockBreak =
            "&cYou can't break &e%%BLOCK%%&c in &e%%OWNER%%&c's chunk";
    public String chunkCancelUnclaimedBlockBreak =
            "&cYou can't break &e%%BLOCK%%&c in unclaimed chunks";
    public String chunkCancelClaimedBlockPlace =
            "&cYou can't place &e%%BLOCK%%&c in &e%%OWNER%%&c's chunk";
    public String chunkCancelUnclaimedBlockPlace =
            "&cYou can't place &e%%BLOCK%%&c in unclaimed chunks";
    public String chunkCancelPearlLaunch = "&cYou can't use ender pearls in &e%%OWNER%%&c's chunk";

    // AdminOverride localization
    public String adminOverrideEnable = "&eYou now have protection bypass";
    public String adminOverrideDisable = "&eYou no longer have protection bypass";

    // Command description localization
    public String cmdAccess =
            "Change permissions for [player] in either the current chunk, or across your claimed"
                    + " territory";
    public String cmdCheckAccess =
            "List permissions that [player] has in the current chunk or list permissions for all"
                    + " players with access to this chunk";
    public String cmdRevokeAccess =
            "Revoke all permissions for [player] in either the current chunk,"
                    + " or across your claimed territory";
    public String cmdAdminUnclaim =
            "Unclaim the chunk you're standing in whether or not you are the owner";
    public String cmdAlert =
            "Toggle whether or not you will receive alerts when someone enters your chunks";
    public String cmdAuto = "Automatically claim chunks when you enter";
    public String cmdClaim = "Claim the chunk you're standing in";
    public String cmdHelp = "Display ClaimChunk help (for [command], if supplied)";
    public String cmdInfo = "Display information about the current chunk";
    public String cmdList = "Display a paginated list of all your claims in the world";
    public String cmdName = "Change the name that appears when someone enters your land";
    public String cmdReload = "Reload the config for ClaimChunk";
    public String cmdShow = "Outline the chunk you're standing in with particles";
    public String cmdUnclaim = "Unclaim the chunk you're standing in";
    public String cmdUnclaimAll = "Unclaim all the chunks you own in this world";
    public String cmdAdminUnclaimAll =
            "Unclaim all the chunks of the specified player in this world as an admin";
    public String cmdGive = "Give the chunk you're standing in to <player>";
    public String cmdScan = "Scan the surrounding area for claimed chunks";
    public String cmdAdminOverride =
            "Gives or takes away the right to bypass the chunk protection.";

    // Command arguments
    public String argPlayer = "player";
    public String argCmd = "command...";
    public String argPage = "page";
    public String argNewName = "newName";
    public String argScanDistance = "scanDistance";
    public String argRadius = "radius";
    public String argSeconds = "seconds";
    public String argAcrossAllWorlds = "acrossAllWorlds";

    public String argBreak = "break";
    public String argPlace = "place";
    public String argDoors = "doors";
    public String argRedstone = "redstone";
    public String argInteractVehices = "interactVehicles";
    public String argInteractEntities = "interactEntities";
    public String argInteractBlocks = "interactBlocks";
    public String argUseContainers = "useContainers";
    public String argAllChunks = "allChunks";
    public String[] permissionArgs =
            new String[] {
                argBreak,
                argPlace,
                argDoors,
                argRedstone,
                argInteractVehices,
                argInteractEntities,
                argInteractBlocks,
                argUseContainers,
                argAllChunks
            };

    // Argument types
    public String argTypeBoolTrue = "true";
    public String argTypeBoolFalse = "false";
    public String argTypePlayer = "Player";

    // PlaceholderAPI
    public String placeholderApiUnclaimedChunkOwner = "nobody";

    /* FUNCTIONS */

    public Optional<Boolean> parseBool(@NotNull String input) {
        input = input.trim().toLowerCase();
        if (input.equals(argTypeBoolTrue)) {
            return Optional.of(true);
        } else if (input.equals(argTypeBoolFalse)) {
            return Optional.of(false);
        }
        return Optional.empty();
    }

    public static void sendAccessDeniedEntityMessage(
            @NotNull Player player,
            @NotNull ClaimChunk claimChunk,
            @NotNull NamespacedKey entityKey,
            @NotNull EntityAccess.EntityAccessType accessType,
            @Nullable UUID chunkOwner) {
        // Get display name
        final String entityName = "entity." + entityKey.getNamespace() + "." + entityKey.getKey();
        final String ownerName =
                chunkOwner != null ? claimChunk.getPlayerHandler().getChunkName(chunkOwner) : null;

        // Determine the correct message
        final V2JsonMessages messages = claimChunk.getMessages();
        String msg = null;
        if (accessType == EntityAccess.EntityAccessType.INTERACT) {
            if (chunkOwner == null) {
                msg = messages.chunkCancelUnclaimedEntityInteract;
            } else {
                msg = messages.chunkCancelClaimedEntityInteract;
            }
        } else if (accessType == EntityAccess.EntityAccessType.DAMAGE) {
            if (chunkOwner == null) {
                msg = messages.chunkCancelUnclaimedEntityDamage;
            } else {
                msg = messages.chunkCancelClaimedEntityDamage;
            }
        }

        // Send the message
        if (msg == null) {
            Utils.err("Unknown message to send to player after entity event");
        } else {
            Utils.toPlayer(
                    player,
                    replaceOwnerAndLocalizedMsg(player, msg, ownerName, "%%ENTITY%%", entityName));
        }
    }

    public static void sendAccessDeniedBlockMessage(
            @NotNull Player player,
            @NotNull ClaimChunk claimChunk,
            @NotNull NamespacedKey blockKey,
            @NotNull BlockAccess.BlockAccessType accessType,
            @Nullable UUID chunkOwner) {
        // Get display name
        final String blockName = "block." + blockKey.getNamespace() + "." + blockKey.getKey();
        final String ownerName =
                chunkOwner != null ? claimChunk.getPlayerHandler().getChunkName(chunkOwner) : null;

        // Determine the correct message
        final V2JsonMessages messages = claimChunk.getMessages();
        String msg = null;
        if (accessType == BlockAccess.BlockAccessType.INTERACT) {
            if (chunkOwner == null) {
                msg = messages.chunkCancelUnclaimedBlockInteract;
            } else {
                msg = messages.chunkCancelClaimedBlockInteract;
            }
        } else if (accessType == BlockAccess.BlockAccessType.BREAK) {
            if (chunkOwner == null) {
                msg = messages.chunkCancelUnclaimedBlockBreak;
            } else {
                msg = messages.chunkCancelClaimedBlockBreak;
            }
        } else if (accessType == BlockAccess.BlockAccessType.PLACE) {
            if (chunkOwner == null) {
                msg = messages.chunkCancelUnclaimedBlockPlace;
            } else {
                msg = messages.chunkCancelClaimedBlockPlace;
            }
        }

        // Send the message
        if (msg == null) {
            Utils.err("Unknown message to send to player after block event");
        } else {
            if (ownerName != null) {
                msg = msg.replace("%%OWNER%%", ownerName);
            }
            Utils.toPlayer(
                    player,
                    replaceOwnerAndLocalizedMsg(player, msg, ownerName, "%%BLOCK%%", blockName));
        }
    }

    public static void sendAccessDeniedPearlMessage(
            @NotNull Player player, @NotNull ClaimChunk claimChunk, @NotNull UUID chunkOwner) {
        final String ownerName = claimChunk.getPlayerHandler().getChunkName(chunkOwner);
        String msg = claimChunk.getMessages().chunkCancelPearlLaunch;
        String localizedOwner =
                ownerName == null ? claimChunk.getMessages().unknownChunkOwner : ownerName;
        Utils.toPlayer(player, replaceLocalizedMsg(player, msg, "%%OWNER%%", localizedOwner));
    }

    public static BaseComponent replaceOwnerAndLocalizedMsg(
            @NotNull CommandSender sender,
            @NotNull String input,
            @Nullable String ownerName,
            @NotNull String search,
            @NotNull String localizedVersion) {
        if (ownerName != null) input = input.replace("%%OWNER%%", ownerName);
        return replaceLocalizedMsg(sender, input, search, localizedVersion);
    }

    public static BaseComponent replaceLocalizedMsg(
            @NotNull CommandSender sender,
            @NotNull String input,
            @NotNull String search,
            @NotNull String localized) {
        if (!input.contains(search)) return Utils.toComponent(sender, input);

        String firstPart = input.substring(0, input.indexOf(search));

        BaseComponent a = Utils.toComponent(sender, firstPart);
        BaseComponent endA = a.getExtra().isEmpty() ? a : a.getExtra().get(a.getExtra().size() - 1);
        BaseComponent translated = new TranslatableComponent(localized);
        BaseComponent b =
                Utils.toComponent(sender, input.substring(firstPart.length() + search.length()));

        translated.copyFormatting(endA);

        return new TextComponent(new ComponentBuilder(a).append(translated).append(b).create());
    }

    /* LOADING */

    private static Gson gson;

    public static V2JsonMessages load(File file) throws IOException {
        // Create an empty default
        V2JsonMessages messages = new V2JsonMessages();

        boolean overwrite = true;

        // Load from file if it exists
        if (file.exists()) {
            try (BufferedReader bufferedReader = Files.newBufferedReader(file.toPath())) {
                messages = getGson().fromJson(bufferedReader, V2JsonMessages.class);
                Utils.log("Loaded messages from messages.json");
            } catch (Exception e) {
                Utils.err("Failed to load messages.json file!");
                Utils.err("This is probably a problem!!");
                Utils.err("Here's the error report:");
                e.printStackTrace();

                // Don't overwrite users' files if they don't parse correctly, that's mean.
                overwrite = false;
            }
        } else {
            Utils.log("Creating new messages.json");
        }

        if (overwrite) {
            Files.write(
                    file.toPath(),
                    Collections.singletonList(getGson().toJson(messages)),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        }

        return messages;
    }

    private static Gson getGson() {
        if (gson == null) {
            gson =
                    new GsonBuilder()
                            .setLenient()
                            .setPrettyPrinting()
                            .disableHtmlEscaping()
                            .create();
        }
        return gson;
    }
}
