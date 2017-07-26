package com.cjburkey.claimchunk.packet;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class ParticleHandler {
	
	public static void spawnParticleForPlayers(Location loc, Particles particle, Player... forPlayer) {
		try {
			spawnParticle(loc, forPlayer, particle);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void spawnParticle(Location loc, Player[] players, Particles particle) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, NoSuchFieldException, ClassNotFoundException, InstantiationException {
		Class<?> particleClass = PacketHandler.getNMSClass("PacketPlayOutWorldParticles");
		Class<?> particleEnum = PacketHandler.getNMSClass("EnumParticle");
		
		Class<?>[] parameters = new Class<?>[] {
			particleEnum, boolean.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class, int.class, int[].class
		};
		Object[] arguments = new Object[] {
			particleEnum.getField(particle.name()).get(null), true, (float) loc.getX(), (float) loc.getY(), (float) loc.getZ(), 0f, 0f, 0f, 1f, 0, new int[0]
		};
		
		Constructor<?> constructor = particleClass.getConstructor(parameters);
		Object packet = constructor.newInstance(arguments);
		
		for (Player p : players) {
			PacketHandler.sendPacket(p, packet);
		}
	}
	
	public static enum Particles {
		
		EXPLOSION_NORMAL,
		EXPLOSION_LARGE,
		EXPLOSION_HUGE,
		FIREWORKS_SPARK,
		WATER_BUBBLE,
		WATER_SPLASH,
		WATER_WAKE,
		SUSPENDED,
		SUSPENDED_DEPTH,
		CRIT,
		CRIT_MAGIC,
		SMOKE_NORMAL,
		SMOKE_LARGE,
		SPELL,
		SPELL_INSTANT,
		SPELL_MOB,
		SPELL_MOB_AMBIENT,
		SPELL_WITCH,
		DRIP_WATER,
		DRIP_LAVA,
		VILLAGER_ANGRY,
		VILLAGER_HAPPY,
		TOWN_AURA,
		NOTE,
		PORTAL,
		ENCHANTMENT_TABLE,
		FLAME,
		LAVA,
		FOOTSTEP,
		CLOUD,
		REDSTONE,
		SNOWBALL,
		SNOW_SHOVEL,
		SLIME,
		HEART,
		BARRIER,
		ITEM_CRACK,
		BLOCK_CRACK,
		BLOCK_DUST,
		WATER_DROP,
		ITEM_TAKE,
		MOB_APPEARANCE,
		DRAGON_BREATH,
		END_ROD,
		DAMAGE_INDICATOR,
		SWEEP_ATTACK,
		FALLING_DUST,
		TOTEM,
		SPIT
		
	}
	
}