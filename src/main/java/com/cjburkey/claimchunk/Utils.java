package com.cjburkey.claimchunk;

import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.cjburkey.claimchunk.packet.TitleHandler;

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

    public static String getMsg(String key) {
        String out = Config.getString("messages", key);
        if (out == null) {
            return "messages." + out;
        }
        return out;
    }

    public static int clamp(int val, int min, int max) {
        if (val > max)
            return max;
        if (val < min)
            return min;
        return val;
    }

    public static void msg(CommandSender to, String msg) {
        to.sendMessage(color(msg));
    }

    public static void toPlayer(Player ply, ChatColor color, String msg) {
        if (Config.getBool("titles", "useTitlesInsteadOfChat")) {
            try {
                int in = Config.getInt("titles", "titleFadeInTime");
                int stay = Config.getInt("titles", "titleStayTime");
                int out = Config.getInt("titles", "titleFadeOutTime");
                TitleHandler.showTitle(ply, "", ChatColor.BLACK, in, stay, out);
                TitleHandler.showSubTitle(ply, msg, color, in, stay, out);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            msg(ply, color + msg);
        }
    }

    public static boolean hasPerm(CommandSender sender, String perm) {
        return sender.hasPermission(perm);
    }

    private static String prepMsg(Object msg) {
        String out = (msg == null) ? "null" : msg.toString();
        return String.format("[%s] %s", ClaimChunk.getInstance().getDescription().getPrefix(), color(out));
    }

}