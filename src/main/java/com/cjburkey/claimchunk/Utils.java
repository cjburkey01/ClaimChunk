package com.cjburkey.claimchunk;

import com.cjburkey.claimchunk.placeholder.ClaimChunkPlaceholders;
import lombok.Getter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;

public final class Utils {

    private static final Logger log = Logger.getLogger("Minecraft");

    private static ClaimChunk claimChunk;
    @Getter static boolean debugEnableOverride = false;

    static void init(ClaimChunk claimChunk) {
        Utils.claimChunk = claimChunk;
    }

    static void overrideDebugEnable() {
        debugEnableOverride = true;
    }

    static void overrideDebugDisable() {
        debugEnableOverride = false;
    }

    public static void log(String msg, Object... data) {
        log.info(prepMsg(msg, data));
    }

    public static void debug(String msg, Object... data) {
        if (debugEnableOverride
                || claimChunk != null
                        && claimChunk.getConfigHandler() != null
                        && claimChunk.getConfigHandler().getDebugSpam()) {
            log.info(prepMsg("[DEBUG] " + msg, data));
        }
    }

    public static void err(String msg, Object... data) {
        log.severe(prepMsg(msg, data));
    }

    public static void warn(String msg, Object... data) {
        log.warning(prepMsg(msg, data));
    }

    public static int clamp(int val, int min, int max) {
        return Math.max(Math.min(val, max), min);
    }

    public static String color(String in) {
        return ChatColor.translateAlternateColorCodes('&', in);
    }

    @SuppressWarnings("deprecation")
    public static BaseComponent toComponent(@Nullable CommandSender sender, String input) {
        var placeholders = claimChunk.getPlaceholderLayer().getPlaceholders();
        var str =
                placeholders == null
                        ? input
                        : ClaimChunkPlaceholders.fillPlaceholders(sender, input);
        // REVERT TO THIS! PAPER ISN'T PLAYING NICE!
        return new TextComponent(TextComponent.fromLegacyText(color(str)));
    }

    public static void msg(CommandSender to, BaseComponent msg) {
        to.spigot().sendMessage(msg);
    }

    public static void msg(CommandSender to, String text) {
        msg(to, toComponent(to, text));
    }

    public static void toPlayer(@NotNull Player ply, @NotNull BaseComponent msg) {
        if (claimChunk.getConfigHandler().getUseTitlesInsteadOfChat()) {
            // Use titles
            try {
                // Title configs
                int in = claimChunk.getConfigHandler().getTitleFadeInTime();
                int stay = claimChunk.getConfigHandler().getTitleStayTime();
                int out = claimChunk.getConfigHandler().getTitleFadeOutTime();

                // Make the big title empty
                // TitleHandler.showTitle(ply, new TextComponent(""), in, stay, out);
                if (claimChunk.getConfigHandler().getUseActionBar()) {
                    // Show the message in the action bar
                    ply.spigot().sendMessage(ChatMessageType.ACTION_BAR, msg);
                } else {
                    // Show the message in the subtitle (bigger but less room)
                    ply.sendTitle(" ", msg.toLegacyText(), in, stay, out);
                }
            } catch (Exception e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();

                // An error occurred, use chat
                msg(ply, msg);
            }
        } else {
            // Use chat
            msg(ply, msg);
        }
    }

    public static void toPlayer(Player ply, String text) {
        if (!text.isBlank()) {
            toPlayer(ply, toComponent(ply, text));
        }
    }

    /**
     * Check if a command sender has a given permission. Note: If the sender has {@code
     * claimchunk.admin}, they will have all permissions.
     *
     * @param sender The given command sender (player, console, etc).
     * @param basic Whether {@code claimchunk.player} should also grant this permission.
     * @param perm The string for the permission node.
     * @return A boolean representing whether the sender has this permission.
     */
    public static boolean hasPerm(@Nullable CommandSender sender, boolean basic, String perm) {
        if (sender == null) return false;

        // Ops can do everything
        if (sender.isOp()) return true;

        // If permissions are disabled, the user will have this command if it's a "basic" command
        if (claimChunk.getConfigHandler().getDisablePermissions()) {
            return basic;
        }

        // If `claimchunk.player` is used, then the player will be able to use this command if it's
        // a
        // "basic" command
        if (basic && sender.hasPermission("claimchunk.player")) {
            return true;
        }

        // Check permission
        return sender.hasPermission("claimchunk." + perm);
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
        return String.format(
                "[%s] %s",
                claimChunk.getDescription().getPrefix(), color(String.format(out, data)));
    }

    public static Map<String, Boolean> getDefaultPermissionsMap() {
        // Default permissions for players with access to a chunk
        // Used to provide the values for any permissions not specified when adding a new player to
        // a chunk
        HashMap<String, Boolean> defaultPermissions = new HashMap<>();

        defaultPermissions.put("break", true);
        defaultPermissions.put("place", true);
        defaultPermissions.put("doors", true);
        defaultPermissions.put("redstone", true);
        defaultPermissions.put("interactVehicles", true);
        defaultPermissions.put("interactEntities", true);
        defaultPermissions.put("interactBlocks", true);
        defaultPermissions.put("useContainers", true);

        return defaultPermissions;
    }

    public static Map<String, Boolean> getAllFalsePermissionsMap() {
        // Map of permissions with all set to false
        Map<String, Boolean> permissionsMap = getDefaultPermissionsMap();
        permissionsMap.replaceAll((p, v) -> false);
        return permissionsMap;
    }

    // Should prevent the old enum values from mucking everything up :)
    public static @Nullable Material materialFromString(String input) {
        // First, try to match based on the new enum values
        Material matchedEnum = Material.matchMaterial(input);
        if (matchedEnum != null) {
            return matchedEnum;
        }

        // Otherwise, try the legacy as a fallback
        return Material.matchMaterial(input, true);
    }

    // -- JAVA UTIL -- //

    public static <K, V> HashMap<K, V> deepCloneMap(HashMap<K, V> map, Function<V, V> cloneFunc) {
        return map.entrySet().stream()
                .map(
                        entry ->
                                new AbstractMap.SimpleEntry<>(
                                        entry.getKey(), cloneFunc.apply(entry.getValue())))
                .collect(
                        HashMap::new,
                        (m, entry) -> m.put(entry.getKey(), entry.getValue()),
                        HashMap::putAll);
    }
}
