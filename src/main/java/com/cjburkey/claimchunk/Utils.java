package com.cjburkey.claimchunk;

import java.util.logging.Logger;
import org.bukkit.command.CommandSender;
import net.md_5.bungee.api.ChatColor;

public final class Utils {
	
	private static final Logger log = Logger.getLogger("Minecraft");
	
	public static void log(Object msg) {
		log.info(prepMsg(msg));
	}
	
	public static void err(Object msg) {
		log.severe(prepMsg(msg));
	}
	
	public static String color(String in) {
		return ChatColor.translateAlternateColorCodes('&', in);
	}
	
	public static void msg(CommandSender to, String msg) {
		to.sendMessage(color(msg));
	}
	
	private static String prepMsg(Object msg) {
		String out = (msg == null) ? "null" : msg.toString();
		return String.format("[%s] %s", ClaimChunk.getInstance().getDescription().getPrefix(), color(out));
	}
	
}