package com.cjburkey.claimchunk.cmds;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.cmd.Argument;
import com.cjburkey.claimchunk.cmd.ICommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CmdHelp implements ICommand {

    @Override
    public String getCommand() {
        return "help";
    }

    @Override
    public String getDescription() {
        return ClaimChunk.getInstance().getMessages().cmdHelp;
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return Utils.hasPerm(sender, true, "base");
    }

    public String getPermissionMessage() {
        return ClaimChunk.getInstance().getMessages().noPluginPerm;
    }

    @Override
    public Argument[] getPermittedArguments() {
        return new Argument[] {new Argument("command", Argument.TabCompletion.COMMAND)};
    }

    @Override
    public int getRequiredArguments() {
        return 0;
    }

    @Override
    public boolean onCall(String cmdUsed, Player executor, String[] args) {
        if (args.length == 0) {
            // Display the help command header
            Utils.msg(executor, ClaimChunk.getInstance().getMessages().helpHeader);

            // List all the commands
            for (ICommand cmd : ClaimChunk.getInstance().getCommandHandler().getCmds()) {
                // Only show commands that the user has permission to use
                if (cmd.getShouldDisplayInHelp(executor)) {
                    // Display this command's help
                    displayCommand(cmdUsed, executor, cmd);
                }
            }
        } else {
            // Get the command
            ICommand cmd = ClaimChunk.getInstance().getCommandHandler().getCommand(args[0]);
            if (cmd == null) {
                // Display the command wasn't found
                Utils.msg(executor,
                        ClaimChunk.getInstance().getMessages().helpCmdNotFound
                                .replaceAll("%%USED%%", cmdUsed)
                                .replaceAll("%%CMD%%", args[0]));
            } else {
                // Display the command's help header
                Utils.msg(executor, ClaimChunk.getInstance().getMessages().helpCmdHeader
                        .replaceAll("%%USED%%", cmdUsed)
                        .replaceAll("%%CMD%%", cmd.getCommand()));

                // Display the command's help
                displayCommand(cmdUsed, executor, cmd);
            }
        }
        return true;
    }

    private void displayCommand(String cmdUsed, Player executor, ICommand cmd) {
        // Create the display string
        String out = ClaimChunk.getInstance().getMessages().helpCmd
                .replaceAll("%%USED%%", cmdUsed)
                .replaceAll("%%CMD%%", cmd.getCommand())
                .replaceAll("%%ARGS%%", ClaimChunk.getInstance().getCommandHandler().getUsageArgs(cmd))
                .replaceAll("%%DESC%%", cmd.getDescription());

        // Display the string
        Utils.msg(executor, out);
    }

}
