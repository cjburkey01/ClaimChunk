package com.cjburkey.claimchunk.cmd;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Config;
import com.cjburkey.claimchunk.Econ;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.chunk.ChunkHandler;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.packet.ParticleHandler;
import com.cjburkey.claimchunk.worldguard.WorldGuardHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public final class MainHandler {

    /**
     * Display particle effects around the provided chunk to the provided
     * player for the provided amount of time.
     *
     * @param chunk      The position of the chunk for which particle effects
     *                   should be shown.
     * @param showTo     The player to whom particles should be shown.
     * @param timeToShow The amount of time (in seconds) that the particles
     *                   should be displayed. This should be between 1 and 10
     *                   but it is clamped within this method.
     */
    public static void outlineChunk(ChunkPos chunk, Player showTo, int timeToShow) {
        // Get the particle effect to be used from the config
        String particleStr = Config.getString("chunks", "chunkOutlineParticle");
        final ParticleHandler.Particles particle;
        try {
            particle = ParticleHandler.Particles.valueOf(particleStr);
        } catch (Exception e) {
            Utils.err("Invalid particle effect: %s", particleStr);
            return;
        }

        // A list of locations to display particles
        List<Location> particleLocations = new ArrayList<>();

        // The current world
        World world = ClaimChunk.getInstance().getServer().getWorld(chunk.getWorld());
        // Make sure the world is valid
        if (world == null) return;

        // Limit how long chunks can be displayed from 1 to 10 seconds
        int showTimeInSeconds = Utils.clamp(timeToShow, 1, 10);

        // Get the start position in world coordinates
        int xStart = chunk.getX() << 4;
        int zStart = chunk.getZ() << 4;
        int yStart = (int) showTo.getLocation().getY() - 1;

        // The particle effects with be three blocks tall
        for (int ys = 0; ys < 3; ys++) {
            // The y--coordinate including the offset
            int y = yStart + ys;

            // Add the particles for the x-axis
            for (int i = 1; i < 16; i++) {
                particleLocations.add(new Location(world, xStart + i, y, zStart));
                particleLocations.add(new Location(world, xStart + i, y, zStart << 4));
            }

            // Add the particles for the z-axis
            for (int i = 0; i < (16 + 1); i++) {
                particleLocations.add(new Location(world, xStart, y, zStart + i));
                particleLocations.add(new Location(world, xStart << 4, y, zStart + i));
            }
        }

        // Loop through all the blocks that should display particles effects
        for (Location loc : particleLocations) {
            for (int i = 0; i < showTimeInSeconds * 2 + 1; i++) {
                // Schedule the particles for every half of second until the
                // end of the duration
                ClaimChunk.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(ClaimChunk.getInstance(),
                        () -> {
                            if (showTo.isOnline()) {
                                // If the player is still online, display the
                                // particles for them
                                ParticleHandler.spawnParticleForPlayers(loc, particle,
                                        showTo);
                            }
                        }, i * 10); // Flash every 10 ticks (half second)
            }
        }
    }

    // TODO: CHECK THIS METHOD
    public static void claimChunk(Player p, Chunk loc) {
        // Check permissions
        if (!Utils.hasPerm(p, true, "claim")) {
            Utils.toPlayer(p, ClaimChunk.getInstance().getMessages().claimNoPerm);
            return;
        }

        // Check if the chunk is already claimed
        ChunkHandler ch = ClaimChunk.getInstance().getChunkHandler();
        if (ch.isClaimed(loc.getWorld(), loc.getX(), loc.getZ())) {
            Utils.toPlayer(p, ClaimChunk.getInstance().getMessages().claimAlreadyOwned);
            return;
        }

        // Check if players can claim chunks here/in this world
        boolean allowedToClaimWG = WorldGuardHandler.isAllowedClaim(loc);
        boolean worldAllowsClaims = !Config.getList("chunks", "disabledWorlds").contains(loc.getWorld().getName());
        boolean adminOverride = Config.getBool("worldguard", "allowAdminOverride");
        boolean hasAdmin = Utils.hasAdmin(p);       // UH OH THIS WAS BROKEN SINCE 0.0.8!!!
        if (!(worldAllowsClaims || (hasAdmin && adminOverride)) || !(allowedToClaimWG || (hasAdmin && adminOverride))) {
            Utils.toPlayer(p, ClaimChunk.getInstance().getMessages().claimLocationBlock);
            return;
        }

        // Check if the player has room for more chunk claims
        int max = ClaimChunk.getInstance().getRankHandler().getMaxClaimsForPlayer(p);
        Utils.debug("Player %s can claim %s chunks", p.getDisplayName(), max);
        if (max > 0) {
            if (ch.getClaimed(p.getUniqueId()) >= max) {
                Utils.toPlayer(p, ClaimChunk.getInstance().getMessages().claimTooMany);
                return;
            }
        }

        // Check if economy should be used
        boolean useEcon = ClaimChunk.getInstance().useEconomy();
        boolean econFree = false;
        double finalCost = 0.0d;
        Econ e = null;
        if (useEcon) {
            if (!ch.getHasAllFreeChunks(p.getUniqueId())) {
                econFree = true;
            } else {
                e = ClaimChunk.getInstance().getEconomy();
                double cost = Config.getDouble("economy", "claimPrice");
                if (cost > 0 && !e.buy(p.getUniqueId(), cost)) {
                    Utils.toPlayer(p, ClaimChunk.getInstance().getMessages().claimNotEnoughMoney);
                    return;
                }
                finalCost = cost;
            }
        }

        // Claim the chunk if nothing is wrong
        ChunkPos pos = ch.claimChunk(loc.getWorld(), loc.getX(), loc.getZ(), p.getUniqueId());
        if (pos != null && Config.getBool("chunks", "particlesWhenClaiming")) {
            outlineChunk(pos, p, 3);
        }
        String msg;
        if (econFree) {
            int freeCount = Config.getInt("economy", "firstFreeChunks");
            if (freeCount == 1) {
                msg = ClaimChunk.getInstance().getMessages().claimFree1;
            } else {
                msg = ClaimChunk.getInstance().getMessages().claimFrees.replaceAll(Pattern.quote("%%COUNT%%"), freeCount + "");
            }
        } else {
            msg = ClaimChunk.getInstance().getMessages().claimSuccess
                    .replace("%%PRICE%%", ((e == null || finalCost <= 0.0d) ? ClaimChunk.getInstance().getMessages().claimNoCost : e.format(finalCost)));
        }
        Utils.toPlayer(p, msg);
    }

    public static void toggleTnt(Player executor) {
        ChunkHandler handler = ClaimChunk.getInstance().getChunkHandler();
        Chunk chunk = executor.getLocation().getChunk();
        if (handler.isOwner(chunk, executor)) {
            Utils.toPlayer(executor, (handler.toggleTnt(chunk) ? ClaimChunk.getInstance().getMessages().tntEnabled : ClaimChunk.getInstance().getMessages().tntDisabled));
            return;
        }
        Utils.toPlayer(executor, ClaimChunk.getInstance().getMessages().tntNoPerm);
    }

    // TODO: CHECK THIS METHOD
    public static boolean unclaimChunk(boolean adminOverride, boolean hideTitle, Player p, String world, int x, int z) {
        try {
            // Check permissions
            if ((!adminOverride && !Utils.hasPerm(p, true, "unclaim"))
                    || (adminOverride && !Utils.hasAdmin(p))) {
                if (!hideTitle)
                    Utils.toPlayer(p, ClaimChunk.getInstance().getMessages().unclaimNoPerm);
                return false;
            }

            // Check if the chunk isn't claimed
            ChunkHandler ch = ClaimChunk.getInstance().getChunkHandler();
            World w = Bukkit.getWorld(world);
            if (w == null) {
                Utils.err("Failed to locate world %s", world);
                return false;
            }
            if (!ch.isClaimed(w, x, z)) {
                if (!hideTitle)
                    Utils.toPlayer(p, ClaimChunk.getInstance().getMessages().unclaimNotOwned);
                return false;
            }

            // Check if the unclaimer is the owner or admin override is enable
            if (!adminOverride && !ch.isOwner(w, x, z, p)) {
                if (!hideTitle)
                    Utils.toPlayer(p, ClaimChunk.getInstance().getMessages().unclaimNotOwner);
                return false;
            }

            // Check if a refund is required
            boolean refund = false;

            if (!adminOverride && ClaimChunk.getInstance().useEconomy()
                    && ch.getClaimed(p.getUniqueId()) > Config.getInt("economy", "firstFreeChunks")) {
                Econ e = ClaimChunk.getInstance().getEconomy();
                double reward = Config.getDouble("economy", "unclaimReward");
                if (reward > 0) {
                    e.addMoney(p.getUniqueId(), reward);
                    if (!hideTitle) {
                        Utils.toPlayer(p, ClaimChunk.getInstance().getMessages().unclaimRefund.replace("%%AMT%%", e.format(reward)));
                    }
                    refund = true;
                }
            }

            // Unclaim the chunk
            ch.unclaimChunk(w, x, z);
            if (!refund && !hideTitle) {
                Utils.toPlayer(p, ClaimChunk.getInstance().getMessages().unclaimSuccess);
            }
            return true;
        } catch (Exception e) {
            Utils.err("Failed to unclaim chunk for player %s at %s,%s in %s", p.getDisplayName(), x, z, world);
            e.printStackTrace();
        }
        return false;
    }

    public static void unclaimChunk(boolean adminOverride, boolean raw, Player p) {
        Chunk chunk = p.getLocation().getChunk();
        unclaimChunk(adminOverride, raw, p, p.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    private static void accessChunk(Player p, String player, boolean multiple) {
        if (!Utils.hasPerm(p, true, "claim")) {
            Utils.toPlayer(p, ClaimChunk.getInstance().getMessages().accessNoPerm);
            return;
        }

        Player other = ClaimChunk.getInstance().getServer().getPlayer(player);
        if (other != null) {
            toggleAccess(p, other.getUniqueId(), other.getName(), multiple);
        } else {
            UUID otherId = ClaimChunk.getInstance().getPlayerHandler().getUUID(player);
            if (otherId == null) {
                Utils.toPlayer(p, ClaimChunk.getInstance().getMessages().noPlayer);
                return;
            }
            toggleAccess(p, otherId, player, multiple);
        }
    }

    public static void accessChunk(Player p, String[] players) {
        for (String player : players) accessChunk(p, player, players.length > 1);
    }

    private static void toggleAccess(Player owner, UUID other, String otherName, boolean multiple) {
        if (owner.getUniqueId().equals(other)) {
            Utils.toPlayer(owner, ClaimChunk.getInstance().getMessages().accessOneself);
            return;
        }
        boolean hasAccess = ClaimChunk.getInstance().getPlayerHandler().toggleAccess(owner.getUniqueId(), other);
        if (hasAccess) {
            Utils.toPlayer(owner,
                    (multiple ? ClaimChunk.getInstance().getMessages().accessToggleMultiple : ClaimChunk.getInstance().getMessages().accessHas).replace("%%PLAYER%%", otherName));
            return;
        }
        Utils.toPlayer(owner,
                (multiple ? ClaimChunk.getInstance().getMessages().accessToggleMultiple : ClaimChunk.getInstance().getMessages().accessNoLongerHas).replace("%%PLAYER%%", otherName));
    }

    public static void listAccessors(Player executor) {
        Utils.msg(executor, Config.infoColor() + "&l---[ ClaimChunk Access ] ---");
        boolean anyOthersHaveAccess = false;
        for (UUID player : ClaimChunk.getInstance().getPlayerHandler().getAccessPermitted(executor.getUniqueId())) {
            String name = ClaimChunk.getInstance().getPlayerHandler().getUsername(player);
            if (name != null) {
                Utils.msg(executor, Config.infoColor() + "  - " + name);
                anyOthersHaveAccess = true;
            }
        }
        if (!anyOthersHaveAccess) {
            Utils.msg(executor, "  " + ClaimChunk.getInstance().getMessages().accessNoOthers);
        }
    }

}
