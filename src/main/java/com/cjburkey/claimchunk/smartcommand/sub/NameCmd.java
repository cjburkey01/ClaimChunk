package com.cjburkey.claimchunk.smartcommand.sub;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.player.PlayerHandler;
import com.cjburkey.claimchunk.smartcommand.CCSubCommand;

import de.goldmensch.commanddispatcher.ExecutorLevel;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** @since 0.0.23 */
public class NameCmd extends CCSubCommand {

    public NameCmd(ClaimChunk claimChunk) {
        super(claimChunk, ExecutorLevel.PLAYER);
    }

    @Override
    public String getDescription() {
        return claimChunk.getMessages().cmdName;
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
        return new CCArg[] {
            new CCArg("newName", CCAutoComplete.NONE),
        };
    }

    @Override
    public int getRequiredArguments() {
        return 0;
    }

    @Override
    public boolean onCall(String cmdUsed, CommandSender executor, String[] args) {
        Player player = (Player) executor;
        PlayerHandler nh = claimChunk.getPlayerHandler();
        try {
            if (args.length == 0) {
                if (nh.hasChunkName(player.getUniqueId())) {
                    nh.clearChunkName(player.getUniqueId());
                    Utils.toPlayer(player, claimChunk.getMessages().nameClear);
                } else {
                    Utils.toPlayer(player, claimChunk.getMessages().nameNotSet);
                }
            } else {
                nh.setChunkName(player.getUniqueId(), args[0].trim());
                Utils.toPlayer(
                        player,
                        claimChunk.getMessages().nameSet.replace("%%NAME%%", args[0].trim()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Utils.msg(player, "&4&lAn error occurred, please contact an admin.");
        }
        return true;
    }
}
