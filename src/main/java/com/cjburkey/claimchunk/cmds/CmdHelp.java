package com.cjburkey.claimchunk.cmds;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.cmd.Argument;
import com.cjburkey.claimchunk.cmd.ICommand;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Deprecated
public class CmdHelp implements ICommand {

    @Override
    public String getCommand(ClaimChunk claimChunk) {
        return "help";
    }

    @Override
    public String getDescription(ClaimChunk claimChunk) {
        return claimChunk.getMessages().cmdHelp;
    }

    @Override
    public boolean hasPermission(ClaimChunk claimChunk, CommandSender sender) {
        return Utils.hasPerm(sender, true, "base");
    }

    public String getPermissionMessage(ClaimChunk claimChunk) {
        return claimChunk.getMessages().noPluginPerm;
    }

    @Override
    public Argument[] getPermittedArguments(ClaimChunk claimChunk) {
        return new Argument[] {new Argument("command", Argument.TabCompletion.COMMAND)};
    }

    @Override
    public int getRequiredArguments(ClaimChunk claimChunk) {
        return 0;
    }

    @Override
    public boolean onCall(ClaimChunk claimChunk, String cmdUsed, Player executor, String[] args) {
        if (args.length == 0) {
            // Display the help command header
            Utils.msg(executor, claimChunk.getMessages().helpHeader);

            // List all the commands
            for (ICommand cmd : claimChunk.getCommandHandler().getCmds()) {
                // Only show commands that the user has permission to use
                if (cmd.getShouldDisplayInHelp(claimChunk, executor)) {
                    // Display this command's help
                    displayCommand(claimChunk, cmdUsed, executor, cmd);
                }
            }
        } else {
            // Get the command
            ICommand cmd = claimChunk.getCommandHandler().getCommand(args[0]);
            if (cmd == null) {
                // Display the command wasn't found
                Utils.msg(
                        executor,
                        claimChunk
                                .getMessages()
                                .helpCmdNotFound
                                .replace("%%USED%%", cmdUsed)
                                .replace("%%CMD%%", args[0]));
            } else {
                // Display the command's help header
                Utils.msg(
                        executor,
                        claimChunk
                                .getMessages()
                                .helpCmdHeader
                                .replace("%%USED%%", cmdUsed)
                                .replace("%%CMD%%", cmd.getCommand(claimChunk)));

                // Display the command's help
                displayCommand(claimChunk, cmdUsed, executor, cmd);
            }
        }
        return true;
    }

    private void displayCommand(
            ClaimChunk claimChunk, String cmdUsed, Player executor, ICommand cmd) {
        // Create the display string
        String out =
                claimChunk
                        .getMessages()
                        .helpCmd
                        .replace("%%USED%%", cmdUsed)
                        .replace("%%CMD%%", cmd.getCommand(claimChunk))
                        .replace("%%ARGS%%", claimChunk.getCommandHandler().getUsageArgs(cmd))
                        .replace("%%DESC%%", cmd.getDescription(claimChunk));

        // Display the string
        Utils.msg(executor, out);
    }
}
