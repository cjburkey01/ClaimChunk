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
    public String getCommand() {
        return "reload";
    }

    @Override
    public String getDescription() {
        return ClaimChunk.getInstance().getMessages().cmdReload;
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return Utils.hasAdmin(sender);
    }

    @Override
    public String getPermissionMessage() {
        return ClaimChunk.getInstance().getMessages().reloadNoPerm;
    }

    @Override
    public Argument[] getPermittedArguments() {
        return new Argument[0];
    }

    @Override
    public int getRequiredArguments() {
        return 0;
    }

    @Override
    public boolean onCall(String cmdUsed, Player executor, String[] args) {
        PluginManager pluginManager = ClaimChunk.getInstance().getServer().getPluginManager();
        ClaimChunk.getInstance().reloadConfig();
        pluginManager.disablePlugin(ClaimChunk.getInstance());
        pluginManager.enablePlugin(ClaimChunk.getInstance());
        Utils.toPlayer(executor, ClaimChunk.getInstance().getMessages().reloadComplete);
        return true;
    }

}
