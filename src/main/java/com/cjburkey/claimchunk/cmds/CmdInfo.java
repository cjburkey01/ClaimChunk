package com.cjburkey.claimchunk.cmds;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Config;
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
    public String getCommand() {
        return "info";
    }

    @Override
    public String getDescription() {
        return "Display information about the current chunk";
    }

    @Override
    public boolean getShouldDisplayInHelp(CommandSender sender) {
        return Utils.hasPerm(sender, true, "base");
    }

    @Override
    public Argument[] getPermittedArguments() {
        return new Argument[] {};
    }

    @Override
    public int getRequiredArguments() {
        return 0;
    }

    @Override
    public boolean onCall(String cmdUsed, Player executor, String[] args) {
        PlayerHandler playerHandler = ClaimChunk.getInstance().getPlayerHandler();
        Chunk chunk = executor.getLocation().getChunk();
        UUID owner = ClaimChunk.getInstance().getChunkHandler().getOwner(chunk);

        String ownerName = ((owner == null)
                ? null
                : playerHandler.getUsername(owner));
        if (ownerName == null) ownerName = Utils.getMsg("infoOwnerUnknown");

        String ownerDisplay = ((owner == null || !playerHandler.hasChunkName(owner)) ? null : playerHandler.getChunkName(owner));
        if (ownerDisplay == null) ownerDisplay = Utils.getMsg("infoNameNone");

        Utils.msg(executor, String.format(Config.infoColor() + "&l--- [ %s ] ---", Utils.getMsg("infoTitle")));
        Utils.msg(executor, Config.infoColor() + (Utils.getMsg("infoPosition")
                .replace("%%X%%", "" + chunk.getX())
                .replace("%%Z%%", "" + chunk.getZ())
                .replace("%%WORLD%%", chunk.getWorld().getName())));
        Utils.msg(executor, Config.infoColor() + Utils.getMsg("infoOwner").replace("%%PLAYER%%", ownerName));
        Utils.msg(executor, Config.infoColor() + Utils.getMsg("infoName").replace("%%NAME%%", ownerDisplay));
        return true;
    }

}
