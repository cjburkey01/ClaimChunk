package com.cjburkey.claimchunk.packet;

import com.cjburkey.claimchunk.Utils;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

/**
 * Class to handle particle effects using reflection. Hopefully version
 * independent.
 */
public final class ParticleHandler {

    private static Class<?> particleClass;
    private static Class<?> particleEnum;
    private static Class<?>[] parameters;

    private static boolean useNewParticleSystem = false;

    static {
        try {
            particleClass = PacketHandler.getNMSClass("PacketPlayOutWorldParticles");
            particleEnum = PacketHandler.getNMSClass("EnumParticle");

            parameters = new Class<?>[]{particleEnum, boolean.class, float.class, float.class, float.class,
                    float.class, float.class, float.class, float.class, int.class, int[].class};
        } catch (ClassNotFoundException ignored) {
            useNewParticleSystem = true;
        }
    }

    /**
     * Spawns (particle) at (loc), visible to all of (forPlayer)
     *
     * @param loc      The location at which to display the particle.
     * @param particle The particle to display.
     * @param players  The player(s) for whom to display the particles.
     */
    public static void spawnParticleForPlayers(Location loc, Particles particle, Player... players) {
        try {
            spawnParticle(loc, players, particle);
        } catch (Exception e) {
            Utils.err("Failed to spawn particle %s for players: %s at %s", particle.name(), Arrays.toString(players), loc);
            e.printStackTrace();
        }
    }

    private static void spawnParticle(Location loc, Player[] players, Particles particle) throws Exception {
        if (useNewParticleSystem) spawn(loc, players, particle);
        else spawnLegacy(loc, players, particle);
    }

    private static void spawn(Location loc, Player[] players, Particles particle) {
        final Particle bukkitParticle = Particle.valueOf(particle.name());
        //noinspection ConstantConditions
        if (bukkitParticle == null) {
            Utils.err("Invalid particle: %s", particle.name());
            return;
        }
        for (Player p : players) p.spawnParticle(bukkitParticle, loc, 1, 0.0d, 0.0d, 0.0d, 0.0d, null);
    }

    private static void spawnLegacy(Location loc, Player[] players, Particles particle) throws Exception {
        if (particleClass == null || particleEnum == null || parameters == null) {
            Utils.err("Failed to locate particle classes and enums");
            return;
        }
        Constructor<?> constructor = particleClass.getConstructor(parameters);
        //noinspection JavaReflectionInvocation
        Object packet = constructor.newInstance(particleEnum.getField(particle.name()).get(null), true, (float) loc.getX(),
                (float) loc.getY(), (float) loc.getZ(), 0f, 0f, 0f, 1f, 0, new int[0]);
        for (Player p : players) PacketHandler.sendPacket(p, packet);
    }

    /**
     * Contains a (hopefully) non-version-specific list of particles available for use.
     *
     * @author cjburkey
     */
    @SuppressWarnings("unused")
    public enum Particles {

        EXPLOSION_NORMAL, EXPLOSION_LARGE, EXPLOSION_HUGE, FIREWORKS_SPARK, WATER_BUBBLE, WATER_SPLASH, WATER_WAKE, SUSPENDED, SUSPENDED_DEPTH, CRIT, CRIT_MAGIC, SMOKE_NORMAL, SMOKE_LARGE, SPELL, SPELL_INSTANT, SPELL_MOB, SPELL_MOB_AMBIENT, SPELL_WITCH, DRIP_WATER, DRIP_LAVA, VILLAGER_ANGRY, VILLAGER_HAPPY, TOWN_AURA, NOTE, PORTAL, ENCHANTMENT_TABLE, FLAME, LAVA, FOOTSTEP, CLOUD, REDSTONE, SNOWBALL, SNOW_SHOVEL, SLIME, HEART, BARRIER, ITEM_CRACK, BLOCK_CRACK, BLOCK_DUST, WATER_DROP, ITEM_TAKE, MOB_APPEARANCE, DRAGON_BREATH, END_ROD, DAMAGE_INDICATOR, SWEEP_ATTACK, FALLING_DUST, TOTEM, SPIT

    }

}
