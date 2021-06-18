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
    public String errEnterValidNum = "&cPlease enter a valid number";
    public String noPluginPerm = "&cYou do not have permission to use ClaimChunk";
    public String noPlayer = "&cThat player has not joined the server before";

    // CMD localization
    public String ingameOnly = "Only in-game players may use ClaimChunk";
    public String invalidCommand = "&cInvalid command. See: &6/%%CMD%% help";
    public String errorDisplayUsage = "&cUsage: &6/%%CMD%% %%SUB_CMD%% %%ARGS%%";

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

    // Give localization
    public String giveDisabled = "&cChunk giving has been disabled";
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
    public String unclaimNoPermAdmin = "&cYou do not have permission to admin unclaim";
    public String unclaimNotOwned = "&cThis chunk is not owned";
    public String unclaimNotOwner = "&cYou do not own this chunk";
    public String unclaimSuccess = "&aChunk unclaimed!";
    public String unclaimRefund = "&aYou have been refunded %%AMT%%";
    public String unclaimAll = "&aUnclaimed %%CHUNKS%% chunks";

    // Admin unclaim localization
    public String adminUnclaimAll = "&aAdmin unclaimed %%CHUNKS%% chunks";

    // Access localization
    public String accessListTitle = "&6&l---[ ClaimChunk Access ] ---";
    public String accessNoPerm = "&cYou do not have permission to give access to chunks";
    public String accessHas = "&a%%PLAYER%% now has access to your chunks";
    public String accessNoLongerHas = "&a%%PLAYER%% no longer has access to your chunks";
    public String accessToggleMultiple = "&aThe provided players'' access to your chunks has been toggled";
    public String accessOneself = "&cYou already have access to your own chunks";
    public String accessNoOthers = "&cNo other players have access to your chunks";

    // TNT localization
    public String tntNoPerm = "&cYou do not have permission to toggle TNT in this chunk";
    //public String tntAlreadyEnabled = "&cTNT is already enabled in the config";
    public String tntEnabled = "&aTNT has been enabled in this chunk";
    public String tntDisabled = "&aTNT has been disabled in this chunk";

    // Name localization
    public String nameClear = "&aYour name has been cleared";
    public String nameNotSet = "&cYou do not have a custom name set";
    public String nameSet = "&aYour name has been set: %%NAME%%";

    // Auto localization
    public String autoNoPerm = "&cYou may not auto-claim chunks";
    public String autoEnabled = "&aAutomatic claiming enabled";
    public String autoDisabled = "&aAutomatic claiming disabled";

    // Reload localization
    public String reloadNoPerm = "&cYou do not have permission to reload ClaimChunk";
    public String reloadComplete = "&aReload complete";

    // Alert localization
    public String playerEnterChunk = "&6%%PLAYER%% has entered your claimed chunk";
    public String enabledAlerts = "&aEnabled alerts";
    public String disabledAlerts = "&aDisabled alerts";
    public String alertNoPerm = "&cYou do not have permission to toggle alerts";

    // Help localization
    public String helpHeader = "&6--- [ &lClaimChunk Help&r&6 ] ---";
    public String helpCmdHeader = "&6--- [ &e/%%USED%% %%CMD%% &l&6Help ] ---";
    @SuppressWarnings("SpellCheckingInspection")
    public String helpCmdNotFound = "&cCommand &e/%%USED%% %%CMD%% &cnot found.";
    public String helpCmd = "&e/%%USED%% %%CMD%% %%ARGS%%\n  &6%%DESC%%";

    // Info localization
    public String infoTitle = "Chunk Information";
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
    public String chunkCancelAdjacentPlace = "&cYou can't place &e%%BLOCK%%&c next to &e%%BLOCK%%&c in %%OWNER%%&c's chunks";
    public String chunkCancelClaimedEntityInteract = "&cYou can't interact with &e%%ENTITY%%&c in &e%%OWNER%%&c's chunks";
    public String chunkCancelUnclaimedEntityInteract = "&cYou can't interact with &e%%ENTITY%%&c in unclaimed chunks";
    public String chunkCancelClaimedEntityDamage = "&cYou can't damage &e%%ENTITY%%&c in &e%%OWNER%%&c's chunks";
    public String chunkCancelUnclaimedEntityDamage = "&cYou can't damage &e%%ENTITY%%&c in unclaimed chunks";
    public String chunkCancelClaimedBlockInteract = "&cYou can't interact with &e%%BLOCK%%&c in &e%%OWNER%%&c's chunks";
    public String chunkCancelUnclaimedBlockInteract = "&cYou can't interact with &e%%BLOCK%%&c in unclaimed chunks";
    public String chunkCancelClaimedBlockBreak = "&cYou can't break &e%%BLOCK%%&c in &e%%OWNER%%&c's chunks";
    public String chunkCancelUnclaimedBlockBreak = "&cYou can't break &e%%BLOCK%%&c in unclaimed chunks";
    public String chunkCancelClaimedBlockPlace = "&cYou can't place &e%%BLOCK%%&c in &e%%OWNER%%&c's chunks";
    public String chunkCancelUnclaimedBlockPlace = "&cYou can't place &e%%BLOCK%%&c in unclaimed chunks";

    // AdminOverride localization
    public String adminOverrideNoPerm = "&cYou have no permissions to use adminOverride";
    public String adminOverrideEnable = "&eYou now have protection bypass";
    public String adminOverrideDisable = "&eYou no longer have protection bypass";

    // Command description localization
    public String cmdAccess = "Toggle access for [player] in your claimed territory or list players that have access to your chunks";
    public String cmdAdminUnclaim = "Unclaim the chunk you're standing in whether or not you are the owner";
    public String cmdAlert = "Toggle whether or not you will receive alerts when someone enters your chunks";
    public String cmdAuto = "Automatically claim chunks when you enter";
    public String cmdClaim = "Claim the chunk you're standing in";
    public String cmdHelp = "Display ClaimChunk help (for [command], if supplied)";
    public String cmdInfo = "Display information about the current chunk";
    public String cmdList = "Display a paginated list of all your claims in the world";
    public String cmdName = "Change the name that appears when someone enters your land";
    public String cmdReload = "Reload the config for ClaimChunk";
    public String cmdShow = "Outline the chunk you're standing in with particles";
    public String cmdTnt = "Toggle whether or not TNT can explode in the current chunk";
    public String cmdUnclaim = "Unclaim the chunk you're standing in";
    public String cmdUnclaimAll = "Unclaim all the chunks you own in this world";
    public String cmdAdminUnclaimAll = "Unclaim all the chunks of the specified player in this world as an admin";
    public String cmdGive = "Give the chunk you're standing in to <player>";
    public String cmdAdminOverride = "Gives or takes away the right to bypass the chunkprotection.";

    // PlaceholderAPI
    public String placeholderApiUnclaimedChunkOwner = "nobody";
    public String placeholderApiTrusted = "trusted";
    public String placeholderApiNotTrusted = "not trusted";

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
