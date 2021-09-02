package com.cjburkey.claimchunk.smartcommand.sub.ply;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.smartcommand.CCSubCommand;

import de.goldmensch.commanddispatcher.Executor;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/** @since 0.0.23 */
public class InfoCmd extends CCSubCommand {

    public InfoCmd(ClaimChunk claimChunk) {
        super(claimChunk, Executor.PLAYER);
    }

    @Override
    public @NotNull Optional<String> getDescription() {
        return Optional.ofNullable(claimChunk.getMessages().cmdInfo);
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return Utils.hasPerm(sender, true, "base");
    }

    @Override
    public @NotNull String getPermissionMessage() {
        return claimChunk.getMessages().noPluginPerm;
    }

    @Override
    public CCArg[] getPermittedArguments() {
        return new CCArg[0];
    }

    @Override
    public int getRequiredArguments() {
        return 0;
    }

    @Override
    public boolean onCall(@NotNull String cmdUsed, @NotNull CommandSender executor, String[] args) {
        var player = (Player) executor;
        var playerHandler = claimChunk.getPlayerHandler();
        var chunk = player.getLocation().getChunk();
        var owner = claimChunk.getChunkHandler().getOwner(chunk);

        var ownerName = ((owner == null) ? null : playerHandler.getUsername(owner));
        if (ownerName == null) ownerName = claimChunk.getMessages().infoOwnerUnknown;

        var ownerDisplay =
                ((owner == null || !playerHandler.hasChunkName(owner))
                        ? null
                        : playerHandler.getChunkName(owner));
        if (ownerDisplay == null) ownerDisplay = claimChunk.getMessages().infoNameNone;

        messageChat(
                player,
                String.format(
                        claimChunk.getMessages().infoHeader, // "%s&l--- [ %s ] ---",
                        claimChunk.chConfig().getInfoColor(),
                        claimChunk.getMessages().infoTitle));
        messageChat(
                player,
                claimChunk.chConfig().getInfoColor()
                        + (claimChunk
                                .getMessages()
                                .infoPosition
                                .replace("%%X%%", "" + chunk.getX())
                                .replace("%%Z%%", "" + chunk.getZ())
                                .replace("%%WORLD%%", chunk.getWorld().getName())));
        messageChat(
                player,
                claimChunk.chConfig().getInfoColor()
                        + claimChunk.getMessages().infoOwner.replace("%%PLAYER%%", ownerName));
        messageChat(
                player,
                claimChunk.chConfig().getInfoColor()
                        + claimChunk.getMessages().infoName.replace("%%NAME%%", ownerDisplay));
        return true;
    }
}
