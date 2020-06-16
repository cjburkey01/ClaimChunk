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
            // Use titles
            try {
                // Title configs
                int in = Config.getInt("titles", "titleFadeInTime");
                int stay = Config.getInt("titles", "titleStayTime");
                int out = Config.getInt("titles", "titleFadeOutTime");

                // Make the big title empty
                TitleHandler.showTitle(ply, "", in, stay, out);
                if (Config.getBool("titles", "useActionBar")) {
                    // Show the message in the action bar
                    TitleHandler.showActionbarTitle(ply, msg, in, stay, out);
                    TitleHandler.showSubTitle(ply, "", in, stay, out);
                } else {
                    // Show the message in the sub title (bigger but less room)
                    TitleHandler.showActionbarTitle(ply, "", in, stay, out);
                    TitleHandler.showSubTitle(ply, msg, in, stay, out);
                }
            } catch (Exception e) {
                e.printStackTrace();

                // An error occurred, use chat
                msg(ply, msg);
            }
        } else {
            // Use chat
            msg(ply, msg);
        }
    }

    // Methods like these make me wish we had macros in Java
    public static boolean hasPerm(@Nullable CommandSender sender, boolean basic, String perm) {
        if (sender == null) return false;

        // Ops can do everything
        if (sender.isOp()) return true;

        // If permissions are disabled, the user will have this command if it's a "basic" command
        if (Config.getBool("basic", "disablePermissions")) {
            return basic;
        }

        // If `claimchunk.player` is used, then the player will be able to use this command if it's a "basic" command
        if (basic && sender.hasPermission("claimchunk.player")) {
            return true;
        }

        // Check permission
        return sender.hasPermission("claimchunk." + perm);
    }

    // Methods like these make me wish we had macros in Java
    public static boolean hasPerm(CommandSender sender, boolean basic, Permission perm) {
        if (sender == null) return false;

        // Ops can do everything
        if (sender.isOp()) return true;

        // If permissions are disabled, the user will have this command if it's a "basic" command
        if (Config.getBool("basic", "disablePermissions")) {
            return basic;
        }

        // If `claimchunk.player` is used, then the player will be able to use this command if it's a "basic" command
        if (basic && sender.hasPermission("claimchunk.player")) {
            return true;
        }

        // Check permission
        return sender.hasPermission(perm);
    }

    public static boolean hasAdmin(@Nullable CommandSender sender) {
        // Check if the user has the admin permission
        // This is just a shortcut
        return sender != null && (sender.isOp() || hasPerm(sender, false, "admin"));
    }

    private static String prepMsg(String msg, Object... data) {
        // Prepare a safe console message
        String out = (msg == null) ? "null" : msg;

        // Output with the ClaimChunk prefix
        return String.format("[%s] %s", ClaimChunk.getInstance().getDescription().getPrefix(), color(String.format(out, data)));
    }

}
