package com.cjburkey.claimchunk.cmd;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public interface ICommand {

    String getCommand();

    String getDescription();

    boolean getShouldDisplayInHelp(CommandSender sender);

    Argument[] getPermittedArguments();

    int getRequiredArguments();

    boolean onCall(String cmdUsed, Player executor, String[] args);

}
