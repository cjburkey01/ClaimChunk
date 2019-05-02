package com.cjburkey.claimchunk.cmds;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Config;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.cmd.Argument;
import com.cjburkey.claimchunk.cmd.ICommand;
import org.bukkit.ChatColor;
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
        return "Reload the config for ClaimChunk";
    }

    @Override
    public boolean getShouldDisplayInHelp(CommandSender sender) {
        return Utils.hasPerm(sender, false, "admin");
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
        if (!Utils.hasPerm(executor, false, "admin")) {
            Utils.toPlayer(executor, ChatColor.RED, Config.getString("messages", "reloadNoPerm"));
            return true;
        }
        PluginManager pluginManager = ClaimChunk.getInstance().getServer().getPluginManager();
        ClaimChunk.getInstance().reloadConfig();
        pluginManager.disablePlugin(ClaimChunk.getInstance());
        pluginManager.enablePlugin(ClaimChunk.getInstance());
        return true;
    }

}
