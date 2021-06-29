package com.cjburkey.claimchunk.cmd;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Econ;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.chunk.ChunkHandler;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.packet.ParticleHandler;
import com.cjburkey.claimchunk.rank.RankHandler;
import com.cjburkey.claimchunk.service.prereq.PrereqChecker;
import com.cjburkey.claimchunk.service.prereq.claim.*;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class MainHandler {

    private final ClaimChunk claimChunk;

    public MainHandler(ClaimChunk claimChunk) {
        this.claimChunk = claimChunk;
    }

    /**
     * Display particle effects around the provided chunk to the provided
     * player for the provided amount of time.
     *
     * @param chunk      The position of the chunk for which particle effects
     *                   should be shown.
     * @param showTo     The player to whom particles should be shown.
     * @param timeToShow The amount of time (in seconds) that the particles
     *                   should be displayed. This should be between 1 and 60,
     *                   but it is clamped within this method.
     */
    public void outlineChunk(ChunkPos chunk, Player showTo, int timeToShow) {
        // Get the particle effect to be used from the config
        String particleStr = claimChunk.chConfig().getChunkOutlineParticle();
        final Particle particle;
        try {
            particle = Particle.valueOf(particleStr);
        } catch (Exception e) {
            Utils.err("Invalid particle effect: %s", particleStr);
            Utils.err("You can see /plugins/ClaimChunk/ValidParticleEffects.txt for a complete list.");
            return;
        }

        // A list of locations to display particles
        List<Location> particleLocations = new ArrayList<>();

        // The current world
        World world = claimChunk.getServer()
                                .getWorld(chunk.getWorld());
        // Make sure the world is valid
        if (world == null) {
            return;
        }

        // Limit how long chunks can be displayed from 1 to 10 seconds
        int showTimeInSeconds = Utils.clamp(timeToShow, 1, 60);

        // Get the start position in world coordinates
        int xStart = chunk.getX() << 4;
        int zStart = chunk.getZ() << 4;
        int yStart = (int) showTo.getLocation()
                                 .getY() - 1;

        // The particle effects with be three blocks tall
        for (int ys = 0; ys < 3; ys++) {
            // The y--coordinate including the offset
            int y = yStart + ys;

            // Add the particles for the x-axis
            for (int i = 1; i < 16; i++) {
                particleLocations.add(new Location(world, xStart + i, y, zStart));
                particleLocations.add(new Location(world, xStart + i, y, zStart + 16));
            }

            // Add the particles for the z-axis
            for (int i = 0; i < (16 + 1); i++) {
                particleLocations.add(new Location(world, xStart, y, zStart + i));
                particleLocations.add(new Location(world, xStart + 16, y, zStart + i));
            }
        }

        int perSec = claimChunk.chConfig().getChunkOutlineSpawnPerSec();

        // Loop through all the blocks that should display particles effects
        for (Location loc : particleLocations) {
            for (int i = 0; i <= showTimeInSeconds * perSec; i++) {
                // Schedule the particles for every half of second until the
                // end of the duration
                claimChunk.getServer()
                          .getScheduler()
                          .scheduleSyncDelayedTask(claimChunk, () -> {
                              if (showTo.isOnline()) {
                                  // If the player is still online, display the
                                  // particles for them
                                  ParticleHandler.spawnParticleForPlayers(
                                          particle,
                                          loc,
                                          claimChunk.chConfig().getChunkOutlineParticlesPerSpawn(),
                                          showTo);
                              }
                          }, i * (20L / perSec));
            }
        }
    }

    public void claimChunk(Player p, Chunk loc) {
        // TODO: Temporary code until the services system is fully running
        final ArrayList<IClaimPrereq> claimPrereqs = new ArrayList<>();

        // Check permissions
        claimPrereqs.add(new PermissionPrereq());

        // Check that the world is enabled
        claimPrereqs.add(new WorldPrereq());

        // Check if the chunk is already claimed
        claimPrereqs.add(new UnclaimedPrereq());

        // Check if players can claim chunks here/in this world
        claimPrereqs.add(new WorldGuardPrereq());

        // Check if the player has room for more chunk claims
        claimPrereqs.add(new MaxChunksPrereq());

        // Check if economy should be used
        if (claimChunk.useEconomy()) {
            claimPrereqs.add(new EconPrereq());
        }

        // Create the prereq checker object for claiming
        final PrereqChecker<IClaimPrereq, PrereqClaimData> PREREQ = new PrereqChecker<>(claimPrereqs);

        final ClaimChunk CLAIM_CHUNK = claimChunk;
        final ChunkHandler CHUNK_HANDLE = CLAIM_CHUNK.getChunkHandler();

        PREREQ.check(new PrereqClaimData(CLAIM_CHUNK, loc, p.getUniqueId(), p),
                     CLAIM_CHUNK.getMessages().claimSuccess.replace("%%PRICE%%", CLAIM_CHUNK.getMessages().claimNoCost),
                     errorMsg -> errorMsg.ifPresent(msg -> Utils.toPlayer(p, msg)), successMsg -> {
                    // Claim the chunk if nothing is wrong
                    ChunkPos pos = CHUNK_HANDLE.claimChunk(loc.getWorld(), loc.getX(), loc.getZ(), p.getUniqueId());

                    // Error check, though it *shouldn't* occur
                    if (pos == null) {
                        Utils.err(
                                "Failed to claim chunk (%s, %s) in world %s for player %s. The data handler returned "
                                + "a null position?",
                                loc.getX(), loc.getZ(), loc.getWorld()
                                                           .getName(), p.getName()
                        );
                        return;
                    }

                    // Send the success message to the player if it's present (it should be)
                    successMsg.ifPresent(msg -> Utils.toPlayer(p, msg));

                    // Display the chunk outline
                    if (claimChunk.chConfig().getParticlesWhenClaiming()) {
                        outlineChunk(pos, p, claimChunk.chConfig().getChunkOutlineDurationSeconds());
                    }
                }
        );
    }

    @SuppressWarnings("unused")
    @Deprecated
    public void toggleTnt(Player executor) {
        ChunkHandler handler = claimChunk.getChunkHandler();
        Chunk chunk = executor.getLocation()
                              .getChunk();
        if (handler.isOwner(chunk, executor)) {
            Utils.toPlayer(executor, (handler.toggleTnt(
                    chunk) ? claimChunk.getMessages().tntEnabled : claimChunk.getMessages().tntDisabled));
            return;
        }
        Utils.toPlayer(executor, claimChunk.getMessages().tntNoPerm);
    }

    // TODO: CHECK THIS METHOD
    public boolean unclaimChunk(boolean adminOverride, boolean hideTitle, Player p, String world, int x, int z) {
        try {
            // Check permissions
            if ((!adminOverride && !Utils.hasPerm(p, true, "unclaim")) || (adminOverride && !Utils.hasAdmin(p))) {
                if (!hideTitle) {
                    Utils.toPlayer(p, claimChunk.getMessages().unclaimNoPerm);
                }
                return false;
            }

            // Check if the chunk isn't claimed
            ChunkHandler ch = claimChunk.getChunkHandler();
            World w = Bukkit.getWorld(world);
            if (w == null) {
                Utils.err("Failed to locate world %s", world);
                return false;
            }
            if (!ch.isClaimed(w, x, z)) {
                if (!hideTitle) {
                    Utils.toPlayer(p, claimChunk.getMessages().unclaimNotOwned);
                }
                return false;
            }

            // Check if the unclaiming player is the owner or admin override is enable
            if (!adminOverride && !ch.isOwner(w, x, z, p)) {
                if (!hideTitle) {
                    Utils.toPlayer(p, claimChunk.getMessages().unclaimNotOwner);
                }
                return false;
            }

            // Check if a refund is required
            boolean refund = false;

            if (!adminOverride
                    && claimChunk.useEconomy()
                    && ch.getClaimed(p.getUniqueId()) > claimChunk.chConfig().getFirstFreeChunks()) {
                Econ e = claimChunk.getEconomy();
                double reward = claimChunk.chConfig().getUnclaimReward();
                if (reward > 0) {
                    e.addMoney(p.getUniqueId(), reward);
                    if (!hideTitle) {
                        Utils.toPlayer(p, claimChunk.getMessages().unclaimRefund.replace("%%AMT%%", e.format(reward)));
                    }
                    refund = true;
                }
            }

            // Unclaim the chunk
            ch.unclaimChunk(w, x, z);
            if (!refund && !hideTitle) {
                Utils.toPlayer(p, claimChunk.getMessages().unclaimSuccess);
            }
            return true;
        } catch (Exception e) {
            Utils.err("Failed to unclaim chunk for player %s at %s,%s in %s", p.getDisplayName(), x, z, world);
            e.printStackTrace();
        }
        return false;
    }

    public void unclaimChunk(boolean adminOverride, boolean raw, Player p) {
        Chunk chunk = p.getLocation().getChunk();
        unclaimChunk(adminOverride, raw, p, p.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    private void accessChunk(Player p, String player, boolean multiple) {
        if (!Utils.hasPerm(p, true, "claim")) {
            Utils.toPlayer(p, claimChunk.getMessages().accessNoPerm);
            return;
        }

        Player other = claimChunk.getServer().getPlayer(player);
        if (other != null) {
            toggleAccess(p, other.getUniqueId(), other.getName(), multiple);
        } else {
            UUID otherId = claimChunk.getPlayerHandler().getUUID(player);
            if (otherId == null) {
                Utils.toPlayer(p, claimChunk.getMessages().noPlayer);
                return;
            }
            toggleAccess(p, otherId, player, multiple);
        }
    }

    public void accessChunk(Player p, String[] players) {
        for (String player : players)
            accessChunk(p, player, players.length > 1);
    }

    private void toggleAccess(Player owner, UUID other, String otherName, boolean multiple) {
        if (owner.getUniqueId()
                 .equals(other)) {
            Utils.toPlayer(owner, claimChunk.getMessages().accessOneself);
            return;
        }
        boolean hasAccess = claimChunk.getPlayerHandler()
                                      .toggleAccess(owner.getUniqueId(), other);
        if (hasAccess) {
            Utils.toPlayer(owner,
                           (multiple ? claimChunk.getMessages().accessToggleMultiple :
                                    claimChunk.getMessages().accessHas).replace(
                                   "%%PLAYER%%", otherName)
            );
            return;
        }
        Utils.toPlayer(owner,
                       (multiple ? claimChunk.getMessages().accessToggleMultiple :
                                claimChunk.getMessages().accessNoLongerHas).replace(
                               "%%PLAYER%%", otherName)
        );
    }

    public void listAccessors(Player executor) {
        Utils.msg(executor, claimChunk.getMessages().accessListTitle);
        boolean anyOthersHaveAccess = false;

        for (UUID player : claimChunk.getPlayerHandler().getAccessPermitted(executor.getUniqueId())) {
            String name = claimChunk.getPlayerHandler().getUsername(player);
            if (name != null) {
                Utils.msg(executor, claimChunk.chConfig().getInfoColor() + "  - " + name);
                anyOthersHaveAccess = true;
            }
        }

        if (!anyOthersHaveAccess) {
            Utils.msg(executor, "  " + claimChunk.getMessages().accessNoOthers);
        }
    }

    public void giveChunk(Player giver, Chunk chunk, String newOwner) {
        // Make sure the server has chunk giving enabled
        if (!claimChunk.chConfig().getAllowChunkGive()) {
            Utils.toPlayer(giver, claimChunk.getMessages().giveDisabled);
            return;
        }

        // Make sure player has access to give chunks
        if (!Utils.hasPerm(giver, true, "give")) {
            Utils.toPlayer(giver, claimChunk.getMessages().giveNoPerm);
            return;
        }

        // Get the chunk handler
        final ChunkHandler chunkHandler = claimChunk.getChunkHandler();
        final RankHandler rankHandler = claimChunk.getRankHandler();

        // Check if this player owns this chunk
        if (!chunkHandler.isOwner(chunk, giver)) {
            Utils.toPlayer(giver, claimChunk.getMessages().giveNotYourChunk);
            return;
        }

        // Get the new chunk owner
        Player givenPly = claimChunk.getServer().getPlayer(newOwner);
        if (givenPly == null) {
            Utils.toPlayer(giver, claimChunk.getMessages().giveNoPlayer.replace("%%PLAYER%%", newOwner));
            return;
        }

        // Get the receiving player's UUID
        UUID given = givenPly.getUniqueId();

        // Make sure the owner isn't trying to give the chunk to themself
        if (giver.getUniqueId().equals(given)) {
            Utils.toPlayer(giver, claimChunk.getMessages().giveNotYourself);
            return;
        }

        // Make sure the receiving player doesn't have too many chunks already
        if (chunkHandler.getClaimed(given) >= rankHandler.getMaxClaimsForPlayer(givenPly)) {
            Utils.toPlayer(giver, claimChunk.getMessages().giveChunksFull.replace("%%PLAYER%%", newOwner));
            return;
        }

        // Unclaim the chunk from the old owner
        chunkHandler.unclaimChunk(chunk.getWorld(), chunk.getX(), chunk.getZ());

        // Claim the chunk for the new owner
        ChunkPos newChunk = chunkHandler.claimChunk(chunk.getWorld(), chunk.getX(), chunk.getZ(), given);

        // Error check (it should never happen)
        if (newChunk == null) {
            Utils.toPlayer(giver, claimChunk.getMessages().giveError);
            Utils.err("Failed to give %s the chunk (%s, %s) in world %s from player %s", newOwner, chunk.getX(),
                      chunk.getZ(), chunk.getWorld().getName(), giver.getDisplayName()
            );
            return;
        }

        // Tell the player they have given their chunk
        Utils.toPlayer(giver, claimChunk.getMessages().gaveChunk.replace("%%PLAYER%%", newOwner));

        // Tell the player (if they're online) that they have received a chunk
        Player onlineGiven = claimChunk.getServer().getPlayer(given);
        if (onlineGiven != null) {
            Utils.toPlayer(onlineGiven, claimChunk.getMessages().givenChunk.replace("%%PLAYER%%", giver.getDisplayName())
            );
        }
    }

}
