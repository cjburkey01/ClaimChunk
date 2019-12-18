package com.cjburkey.claimchunk.cmd;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Config;
import com.cjburkey.claimchunk.Econ;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.chunk.ChunkHandler;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.packet.ParticleHandler;
import com.cjburkey.claimchunk.service.claimprereq.EconPrereq;
import com.cjburkey.claimchunk.service.claimprereq.IClaimPrereq;
import com.cjburkey.claimchunk.service.claimprereq.MaxChunksPrereq;
import com.cjburkey.claimchunk.service.claimprereq.PermissionPrereq;
import com.cjburkey.claimchunk.service.claimprereq.UnclaimedPrereq;
import com.cjburkey.claimchunk.service.claimprereq.WorldGuardPrereq;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
        final ClaimChunk CLAIM_CHUNK = ClaimChunk.getInstance();
        final ChunkHandler CHUNK_HANDLE = CLAIM_CHUNK.getChunkHandler();
        String successOutput = null;

        // TODO: Temporary code until the services system is fully running
        IClaimPrereq[] claimPrereqs = new IClaimPrereq[] {
                // Check permissions
                new PermissionPrereq(),

                // Check if the chunk is already claimed
                new UnclaimedPrereq(),

                // Check if players can claim chunks here/in this world
                new WorldGuardPrereq(),

                // Check if the player has room for more chunk claims
                new MaxChunksPrereq(),

                // Check if economy should be used
                new EconPrereq(),
        };
        Arrays.sort(claimPrereqs, new IClaimPrereq.ClaimPrereqComparator());

        // Check all the prerequisites
        for (IClaimPrereq claimPrereq : claimPrereqs) {
            if (!claimPrereq.getCanClaim(CLAIM_CHUNK, p, loc)) {
                // Get and display (if present) the message for why the player
                // can't claim this chunk
                Optional<String> errorMessage = claimPrereq.getErrorMessage(CLAIM_CHUNK, p, loc);
                errorMessage.ifPresent(error -> Utils.toPlayer(p, error));
                return;
            }

            // Get and update (if present) the message about this user's
            // successful claim
            Optional<String> successMessage = claimPrereq.getSuccessMessage(CLAIM_CHUNK, p, loc);
            if (successMessage.isPresent()) {
                successOutput = successMessage.get();
            }
        }

        // Default success message
        if (successOutput == null) {
            successOutput = CLAIM_CHUNK.getMessages().claimSuccess.replace("%%PRICE%%", CLAIM_CHUNK.getMessages().claimNoCost);
        }

        // Display the success message
        Utils.toPlayer(p, successOutput);

        // Claim the chunk if nothing is wrong
        ChunkPos pos = CHUNK_HANDLE.claimChunk(loc.getWorld(), loc.getX(), loc.getZ(), p.getUniqueId());

        // Error check, though it *shouldn't* occur
        if (pos == null) {
            Utils.err("Failed to claim chunk (%s, %s) in world %s for player %s. The chunk was already claimed?",
                    loc.getX(),
                    loc.getZ(),
                    loc.getWorld().getName(),
                    p.getName());
            return;
        }

        // Display the chunk outline
        if (Config.getBool("chunks", "particlesWhenClaiming")) {
            outlineChunk(pos, p, 3);
        }

        // Run success methods
        for (IClaimPrereq claimPrereq : claimPrereqs) {
            claimPrereq.onClaimSuccess(CLAIM_CHUNK, p, loc);
        }
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
