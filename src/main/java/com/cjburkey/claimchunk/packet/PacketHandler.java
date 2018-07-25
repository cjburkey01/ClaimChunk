package com.cjburkey.claimchunk.packet;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Reflection for packet handler.
 * 
 * @author cjburkey
 */
public class PacketHandler {

    /**
     * Sends the packet (packet) to (player)
     * 
     * @param player
     *            The player for whom to send the packet.
     * @param packet
     *            The packet to send.
     * @throws Exception
     *             Reflection error.
     */
    public static void sendPacket(Player player, Object packet) throws Exception {
        Object handle = player.getClass().getMethod("getHandle").invoke(player);
        Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
        playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
    }

    /**
     * Fetches a class for NMS using reflection.
     * 
     * @param name
     *            The name of the class.
     * @return The class
     * @throws ClassNotFoundException
     *             Class could not be found.
     */
    public static Class<?> getNMSClass(String name) throws ClassNotFoundException {
        return Class.forName("net.minecraft.server."
                + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + "." + name);
    }

}