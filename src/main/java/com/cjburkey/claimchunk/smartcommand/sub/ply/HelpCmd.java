package com.cjburkey.claimchunk.smartcommand.sub.ply;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.smartcommand.CCSubCommand;
import com.cjburkey.claimchunk.smartcommand.ClaimChunkBaseCommand;

import de.goldmensch.commanddispatcher.Executor;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @since 0.0.23
 */
public class HelpCmd extends CCSubCommand {

    private final ClaimChunkBaseCommand baseCommand;

    public HelpCmd(ClaimChunk claimChunk, ClaimChunkBaseCommand baseCommand) {
        // TODO: MAKE ACCESSIBLE FROM CONSOLE
        super(claimChunk, Executor.CONSOLE_PLAYER, true, "player", "help");

        this.baseCommand = baseCommand;
    }

    @Override
    public @Nullable String getDescription() {
        return claimChunk.getMessages().cmdHelp;
    }

    @Override
    public CCArg[] getPermittedArguments() {
        return new CCArg[] {new CCArg(claimChunk.getMessages().argCmd, CCAutoComplete.NONE)};
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
                    messageChat(player, getCommandDisplayStr(cmdUsed, cmd));
                }
            }
        } else {
            // Get the command
            var cmd = baseCommand.getSubCmd(args);
            Utils.log("Args: %s", String.join(", ", args));
            if (cmd.isEmpty()) {
                // Display the command wasn't found
                messageChat(
                        player,
                        claimChunk
                                .getMessages()
                                .helpCmdNotFound
                                .replace("%%USED%%", cmdUsed)
                                .replace("%%CMD%%", String.join(" ", args)));
            } else {
                var ccmd = cmd.get();

                // Display the command's help header
                messageChat(
                        player,
                        claimChunk
                                .getMessages()
                                .helpCmdHeader
                                .replace("%%USED%%", cmdUsed)
                                .replace("%%CMD%%", ccmd.getName()));

                // Display the command's help
                messageChat(player, getCommandDisplayStr(cmdUsed, ccmd));
            }
        }
        return true;
    }

    private @NotNull String getCommandDisplayStr(String cmdUsed, CCSubCommand cmd) {
        @Nullable String desc = cmd.getDescription();

        // Create the display string
        return claimChunk
                .getMessages()
                .helpCmd
                .replace("%%USED%%", cmdUsed)
                .replace("%%CMD%%", cmd.getName())
                .replace("%%ARGS%%", cmd.getUsageArgs())
                .replace(
                        "%%DESC%%",
                        desc == null
                                ? "No description! Oops! Let me know about this please :)"
                                : desc);
    }
}
