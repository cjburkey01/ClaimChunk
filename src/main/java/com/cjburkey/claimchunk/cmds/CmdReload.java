package com.cjburkey.claimchunk.cmds;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.cmd.Argument;
import com.cjburkey.claimchunk.cmd.ICommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

public class CmdReload implements ICommand {

    @Override
    public String getCommand(ClaimChunk claimChunk) {
        return "reload";
    }

    @Override
    public String getDescription(ClaimChunk claimChunk) {
        return claimChunk.getMessages().cmdReload;
    }

    @Override
    public boolean hasPermission(ClaimChunk claimChunk, CommandSender sender) {
        return Utils.hasAdmin(sender);
    }

    @Override
    public String getPermissionMessage(ClaimChunk claimChunk) {
        return claimChunk.getMessages().reloadNoPerm;
    }

    @Override
    public Argument[] getPermittedArguments(ClaimChunk claimChunk) {
        return new Argument[0];
    }

    @Override
    public int getRequiredArguments(ClaimChunk claimChunk) {
        return 0;
    }

    @Override
    public boolean onCall(ClaimChunk claimChunk, String cmdUsed, Player executor, String[] args) {
        PluginManager pluginManager = claimChunk.getServer().getPluginManager();
        pluginManager.disablePlugin(claimChunk);
        // Simulate a restart
        claimChunk.onLoad();
        pluginManager.enablePlugin(claimChunk);
        Utils.toPlayer(executor, claimChunk.getMessages().reloadComplete);
        return true;
    }

}
