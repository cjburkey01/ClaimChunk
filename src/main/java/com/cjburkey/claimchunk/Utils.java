package com.cjburkey.claimchunk;

import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import com.cjburkey.claimchunk.title.TitleHandler;

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
	
	public static ChatColor getConfigColor(String path) {
		return ChatColor.valueOf(ClaimChunk.getInstance().getConfig().getString(path));
	}
	
	public static String getLang(String key) {
		String l = "lang" + key;
		String out = ClaimChunk.getInstance().getConfig().getString(l);
		if(out == null) {
			return l;
		}
		return out;
	}
	
	public static void msg(CommandSender to, String msg) {
		to.sendMessage(color(msg));
	}
	
	public static void toPlayer(Player ply, ChatColor color, String msg) {
		if (ClaimChunk.getInstance().getConfig().getBoolean("useTitlesInsteadOfChat")) {
			try {
				FileConfiguration cfg = ClaimChunk.getInstance().getConfig();
				TitleHandler.showTitle(ply, "", ChatColor.BLACK, 20, 140, 20);
				TitleHandler.showSubTitle(ply, msg, color, cfg.getInt("titleFadeInTime"), cfg.getInt("titleStayTime"), cfg.getInt("titleFadeOutTime"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			msg(ply, color + msg);
		}
	}
	
	public static boolean hasPerm(CommandSender sender, String perm) {
		return ClaimChunk.getInstance().getPermission().has(sender, perm);
	}
	
	private static String prepMsg(Object msg) {
		String out = (msg == null) ? "null" : msg.toString();
		return String.format("[%s] %s", ClaimChunk.getInstance().getDescription().getPrefix(), color(out));
	}
	
}