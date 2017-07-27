package com.cjburkey.claimchunk.tab;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.cmd.Argument;
import com.cjburkey.claimchunk.cmd.ICommand;

public class AutoTabCompletion implements TabCompleter {

	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (args.length < 1) {
			return getCommands("");
		}
		if (args.length == 1) {
			return getCommands(args[0]);
		}
		ICommand cmd = ClaimChunk.getInstance().getCommandHandler().getCommand(args[0]);
		int cmdArg = args.length - 2;
		if (cmdArg < cmd.getPermittedArguments().length) {
			Argument arg = cmd.getPermittedArguments()[cmdArg];
			switch (arg.getCompletion()) {
			case NONE:
				return new ArrayList<>();
			case COMMAND:
				return getCommands(args[args.length - 1]);
			case ONLINE_PLAYER:
				return getOnlinePlayers(args[args.length - 1]);
			case OFFLINE_PLAYER:
				return getOfflinePlayers(args[args.length - 1]);
			default:
				return new ArrayList<>();
			}
		}
		return new ArrayList<>();
	}
	
	private List<String> getOnlinePlayers(String starts) {
		List<String> out = new ArrayList<>();
		for (Player p : ClaimChunk.getInstance().getServer().getOnlinePlayers()) {
			String add = p.getName();
			if (add.toLowerCase().startsWith(starts.toLowerCase())) {
				out.add(p.getName());
			}
		}
		return out;
	}
	
	private List<String> getCommands(String starts) {
		List<String> out = new ArrayList<>();
		for (ICommand cmd : ClaimChunk.getInstance().getCommandHandler().getCmds()) {
			String add = cmd.getCommand();
			if (add.toLowerCase().startsWith(starts.toLowerCase())) {
				out.add(cmd.getCommand());
			}
		}
		return out;
	}
	
	private List<String> getOfflinePlayers(String starts) {
		return ClaimChunk.getInstance().getPlayers().getJoined(starts);
	}
	
}