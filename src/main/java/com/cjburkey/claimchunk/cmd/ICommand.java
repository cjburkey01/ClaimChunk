package com.cjburkey.claimchunk.cmd;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public interface ICommand {

    String getCommand();

    String getDescription();

    boolean hasPermission(CommandSender executor);

    String getPermissionMessage();

    default boolean getShouldDisplayInHelp(CommandSender sender) {
        return hasPermission(sender);
    }

    Argument[] getPermittedArguments();

    int getRequiredArguments();

    boolean onCall(String cmdUsed, Player executor, String[] args);

}
