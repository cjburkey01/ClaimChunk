package com.cjburkey.claimchunk;

import com.cjburkey.claimchunk.packet.TitleHandler;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

public final class Utils {

    private static final Logger log = Logger.getLogger("Minecraft");

    @SuppressWarnings("WeakerAccess")
    public static void log(String msg, Object... data) {
        log.info(prepMsg(msg, data));
    }

    public static void debug(String msg, Object... data) {
        if (Config.getBool("log", "debugSpam")) log.info(prepMsg(msg, data));
    }

    public static void err(String msg, Object... data) {
        log.severe(prepMsg(msg, data));
    }

    public static String color(String in) {
        return ChatColor.translateAlternateColorCodes('&', in);
    }

    public static int clamp(int val, int min, int max) {
        return Math.max(Math.min(val, max), min);
    }

    public static void msg(CommandSender to, String msg) {
        to.sendMessage(color(msg));
    }

    public static void toPlayer(Player ply, String msg) {
        if (Config.getBool("titles", "useTitlesInsteadOfChat")) {
            try {
                int in = Config.getInt("titles", "titleFadeInTime");
                int stay = Config.getInt("titles", "titleStayTime");
                int out = Config.getInt("titles", "titleFadeOutTime");

                TitleHandler.showTitle(ply, "", in, stay, out);
                if (Config.getBool("titles", "useActionBar")) {
                    TitleHandler.showActionbarTitle(ply, msg, in, stay, out);
                    TitleHandler.showSubTitle(ply, "", in, stay, out);
                } else {
                    TitleHandler.showActionbarTitle(ply, "", in, stay, out);
                    TitleHandler.showSubTitle(ply, msg, in, stay, out);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            msg(ply, msg);
        }
    }

    // Methods like these make me wish we had macros in Java
    public static boolean hasPerm(@Nullable CommandSender sender, boolean basic, String perm) {
        if (sender == null) return false;
        if (Config.getBool("basic", "disablePermissions")) {
            return basic;
        }
        if (basic && sender.hasPermission("claimchunk.player")) {
            return true;
        }
        return sender.hasPermission("claimchunk." + perm);
    }

    // Methods like these make me wish we had macros in Java
    public static boolean hasPerm(CommandSender sender, boolean basic, Permission perm) {
        if (sender == null) return false;
        if (Config.getBool("basic", "disablePermissions")) {
            return basic;
        }
        if (basic && sender.hasPermission("claimchunk.player")) {
            return true;
        }
        return sender.hasPermission(perm);
    }

    private static String prepMsg(String msg, Object... data) {
        String out = (msg == null) ? "null" : msg;
        return String.format("[%s] %s", ClaimChunk.getInstance().getDescription().getPrefix(), color(String.format(out, data)));
    }

}
