package com.cjburkey.claimchunk.chunk;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Config;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.packet.ParticleHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public final class ChunkPos {

    private final String world;
    private final int x;
    private final int z;

    public ChunkPos(String world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;
    }

    public ChunkPos(Chunk chunk) {
        this(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    public String getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    // TODO: MOVE THIS TO A DIFFERENT CLASS?
    public void outlineChunk(Player showTo, int timeToShow) {
        String particleStr = Config.getString("chunks", "chunkOutlineParticle");
        final ParticleHandler.Particles particle;
        try {
            particle = ParticleHandler.Particles.valueOf(particleStr);
        } catch (Exception e) {
            Utils.err("Invalid particle effect: %s", particleStr);
            return;
        }

        List<Location> blocksToDo = new ArrayList<>();
        World world = ClaimChunk.getInstance().getServer().getWorld(this.world);

        int showTimeInSeconds = Utils.clamp(timeToShow, 1, 10);

        int xStart = x * 16;
        int zStart = z * 16;
        int yStart = (int) showTo.getLocation().getY() - 1;
        for (int ys = 0; ys < 3; ys++) {
            int y = yStart + ys;
            for (int i = 1; i < 16; i++) {
                blocksToDo.add(new Location(world, xStart + i, y, zStart));
                blocksToDo.add(new Location(world, xStart + i, y, zStart + 16));
            }
            for (int i = 0; i < 17; i++) {
                blocksToDo.add(new Location(world, xStart, y, zStart + i));
                blocksToDo.add(new Location(world, xStart + 16, y, zStart + i));
            }
        }

        for (Location loc : blocksToDo) {
            for (int i = 0; i < showTimeInSeconds * 2 + 1; i++) {
                ClaimChunk.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(ClaimChunk.getInstance(),
                        () -> {
                            if (showTo.isOnline()) {
                                ParticleHandler.spawnParticleForPlayers(loc, particle,
                                        showTo);
                            }
                        }, i * 10);
            }
        }
    }

    @Override
    public String toString() {
        return world + "," + x + "," + z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkPos chunkPos = (ChunkPos) o;
        return x == chunkPos.x &&
                z == chunkPos.z &&
                Objects.equals(world, chunkPos.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, x, z);
    }

}
