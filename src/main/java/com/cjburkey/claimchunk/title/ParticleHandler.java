package com.cjburkey.claimchunk.title;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class ParticleHandler {
	
	public static void spawnFlameParticle(Location loc, Player... forPlayer) {
		try {
			spawnParticle(loc, forPlayer, "FLAME");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void spawnParticle(Location loc, Player[] players, String particle) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, NoSuchFieldException, ClassNotFoundException, InstantiationException {
		Class<?> particleClass = PacketHandler.getNMSClass("PacketPlayOutWorldParticles");
		Class<?> particleEnum = PacketHandler.getNMSClass("EnumParticle");
		
		Class<?>[] parameters = new Class<?>[] {
			particleEnum, boolean.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class, int.class, int[].class
		};
		Object[] arguments = new Object[] {
				particleEnum.getField(particle).get(null), true, (float) loc.getX(), (float) loc.getY(), (float) loc.getZ(), 0f, 0f, 0f, 1f, 0, new int[0]
		};
		
		Constructor<?> constructor = particleClass.getConstructor(parameters);
		Object packet = constructor.newInstance(arguments);
		
		for (Player p : players) {
			PacketHandler.sendPacket(p, packet);
		}
	}
	
}