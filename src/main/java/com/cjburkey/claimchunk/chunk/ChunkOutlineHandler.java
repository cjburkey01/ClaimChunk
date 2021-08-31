package com.cjburkey.claimchunk.chunk;

import com.cjburkey.claimchunk.ClaimChunk;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.Set;

/**
 * Overhaul of how particles surrounding chunks are handled that should be much more efficient.
 *
 * @since 0.0.23
 */
public record ChunkOutlineHandler(
        ClaimChunk claimChunk,
        Particle particle,
        long particlePeriodTicks,
        int yHeight,
        int particleCount) {

    public record OutlineSides(boolean north, boolean south, boolean east, boolean west) {
        public boolean empty() {
            return !(north || south || east || west);
        }

        public static OutlineSides makeAll(boolean show) {
            return new OutlineSides(show, show, show, show);
        }
    }

    /**
     * Start outlining the given chunk with particles to the given player for the given duration in
     * seconds.
     *
     * @param chunkPos The chunk to outline.
     * @param player The player allowed to see the outline.
     * @param durationInSeconds The duration, in seconds, of the particles spawning.
     * @param outlineSides Which sides of the chunk outline should be shown.
     */
    public void showChunkFor(
            @NonNull ChunkPos chunkPos,
            @NonNull Player player,
            int durationInSeconds,
            @NonNull OutlineSides outlineSides) {
        if (!player.isOnline() || outlineSides.empty()) return;

        var entry =
                new ChunkOutlineEntry(
                        chunkPos, player, outlineSides, player.getLocation().getBlockY());
        entry.cyclesLeft = durationInSeconds * 20L / particlePeriodTicks;
        entry.taskId =
                claimChunk
                        .getServer()
                        .getScheduler()
                        .scheduleSyncRepeatingTask(
                                // Slight delay (1 tick), just in case ;)
                                claimChunk, entry::onParticle, 1L, particlePeriodTicks);
    }

    /**
     * Show a set of chunks to the given player for the given amount of time, ensuring that adjacent
     * chunks will not have borders shown between them when unnecessary.
     *
     * @param chunksPos The set of chunk positions.
     * @param player The player who can see the particles.
     * @param durationInSeconds The length of time, in seconds, for which the particles will be
     *     visible.
     */
    public void showChunksFor(
            @NonNull Set<ChunkPos> chunksPos, @NonNull Player player, int durationInSeconds) {
        if (!player.isOnline()) return;

        for (var chunkPos : chunksPos) {
            var north = !chunksPos.contains(chunkPos.north());
            var south = !chunksPos.contains(chunkPos.south());
            var east = !chunksPos.contains(chunkPos.east());
            var west = !chunksPos.contains(chunkPos.west());
            var outlineSides = new OutlineSides(north, south, east, west);
            if (!outlineSides.empty()) {
                showChunkFor(chunkPos, player, durationInSeconds, outlineSides);
            }
        }
    }

    @RequiredArgsConstructor
    private class ChunkOutlineEntry {

        final ChunkPos chunkPos;
        final Player player;
        final OutlineSides sidesShown;
        final int plyY;

        int taskId = -1;
        long cyclesLeft;

        void onParticle() {
            // Loop through `yHeight*2+1` y-levels
            for (var y = plyY - yHeight; y <= plyY + yHeight; y++) {
                var zAt = chunkPos.getZ() << 4;
                // Spawn particles along the x-axis
                if (sidesShown.north | sidesShown.south) {
                    for (var x = chunkPos.getX() << 4; x < ((chunkPos.getX() + 1) << 4); x++) {
                        if (sidesShown.north) spawnParticle(player, x, y, zAt);
                        if (sidesShown.south) spawnParticle(player, x, y, zAt + 15);
                    }
                }
                // Spawn particles along the z-axis (offset because corners were handled long the
                // x-axis)
                if (sidesShown.east | sidesShown.west) {
                    // TODO: Ignore these offsets for now, overdraw doesn't concern me very much lol
                    for (var z = zAt /* + 1*/; z < zAt + 16 /* - 1*/; z++) {
                        var xAt = chunkPos.getX() << 4;
                        if (sidesShown.east) spawnParticle(player, xAt, y, z);
                        if (sidesShown.west) spawnParticle(player, xAt + 15, y, z);
                    }
                }
            }

            // Cancel this task once its time has run out
            if (--cyclesLeft == 0) {
                claimChunk.getServer().getScheduler().cancelTask(taskId);
            }
        }

        private void spawnParticle(Player player, int x, int y, int z) {
            player.spawnParticle(
                    particle, x + 0.5d, y + 0.5d, z + 0.5, particleCount, 0.0, 0.0, 0.0, 0.0);
        }
    }
}
