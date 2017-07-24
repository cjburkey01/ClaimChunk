package com.cjburkey.claimchunk.title;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Class to handle title packets using reflection.
 * Hopefully version independent.
 * @author bramhaag
 * @author cjburkey
 */
public final class TitleHandler {
	
	private static final String jsonFormat = "{\"text\":\"%s\",\"color\":\"%s\"}";
	
	/**
	 * Sends a title to the player.
	 * @param player The player to whom send the title.
	 * @param text The text to display.
	 * @param color The color.
	 * @param fadeInTicks The fade in time in ticks.
	 * @param stayTicks The stay time in ticks.
	 * @param fadeOutTicks The fade out time in ticks.
	 * @throws Exception Reflection error.
	 */
	public static void showTitle(Player player, String text, ChatColor color, int fadeInTicks, int stayTicks, int fadeOutTicks) throws Exception {
		showTitle(player, text, color, fadeInTicks, stayTicks, fadeOutTicks, "TITLE");
	}
	
	/**
	 * Sends a subtitle to the player.
	 * @param player The player to whom send the subtitle.
	 * @param text The text to display.
	 * @param color The color.
	 * @param fadeInTicks The fade in time in ticks.
	 * @param stayTicks The stay time in ticks.
	 * @param fadeOutTicks The fade out time in ticks.
	 * @throws Exception Reflection error.
	 */
	public static void showSubTitle(Player player, String text, ChatColor color, int fadeInTicks, int stayTicks, int fadeOutTicks) throws Exception {
		showTitle(player, text, color, fadeInTicks, stayTicks, fadeOutTicks, "SUBTITLE");
	}
	
	/**
	 * Sends an actionbar title to the player.
	 * @param player The player to whom send the actionbar title.
	 * @param text The text to display.
	 * @param color The color.
	 * @param fadeInTicks The fade in time in ticks.
	 * @param stayTicks The stay time in ticks.
	 * @param fadeOutTicks The fade out time in ticks.
	 * @throws Exception Reflection error.
	 */
	public static void showActionbarTitle(Player player, String text, ChatColor color, int fadeInTicks, int stayTicks, int fadeOutTicks) throws Exception {
		showTitle(player, text, color, fadeInTicks, stayTicks, fadeOutTicks, "ACTIONBAR");
	}
	
	// Some pretty volatile code here, but if it works?
	
	private static void showTitle(Player player, String text, ChatColor color, int fadeInTicks, int stayTicks, int fadeOutTicks, String show) throws Exception {
		Object chatTitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, String.format(jsonFormat, text, color.name().toLowerCase()));
		Constructor<?> titleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"), int.class, int.class, int.class);
		Object packet = titleConstructor.newInstance(getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField(show).get(null), chatTitle, fadeInTicks, stayTicks, fadeOutTicks);
		sendPacket(player, packet);
	}
	
	private static void sendPacket(Player player, Object packet) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, NoSuchFieldException, ClassNotFoundException {
		Object handle = player.getClass().getMethod("getHandle").invoke(player);
		Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
		playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
	}
	
	private static Class<?> getNMSClass(String name) throws ClassNotFoundException {
		return Class.forName("net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + "." + name);
	}
	
}