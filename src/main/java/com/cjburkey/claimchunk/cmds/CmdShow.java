package com.cjburkey.claimchunk.cmds;

import org.bukkit.entity.Player;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.cmd.Argument;
import com.cjburkey.claimchunk.cmd.ICommand;

public class CmdShow implements ICommand {

    public String getCommand() {
        return "show";
    }

    public String getDescription() {
        return "Outline the chunk you're standing in with particles.";
    }

    public Argument[] getPermittedArguments() {
        return new Argument[] { new Argument("seconds", Argument.TabCompletion.NONE) };
    }

    public int getRequiredArguments() {
        return 0;
    }

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