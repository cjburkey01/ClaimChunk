package com.cjburkey.claimchunk.cmds;

import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.cmd.Argument;
import com.cjburkey.claimchunk.cmd.ICommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CmdShow implements ICommand {

    @Override
    public String getCommand() {
        return "show";
    }

    @Override
    public String getDescription() {
        return "Outline the chunk you're standing in with particles.";
    }

    @Override
    public boolean getShouldDisplayInHelp(CommandSender sender) {
        return Utils.hasPerm(sender, true, "base");
    }

    @Override
    public Argument[] getPermittedArguments() {
        return new Argument[] {new Argument("seconds", Argument.TabCompletion.NONE)};
    }

    @Override
    public int getRequiredArguments() {
        return 0;
    }

    @Override
    public boolean onCall(Player executor, String[] args) {
        ChunkPos p = new ChunkPos(executor.getLocation().getChunk());
        int time = 5;
        if (args.length == 1) {
            try {
                time = Integer.parseInt(args[0]);
            } catch (Exception e) {
                return false;
            }
        }
        p.outlineChunk(executor, time);
        return true;
    }

}
