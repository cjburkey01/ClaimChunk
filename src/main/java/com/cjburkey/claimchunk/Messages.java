package com.cjburkey.claimchunk;

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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.UUID;

public final class Messages {

    // Global localization
    public final String errEnterValidNum = "&cPlease enter a valid number";
    public final String noPluginPerm = "&cYou do not have permission to use ClaimChunk";
    public final String noPlayer = "&cThat player has not joined the server before";

    // CMD localization
    public final String ingameOnly = "Only in-game players may use ClaimChunk";
    public final String invalidCommand = "&cInvalid command. See: &6/%%CMD%% help";
    public final String errorDisplayUsage = "&cUsage: &6/%%CMD%% %%SUB_CMD%% %%ARGS%%";

    // Claim localization
    public final String claimNoPerm = "&cYou do not have permission to claim chunks";
    public final String claimWorldDisabled = "&cClaimChunk is not enabled in this world";
    public final String claimLocationBlock = "&cYou cannot claim chunks here";
    public final String claimAlreadyOwned = "&cThis chunk is already claimed";
    public final String claimTooMany = "&cYou own the maximum number of chunks";
    public final String claimSuccess = "&aChunk claimed for %%PRICE%%!";
    public final String claimNoCost = "Free";
    public final String claimFree1 = "&aFirst chunk is free!";
    public final String claimFrees = "&aFirst %%COUNT%% chunks are free!";
    public final String claimNotEnoughMoney = "&cYou do not have enough money";

    // Give localization
    public final String giveDisabled = "&cChunk giving has been disabled";
    public final String giveNoPerm = "&cYou do not have permission to give chunks";
    public final String giveNotYourChunk = "&cYou do not own this chunk";
    public final String giveNoPlayer = "&c%%PLAYER%% was not found, they may be offline";
    public final String giveNotYourself = "&cYou already own this chunk";
    public final String giveChunksFull = "&c%%PLAYER can't own any more chunks";
    public final String giveError = "&cUnable to claim chunk for new owner";
    public final String gaveChunk = "&aChunk given to %%PLAYER%%";
    public final String givenChunk = "&aChunk received from %%PLAYER%%";

    // Unclaim localization
    public final String unclaimNoPerm = "&cYou do not have permission to unclaim chunks";
    public final String unclaimNoPermAdmin = "&cYou do not have permission to admin unclaim";
    public final String unclaimNotOwned = "&cThis chunk is not owned";
    public final String unclaimNotOwner = "&cYou do not own this chunk";
    public final String unclaimSuccess = "&aChunk unclaimed!";
    public final String unclaimRefund = "&aYou have been refunded %%AMT%%";
    public final String unclaimAll = "&aUnclaimed %%CHUNKS%% chunks";

    // Admin unclaim localization
    public final String adminUnclaimAll = "&aAdmin unclaimed %%CHUNKS%% chunks";

    // Access localization
    public final String accessListTitle = "&6&l---[ ClaimChunk Access ] ---";
    public final String accessNoPerm = "&cYou do not have permission to give access to chunks";
    public final String accessHas = "&a%%PLAYER%% now has access to your chunks";
    public final String accessNoLongerHas = "&a%%PLAYER%% no longer has access to your chunks";
    public final String accessToggleMultiple = "&aThe provided players'' access to your chunks has been toggled";
    public final String accessOneself = "&cYou already have access to your own chunks";
    public final String accessNoOthers = "&cNo other players have access to your chunks";

    // TNT localization
    public final String tntNoPerm = "&cYou do not have permission to toggle TNT in this chunk";
    //public final String tntAlreadyEnabled = "&cTNT is already enabled in the config";
    public final String tntEnabled = "&aTNT has been enabled in this chunk";
    public final String tntDisabled = "&aTNT has been disabled in this chunk";

    // Name localization
    public final String nameClear = "&aYour name has been cleared";
    public final String nameNotSet = "&cYou do not have a custom name set";
    public final String nameSet = "&aYour name has been set: %%NAME%%";

    // Auto localization
    public final String autoNoPerm = "&cYou may not auto-claim chunks";
    public final String autoEnabled = "&aAutomatic claiming enabled";
    public final String autoDisabled = "&aAutomatic claiming disabled";

    // Reload localization
    public final String reloadNoPerm = "&cYou do not have permission to reload ClaimChunk";
    public final String reloadComplete = "&aReload complete";

    // Alert localization
    public final String playerEnterChunk = "&6%%PLAYER%% has entered your claimed chunk";
    public final String enabledAlerts = "&aEnabled alerts";
    public final String disabledAlerts = "&aDisabled alerts";
    public final String alertNoPerm = "&cYou do not have permission to toggle alerts";

    // Help localization
    public final String helpHeader = "&6--- [ &lClaimChunk Help&r&6 ] ---";
    public final String helpCmdHeader = "&6--- [ &e/%%USED%% %%CMD%% &l&6Help ] ---";
    @SuppressWarnings("SpellCheckingInspection")
    public final String helpCmdNotFound = "&cCommand &e/%%USED%% %%CMD%% &cnot found.";
    public final String helpCmd = "&e/%%USED%% %%CMD%% %%ARGS%%\n  &6%%DESC%%";

    // Info localization
    public final String infoTitle = "Chunk Information";
    public final String infoPosition = "Chunk position: &l%%X%%, %%Z%% in %%WORLD%%";
    public final String infoOwnerUnknown = "&7Unknown";
    public final String infoOwner = "Chunk owner: &l%%PLAYER%%";
    public final String infoNameNone = "&7None";
    public final String infoName = "Chunk name: &l%%NAME%%";

    // List localization
    public final String claimsTitle = "Claims for %%NAME%% in %%WORLD%%";
    public final String claimsChunk = "%%X%%, %%Z%%";
    public final String claimsPagination = "Page %%PAGE%% of %%MAXPAGE%%";

    // Movement localization
    public final String chunkOwner = "&6Entering the territory of %%PLAYER%%";
    public final String unknownChunkOwner = "&6Entering claimed territory";
    public final String chunkSelf = "&6Entering your own territory";
    public final String chunkLeave = "&6Exiting the territory of %%PLAYER%%";
    public final String chunkLeaveUnknown = "&6Entering unclaimed territory";
    public final String chunkLeaveSelf = "&6Exiting your territory";

    // Protection localization
    public final String chunkCancelAdjacentPlace = "&cYou can't place &e%%BLOCK%%&c next to &e%%BLOCK%%&c in %%OWNER%%&c's chunks";
    public final String chunkCancelClaimedEntityInteract = "&cYou can't interact with &e%%ENTITY%%&c in &e%%OWNER%%&c's chunks";
    public final String chunkCancelUnclaimedEntityInteract = "&cYou can't interact with &e%%ENTITY%%&c in unclaimed chunks";
    public final String chunkCancelClaimedEntityDamage = "&cYou can't damage &e%%ENTITY%%&c in &e%%OWNER%%&c's chunks";
    public final String chunkCancelUnclaimedEntityDamage = "&cYou can't damage &e%%ENTITY%%&c in unclaimed chunks";
    public final String chunkCancelClaimedBlockInteract = "&cYou can't interact with &e%%BLOCK%%&c in &e%%OWNER%%&c's chunks";
    public final String chunkCancelUnclaimedBlockInteract = "&cYou can't interact with &e%%BLOCK%%&c in unclaimed chunks";
    public final String chunkCancelClaimedBlockBreak = "&cYou can't break &e%%BLOCK%%&c in &e%%OWNER%%&c's chunks";
    public final String chunkCancelUnclaimedBlockBreak = "&cYou can't break &e%%BLOCK%%&c in unclaimed chunks";
    public final String chunkCancelClaimedBlockPlace = "&cYou can't place &e%%BLOCK%%&c in &e%%OWNER%%&c's chunks";
    public final String chunkCancelUnclaimedBlockPlace = "&cYou can't place &e%%BLOCK%%&c in unclaimed chunks";

    // AdminOverride localization
    public final String adminOverrideNoPerm = "&cYou have no permissions to use adminOverride";
    public final String adminOverrideEnable = "&eYou now have protection bypass";
    public final String adminOverrideDisable = "&eYou no longer have protection bypass";

    // Command description localization
    public final String cmdAccess = "Toggle access for [player] in your claimed territory or list players that have access to your chunks";
    public final String cmdAdminUnclaim = "Unclaim the chunk you're standing in whether or not you are the owner";
    public final String cmdAlert = "Toggle whether or not you will receive alerts when someone enters your chunks";
    public final String cmdAuto = "Automatically claim chunks when you enter";
    public final String cmdClaim = "Claim the chunk you're standing in";
    public final String cmdHelp = "Display ClaimChunk help (for [command], if supplied)";
    public final String cmdInfo = "Display information about the current chunk";
    public final String cmdList = "Display a paginated list of all your claims in the world";
    public final String cmdName = "Change the name that appears when someone enters your land";
    public final String cmdReload = "Reload the config for ClaimChunk";
    public final String cmdShow = "Outline the chunk you're standing in with particles";
    public final String cmdTnt = "Toggle whether or not TNT can explode in the current chunk";
    public final String cmdUnclaim = "Unclaim the chunk you're standing in";
    public final String cmdUnclaimAll = "Unclaim all the chunks you own in this world";
    public final String cmdAdminUnclaimAll = "Unclaim all the chunks of the specified player in this world as an admin";
    public final String cmdGive = "Give the chunk you're standing in to <player>";
    public final String cmdAdminOverride = "Gives or takes away the right to bypass the chunkprotection.";

    // PlaceholderAPI
    public final String placeholderApiUnclaimedChunkOwner = "nobody";
    public final String placeholderApiTrusted = "trusted";
    public final String placeholderApiNotTrusted = "not trusted";

    /* FUNCTIONS */

    public static void sendAccessDeniedEntityMessage(@Nonnull Player player,
                                                     @Nonnull ClaimChunk claimChunk,
                                                     @Nonnull NamespacedKey entityKey,
                                                     @Nonnull EntityAccess.EntityAccessType accessType,
                                                     @Nullable UUID chunkOwner) {
        // Get display name
        final String entityName = "entity." + entityKey.getNamespace() + "." + entityKey.getKey();
        final String ownerName = chunkOwner != null
                ? claimChunk.getPlayerHandler().getChunkName(chunkOwner)
                : null;

        // Determine the correct message
        final Messages messages = claimChunk.getMessages();
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
            Utils.toPlayer(player, replaceOwnerAndLocalizedMsg(player, msg, ownerName, "%%ENTITY%%", entityName));
        }
    }

    public static void sendAccessDeniedBlockMessage(@Nonnull Player player,
                                                    @Nonnull ClaimChunk claimChunk,
                                                    @Nonnull NamespacedKey blockKey,
                                                    @Nonnull BlockAccess.BlockAccessType accessType,
                                                    @Nullable UUID chunkOwner) {
        // Get display name
        final String blockName = "block." + blockKey.getNamespace() + "." + blockKey.getKey();
        final String ownerName = chunkOwner != null
                ? claimChunk.getPlayerHandler().getChunkName(chunkOwner)
                : null;

        // Determine the correct message
        final Messages messages = claimChunk.getMessages();
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
            Utils.toPlayer(player, replaceOwnerAndLocalizedMsg(player, msg, ownerName, "%%BLOCK%%", blockName));
        }
    }

    private static BaseComponent replaceOwnerAndLocalizedMsg(@Nonnull CommandSender sender,
                                                             @Nonnull String input,
                                                             @Nullable String ownerName,
                                                             @Nonnull String search,
                                                             @Nonnull String localizedVersion) {
        if (ownerName != null) input = input.replace("%%OWNER%%", ownerName);
        return replaceLocalizedMsg(sender, input, search, localizedVersion);
    }

    public static BaseComponent replaceLocalizedMsg(@Nonnull CommandSender sender,
                                                    @Nonnull String input,
                                                    @Nonnull String search,
                                                    @Nonnull String localized) {
        if (!input.contains(search)) return Utils.toComponent(sender, input);

        String firstPart = input.substring(0, input.indexOf(search));

        BaseComponent a = Utils.toComponent(sender, firstPart);
        BaseComponent endA = a.getExtra().isEmpty() ? a : a.getExtra().get(a.getExtra().size() - 1);
        BaseComponent translated = new TranslatableComponent(localized);
        BaseComponent b = Utils.toComponent(sender, input.substring(firstPart.length() + search.length()));

        translated.copyFormatting(endA);

        return new TextComponent(
                new ComponentBuilder(a)
                        .append(translated)
                        .append(b).create()
        );
    }

    /* LOADING */

    private transient static Gson gson;

    static Messages load(File file) throws IOException {
        // Load or create new
        Messages messages = (file.exists()
                ? getGson().fromJson(String.join("", Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)), Messages.class)
                : new Messages());

        // Write it so new messages are written
        Files.write(file.toPath(),
                Collections.singletonList(getGson().toJson(messages)),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE);

        return messages;
    }

    private static Gson getGson() {
        if (gson == null) {
            gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .disableHtmlEscaping()
                    .create();
        }
        return gson;
    }

}
