package com.cjburkey.claimchunk.cmds;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.cmd.Argument;
import com.cjburkey.claimchunk.cmd.ICommand;
import com.cjburkey.claimchunk.player.PlayerHandler;
import java.util.UUID;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CmdInfo implements ICommand {

    @Override
    public String getCommand(ClaimChunk claimChunk) {
        return "info";
    }

    @Override
    public String getDescription(ClaimChunk claimChunk) {
        return claimChunk.getMessages().cmdInfo;
    }

    @Override
    public boolean hasPermission(ClaimChunk claimChunk, CommandSender sender) {
        return Utils.hasPerm(sender, true, "base");
    }

    @Override
    public String getPermissionMessage(ClaimChunk claimChunk) {
        return claimChunk.getMessages().noPluginPerm;
    }

    @Override
    public Argument[] getPermittedArguments(ClaimChunk claimChunk) {
        return new Argument[]{};
    }

    @Override
    public int getRequiredArguments(ClaimChunk claimChunk) {
        return 0;
    }

    @Override
    public boolean onCall(ClaimChunk claimChunk, String cmdUsed, Player executor, String[] args) {
        PlayerHandler playerHandler = claimChunk.getPlayerHandler();
        Chunk chunk = executor.getLocation().getChunk();
        UUID owner = claimChunk.getChunkHandler().getOwner(chunk);

        String ownerName = ((owner == null)
                ? null
                : playerHandler.getUsername(owner));
        if (ownerName == null) ownerName = claimChunk.getMessages().infoOwnerUnknown;

        String ownerDisplay = ((owner == null || !playerHandler.hasChunkName(owner))
                ? null
                : playerHandler.getChunkName(owner));
        if (ownerDisplay == null) ownerDisplay = claimChunk.getMessages().infoNameNone;

        Utils.msg(executor, String.format("%s&l--- [ %s ] ---",
                claimChunk.chConfig().getInfoColor(),
                claimChunk.getMessages().infoTitle));
        Utils.msg(executor, claimChunk.chConfig().getInfoColor() + (claimChunk.getMessages().infoPosition
                .replace("%%X%%", "" + chunk.getX())
                .replace("%%Z%%", "" + chunk.getZ())
                .replace("%%WORLD%%", chunk.getWorld().getName())));
        Utils.msg(executor, claimChunk.chConfig().getInfoColor() + claimChunk.getMessages().infoOwner
                .replace("%%PLAYER%%", ownerName));
        Utils.msg(executor, claimChunk.chConfig().getInfoColor() + claimChunk.getMessages().infoName
                .replace("%%NAME%%", ownerDisplay));
        return true;
    }

}
