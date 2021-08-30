package com.cjburkey.claimchunk.smartcommand.sub;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.smartcommand.CCSubCommand;
import de.goldmensch.commanddispatcher.ExecutorLevel;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AccessCmd extends CCSubCommand {

    public AccessCmd(ClaimChunk claimChunk) {
        // TODO: CREATE `/chunk admin access <PLY>` to allow listing from
        //       console as well
        super(claimChunk, ExecutorLevel.PLAYER);
    }

    @Override
    public String getDescription() {
        return claimChunk.getMessages().cmdAccess;
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return Utils.hasPerm(sender, true, "access");
    }

    @Override
    public String getPermissionMessage() {
        return claimChunk.getMessages().accessNoPerm;
    }

    @Override
    public CCArg[] getPermittedArguments() {
        return new CCArg[]{new CCArg("player", CCAutoComplete.OFFLINE_PLAYER)};
    }

    @Override
    public int getRequiredArguments() {
        return 0;
    }

    @Override
    public boolean onCall(String cmdUsed, CommandSender executor, String[] args) {
        Player player = (Player) executor;
        if (args.length == 0) {
            claimChunk.getMainHandler().listAccessors(player);
        } else {
            claimChunk.getMainHandler().accessChunk(player, args[0].split(","));
        }
        return true;
    }

}
