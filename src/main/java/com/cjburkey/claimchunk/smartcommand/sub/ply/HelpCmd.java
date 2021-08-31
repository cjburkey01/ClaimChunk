package com.cjburkey.claimchunk.smartcommand.sub.ply;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.smartcommand.CCSubCommand;
import com.cjburkey.claimchunk.smartcommand.ClaimChunkBaseCommand;

import de.goldmensch.commanddispatcher.ExecutorLevel;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/** @since 0.0.23 */
public class HelpCmd extends CCSubCommand {

    private final ClaimChunkBaseCommand baseCommand;

    public HelpCmd(ClaimChunk claimChunk, ClaimChunkBaseCommand baseCommand) {
        // TODO: MAKE ACCESSIBLE FROM CONSOLE
        super(claimChunk, ExecutorLevel.CONSOLE_PLAYER);

        this.baseCommand = baseCommand;
    }

    @Override
    public String getDescription() {
        return claimChunk.getMessages().cmdHelp;
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return Utils.hasPerm(sender, true, "base");
    }

    @Override
    public @NotNull String getPermissionMessage() {
        return claimChunk.getMessages().noPluginPerm;
    }

    @Override
    public CCArg[] getPermittedArguments() {
        return new CCArg[] {new CCArg("command...", CCAutoComplete.NONE)};
    }

    @Override
    public int getMaxArguments() {
        // Random high number
        return 100;
    }

    @Override
    public int getRequiredArguments() {
        return 0;
    }

    @Override
    public boolean onCall(@NotNull String cmdUsed, @NotNull CommandSender player, String[] args) {
        if (args.length == 0) {
            // Display the help command header
            messageChat(player, claimChunk.getMessages().helpHeader);

            // List all the commands
            for (var cmd : baseCommand.getCmds()) {
                // Only show commands that the user has permission to use
                if (cmd.getShouldDisplayInHelp(player)) {
                    // Display this command's help
                    getCommandDisplayStr(cmdUsed, cmd);
                }
            }
        } else {
            // Get the command
            var cmd = baseCommand.getCmd(args);
            if (cmd == null) {
                // Display the command wasn't found
                messageChat(
                        player,
                        claimChunk
                                .getMessages()
                                .helpCmdNotFound
                                .replace("%%USED%%", cmdUsed)
                                .replace("%%CMD%%", args[0]));
            } else {
                // Display the command's help header
                messageChat(
                        player,
                        claimChunk
                                .getMessages()
                                .helpCmdHeader
                                .replace("%%USED%%", cmdUsed)
                                .replace("%%CMD%%", cmd.getName()));

                // Display the command's help
                messageChat(player, getCommandDisplayStr(cmdUsed, cmd));
            }
        }
        return true;
    }

    private @NotNull String getCommandDisplayStr(String cmdUsed, CCSubCommand cmd) {
        // Create the display string
        return claimChunk
                .getMessages()
                .helpCmd
                .replace("%%USED%%", cmdUsed)
                .replace("%%CMD%%", cmd.getName())
                .replace("%%ARGS%%", cmd.getUsageArgs())
                .replace("%%DESC%%", cmd.getDescription());
    }
}
