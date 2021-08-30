package com.cjburkey.claimchunk.smartcommand.sub;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.player.PlayerHandler;
import com.cjburkey.claimchunk.smartcommand.CCSubCommand;

import de.goldmensch.commanddispatcher.ExecutorLevel;

import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/** @since 0.0.23 */
public class InfoCmd extends CCSubCommand {

    public InfoCmd(ClaimChunk claimChunk) {
        super(claimChunk, ExecutorLevel.PLAYER);
    }

    @Override
    public String getDescription() {
        return claimChunk.getMessages().cmdInfo;
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return Utils.hasPerm(sender, true, "base");
    }

    @Override
    public String getPermissionMessage() {
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
    public boolean onCall(String cmdUsed, CommandSender executor, String[] args) {
        Player player = (Player) executor;
        PlayerHandler playerHandler = claimChunk.getPlayerHandler();
        Chunk chunk = player.getLocation().getChunk();
        UUID owner = claimChunk.getChunkHandler().getOwner(chunk);

        String ownerName = ((owner == null) ? null : playerHandler.getUsername(owner));
        if (ownerName == null) ownerName = claimChunk.getMessages().infoOwnerUnknown;

        String ownerDisplay =
                ((owner == null || !playerHandler.hasChunkName(owner))
                        ? null
                        : playerHandler.getChunkName(owner));
        if (ownerDisplay == null) ownerDisplay = claimChunk.getMessages().infoNameNone;

        Utils.msg(
                player,
                String.format(
                        claimChunk.getMessages().infoHeader, // "%s&l--- [ %s ] ---",
                        claimChunk.chConfig().getInfoColor(),
                        claimChunk.getMessages().infoTitle));
        Utils.msg(
                player,
                claimChunk.chConfig().getInfoColor()
                        + (claimChunk
                                .getMessages()
                                .infoPosition
                                .replace("%%X%%", "" + chunk.getX())
                                .replace("%%Z%%", "" + chunk.getZ())
                                .replace("%%WORLD%%", chunk.getWorld().getName())));
        Utils.msg(
                player,
                claimChunk.chConfig().getInfoColor()
                        + claimChunk.getMessages().infoOwner.replace("%%PLAYER%%", ownerName));
        Utils.msg(
                player,
                claimChunk.chConfig().getInfoColor()
                        + claimChunk.getMessages().infoName.replace("%%NAME%%", ownerDisplay));
        return true;
    }
}
