package com.cjburkey.claimchunk.cmds;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.cmd.Argument;
import com.cjburkey.claimchunk.cmd.ICommand;
import com.cjburkey.claimchunk.player.PlayerHandler;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Deprecated
public class CmdName implements ICommand {

    @Override
    public String getCommand(ClaimChunk claimChunk) {
        return "name";
    }

    @Override
    public String getDescription(ClaimChunk claimChunk) {
        return claimChunk.getMessages().cmdName;
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
        return new Argument[] {
            new Argument("newName", Argument.TabCompletion.NONE),
        };
    }

    @Override
    public int getRequiredArguments(ClaimChunk claimChunk) {
        return 0;
    }

    @Override
    public boolean onCall(ClaimChunk claimChunk, String cmdUsed, Player executor, String[] args) {
        PlayerHandler nh = claimChunk.getPlayerHandler();
        try {
            if (args.length == 0) {
                if (nh.hasChunkName(executor.getUniqueId())) {
                    nh.clearChunkName(executor.getUniqueId());
                    Utils.toPlayer(executor, claimChunk.getMessages().nameClear);
                } else {
                    Utils.toPlayer(executor, claimChunk.getMessages().nameNotSet);
                }
            } else {
                nh.setChunkName(executor.getUniqueId(), args[0].trim());
                Utils.toPlayer(
                        executor,
                        claimChunk.getMessages().nameSet.replace("%%NAME%%", args[0].trim()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Utils.msg(executor, "&4&lAn error occurred, please contact an admin.");
        }
        return true;
    }
}
