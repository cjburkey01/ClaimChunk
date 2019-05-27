package com.cjburkey.claimchunk.packet;

import com.cjburkey.claimchunk.Utils;
import java.lang.reflect.Constructor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Class to handle title packets using reflection. Hopefully version
 * independent.
 *
 * @author bramhaag
 */
public final class TitleHandler {

    private static final String jsonFormat = "{\"text\":\"%s\",\"color\":\"%s\"}";

    /**
     * Sends a title to the player.
     *
     * @param player       The player to whom send the title.
     * @param text         The text to display.
     * @param color        The color.
     * @param fadeInTicks  The fade in time in ticks.
     * @param stayTicks    The stay time in ticks.
     * @param fadeOutTicks The fade out time in ticks.
     * @throws Exception Reflection error.
     */
    public static void showTitle(Player player, String text, ChatColor color, int fadeInTicks, int stayTicks,
                                 int fadeOutTicks) throws Exception {
        showTitle(player, text, color, fadeInTicks, stayTicks, fadeOutTicks, "TITLE");
    }

    /**
     * Sends a subtitle to the player.
     *
     * @param player       The player to whom send the subtitle.
     * @param text         The text to display.
     * @param color        The color.
     * @param fadeInTicks  The fade in time in ticks.
     * @param stayTicks    The stay time in ticks.
     * @param fadeOutTicks The fade out time in ticks.
     * @throws Exception Reflection error.
     */
    public static void showSubTitle(Player player, String text, ChatColor color, int fadeInTicks, int stayTicks,
                                    int fadeOutTicks) throws Exception {
        showTitle(player, text, color, fadeInTicks, stayTicks, fadeOutTicks, "SUBTITLE");
    }

    /**
     * Sends an actionbar title to the player.
     *
     * @param player       The player to whom send the actionbar title.
     * @param text         The text to display.
     * @param color        The color.
     * @param fadeInTicks  The fade in time in ticks.
     * @param stayTicks    The stay time in ticks.
     * @param fadeOutTicks The fade out time in ticks.
     * @throws Exception Reflection error.
     */
    public static void showActionbarTitle(Player player, String text, ChatColor color, int fadeInTicks, int stayTicks,
                                          int fadeOutTicks) throws Exception {
        // This may fail if the server is running a version that doesn't support action bars (like 1.10)
        // In such a case, unless the action was to clear the action bar, the message will be displyed in the subtitle slot
        //  and a message logged in the console.
        try {
            showTitle(player, text, color, fadeInTicks, stayTicks, fadeOutTicks, "ACTIONBAR");
        } catch (Exception ignored) {
            if (!text.trim().isEmpty()) {
                showSubTitle(player, text, color, fadeInTicks, stayTicks, fadeOutTicks);
                Utils.err("Error: This server is running a version that does not support actionbars. Please display the 'useActionBar' config option under the 'titles' section in the config file.");
            }
        }
    }

    // Some pretty volatile code here, but if it works? idc.
    private static void showTitle(Player player, String text, ChatColor color, int fadeInTicks, int stayTicks,
                                  int fadeOutTicks, String show) throws Exception {
        Object chatTitle = PacketHandler.getNMSClass("IChatBaseComponent").getDeclaredClasses()[0]
                .getMethod("a", String.class).invoke(null, String.format(jsonFormat, text, color.name().toLowerCase()));
        Constructor<?> titleConstructor = PacketHandler.getNMSClass("PacketPlayOutTitle").getConstructor(
                PacketHandler.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0],
                PacketHandler.getNMSClass("IChatBaseComponent"), int.class, int.class, int.class);
        Object packet = titleConstructor.newInstance(
                PacketHandler.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField(show).get(null),
                chatTitle, fadeInTicks, stayTicks, fadeOutTicks);
        PacketHandler.sendPacket(player, packet);
    }

}
