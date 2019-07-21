package com.cjburkey.claimchunk.cmds;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Config;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.cmd.Argument;
import com.cjburkey.claimchunk.cmd.ICommand;
import com.cjburkey.claimchunk.player.PlayerHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CmdName implements ICommand {

    @Override
    public String getCommand() {
        return "name";
    }

    @Override
    public String getDescription() {
        return "Change the name that appears when someone enters your land.";
    }

    @Override
    public boolean getShouldDisplayInHelp(CommandSender sender) {
        return Utils.hasPerm(sender, true, "base");
    }

    @Override
    public Argument[] getPermittedArguments() {
        return new Argument[] {new Argument("newName", Argument.TabCompletion.NONE)};
    }

    @Override
    public int getRequiredArguments() {
        return 0;
    }

    @Override
    public boolean onCall(String cmdUsed, Player executor, String[] args) {
        PlayerHandler nh = ClaimChunk.getInstance().getPlayerHandler();
        try {
            if (args.length == 0) {
                if (nh.hasChunkName(executor.getUniqueId())) {
                    nh.clearChunkName(executor.getUniqueId());
                    Utils.toPlayer(executor, Config.successColor(), Utils.getMsg("nameClear"));
                } else {
                    Utils.toPlayer(executor, Config.errorColor(), Utils.getMsg("nameNotSet"));
                }
            } else {
                nh.setChunkName(executor.getUniqueId(), args[0].trim());
                Utils.toPlayer(executor, Config.successColor(),
                        Utils.getMsg("nameSet").replace("%%NAME%%", args[0].trim()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Utils.msg(executor, "&4&lAn error occurred, please contact an admin.");
        }
        return true;
    }

}
