package com.cjburkey.claimchunk.cmds;

import org.bukkit.entity.Player;
import com.cjburkey.claimchunk.Utils;
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
		return new Argument[] {  };
	}

	public int getRequiredArguments() {
		return 0;
	}

	public void onCall(Player executor, String[] args) {
		ChunkPos p = new ChunkPos(executor.getLocation().getChunk());
		p.outlineChunk(executor);
		Utils.log("Outlining " + p.toString());
	}
	
}