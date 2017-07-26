package com.cjburkey.claimchunk.title;

import java.lang.reflect.InvocationTargetException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PacketHandler {
	
	public static void sendPacket(Player player, Object packet) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, NoSuchFieldException, ClassNotFoundException {
		Object handle = player.getClass().getMethod("getHandle").invoke(player);
		Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
		playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
	}
	
	public static Class<?> getNMSClass(String name) throws ClassNotFoundException {
		return Class.forName("net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + "." + name);
	}
	
}