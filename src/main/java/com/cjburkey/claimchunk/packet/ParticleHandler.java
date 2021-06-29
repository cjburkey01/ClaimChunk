package com.cjburkey.claimchunk.packet;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

/**
 * Class to handle particle effects using reflection. Hopefully version
 * independent.
 */
public final class ParticleHandler {

    /**
     * Spawns (particle) at (loc), visible to all of (forPlayer)
     *
     * @param loc      The location at which to display the particle.
     * @param particle The particle to display.
     * @param players  The player(s) for whom to display the particles.
     */
    public static void spawnParticleForPlayers(Particle particle, Location loc, int count, Player... players) {
        for (Player player : players) {
            if (player != null && player.isOnline()) {
                player.spawnParticle(particle, loc, count);
            }
        }
    }

}
