package com.cjburkey.claimchunk.cmd;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Econ;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.chunk.ChunkHandler;
import com.cjburkey.claimchunk.chunk.ChunkOutlineHandler;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.player.PlayerHandler;
import com.cjburkey.claimchunk.rank.RankHandler;
import com.cjburkey.claimchunk.service.prereq.claim.*;

import org.bukkit.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// TODO: DESTROY THIS CLASS ENTIRELY!

public final class MainHandler {

    private final ClaimChunk claimChunk;

    public MainHandler(ClaimChunk claimChunk) {
        this.claimChunk = claimChunk;
    }

    /**
     * Display particle effects around the provided chunk to the provided player for the provided
     * amount of time.
     *
     * @param chunk The position of the chunk for which particle effects should be shown.
     * @param showTo The player to whom particles should be shown.
     * @param timeToShow The amount of time (in seconds) that the particles should be displayed.
     *     This should be between 1 and 60, but it is clamped within this method.
     */
    @Deprecated
    public void outlineChunk(ChunkPos chunk, Player showTo, int timeToShow) {
        // Get the particle effect to be used from the config
        String particleStr = claimChunk.getConfigHandler().getChunkOutlineParticle();
        final Particle particle;
        try {
            particle = Particle.valueOf(particleStr);
        } catch (Exception e) {
            Utils.err("Invalid particle effect: %s", particleStr);
            Utils.err(
                    "You can see /plugins/ClaimChunk/ValidParticleEffects.txt for a complete"
                            + " list.");
            return;
        }

        // A list of locations to display particles
        List<Location> particleLocations = new ArrayList<>();

        // The current world
        World world = claimChunk.getServer().getWorld(chunk.getWorld());
        // Make sure the world is valid
        if (world == null) {
            return;
        }

        // Limit how long chunks can be displayed from 1 to 10 seconds
        int showTimeInSeconds = Utils.clamp(timeToShow, 1, 60);

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
                particleLocations.add(new Location(world, xStart + i, y, zStart + 16));
            }

            // Add the particles for the z-axis
            for (int i = 0; i < (16 + 1); i++) {
                particleLocations.add(new Location(world, xStart, y, zStart + i));
                particleLocations.add(new Location(world, xStart + 16, y, zStart + i));
            }
        }

        int perSec = claimChunk.getConfigHandler().getChunkOutlineSpawnPerSec();

        // Loop through all the blocks that should display particles effects
        for (Location loc : particleLocations) {
            for (int i = 0; i <= showTimeInSeconds * perSec; i++) {
                // Schedule the particles for every half of second until the
                // end of the duration
                claimChunk
                        .getServer()
                        .getScheduler()
                        .scheduleSyncDelayedTask(
                                claimChunk,
                                () -> {
                                    if (showTo.isOnline()) {
                                        // If the player is still online, display the
                                        // particles for them
                                        world.spawnParticle(
                                                particle,
                                                loc,
                                                claimChunk
                                                        .getConfigHandler()
                                                        .getChunkOutlineParticlesPerSpawn(),
                                                showTo);
                                    }
                                },
                                i * (20L / perSec));
            }
        }
    }

    public void claimChunk(Player p, Chunk loc) {
        final ChunkHandler chunkHandler = claimChunk.getChunkHandler();

        claimChunk
                .getPrereqHandlerLayer()
                .getClaimPrereqChecker()
                .check(
                        new PrereqClaimData(claimChunk, loc, p.getUniqueId(), p),
                        claimChunk
                                .getMessages()
                                .claimSuccess
                                .replace("%%PRICE%%", claimChunk.getMessages().claimNoCost),
                        errorMsg -> errorMsg.ifPresent(msg -> Utils.toPlayer(p, msg)),
                        successMsg -> {
                            // Claim the chunk if nothing is wrong
                            ChunkPos pos =
                                    chunkHandler.claimChunk(
                                            loc.getWorld(),
                                            loc.getX(),
                                            loc.getZ(),
                                            p.getUniqueId(),
                                            true);

                            // Error check, though it *shouldn't* occur
                            if (pos == null) {
                                Utils.err(
                                        "Failed to claim chunk (%s, %s) in world %s for player %s."
                                                + " The data handler returned a null position?",
                                        loc.getX(),
                                        loc.getZ(),
                                        loc.getWorld().getName(),
                                        p.getName());
                                return;
                            }

                            // Send the success message to the player if it's present (it should be)
                            successMsg.ifPresent(msg -> Utils.toPlayer(p, msg));

                            // Display the chunk outline
                            if (claimChunk.getConfigHandler().getParticlesWhenClaiming()) {
                                claimChunk
                                        .getChunkOutlineHandler()
                                        .showChunkFor(
                                                pos,
                                                p,
                                                claimChunk
                                                        .getConfigHandler()
                                                        .getChunkOutlineDurationSeconds(),
                                                ChunkOutlineHandler.OutlineSides.makeAll(true));
                            }
                        });
    }

    @SuppressWarnings("unused")
    @Deprecated
    public void toggleTnt(Player executor) {
        ChunkHandler handler = claimChunk.getChunkHandler();
        Chunk chunk = executor.getLocation().getChunk();
        if (handler.isOwner(chunk, executor)) {
            Utils.toPlayer(
                    executor,
                    (handler.toggleTnt(chunk)
                            ? claimChunk.getMessages().tntEnabled
                            : claimChunk.getMessages().tntDisabled));
            return;
        }
        Utils.toPlayer(executor, claimChunk.getMessages().tntNoPerm);
    }

    // TODO: CHECK THIS METHOD
    public boolean unclaimChunk(
            boolean adminOverride, boolean hideTitle, Player p, String world, int x, int z) {
        try {
            // Check permissions
            if ((!adminOverride && !Utils.hasPerm(p, true, "unclaim"))
                    || (adminOverride && !Utils.hasAdmin(p))) {
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
                    && ch.getClaimed(p.getUniqueId())
                            > claimChunk.getConfigHandler().getFirstFreeChunks()) {
                Econ e = claimChunk.getEconomy();
                double reward = claimChunk.getConfigHandler().getUnclaimReward();
                if (reward > 0) {
                    e.addMoney(p.getUniqueId(), reward);
                    if (!hideTitle) {
                        Utils.toPlayer(
                                p,
                                claimChunk
                                        .getMessages()
                                        .unclaimRefund
                                        .replace("%%AMT%%", e.format(reward)));
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
            Utils.err(
                    "Failed to unclaim chunk for player %s at %s,%s in %s",
                    p.getDisplayName(), x, z, world);
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
        return false;
    }

    public void unclaimChunk(boolean adminOverride, boolean raw, Player p) {
        Chunk chunk = p.getLocation().getChunk();
        unclaimChunk(adminOverride, raw, p, p.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    private void accessChunk(
            Player p, String accessor, Map<String, Boolean> arguments, boolean multiple) {
        if (!Utils.hasPerm(p, true, "claim")) {
            Utils.toPlayer(p, claimChunk.getMessages().accessNoPerm);
            return;
        }

        Player other = claimChunk.getServer().getPlayer(accessor);
        UUID otherId;
        if (other == null) {
            otherId = claimChunk.getPlayerHandler().getUUID(accessor);
            if (otherId == null) {
                Utils.toPlayer(p, claimChunk.getMessages().noPlayer);
                return;
            }
        } else {
            otherId = other.getUniqueId();
        }

        Boolean allChunks = arguments.remove("allChunks");
        if (allChunks != null && allChunks) {
            // Give access to all of the executing player's chunks
            for (ChunkPos chunk : claimChunk.getChunkHandler().getClaimedChunks(p.getUniqueId())) {
                if (!giveAccess(p, chunk, otherId, arguments)) return;
            }
            Utils.toPlayer(
                    p,
                    (multiple
                                    ? claimChunk.getMessages().accessHasMultiple
                                    : claimChunk.getMessages().accessHas)
                            .replace("%%PLAYER%%", accessor));
        } else {
            // Give access only to the chunk that the executing player is currently standing in
            ChunkPos chunk = new ChunkPos(p.getLocation().getChunk());
            UUID currentChunkOwner = claimChunk.getChunkHandler().getOwner(chunk);
            if (currentChunkOwner != null && currentChunkOwner.equals(p.getUniqueId())) {
                if (giveAccess(p, chunk, otherId, arguments)) {
                    Utils.toPlayer(
                            p, claimChunk.getMessages().accessHas.replace("%%PLAYER%%", accessor));
                }
            } else {
                // The executing player does not own the chunk they are currently standing in
                Utils.toPlayer(p, claimChunk.getMessages().giveNotYourChunk);
            }
        }
    }

    public void accessChunk(Player p, String[] accessors, Map<String, Boolean> arguments) {
        for (String accessor : accessors) accessChunk(p, accessor, arguments, accessors.length > 1);
    }

    private boolean giveAccess(
            Player owner, ChunkPos chunk, UUID other, Map<String, Boolean> permissions) {
        if (owner.getUniqueId().equals(other)) {
            Utils.toPlayer(owner, claimChunk.getMessages().accessOneself);
            return false;
        }

        // Get existing permissions for the accessor, and use them to populate values for
        // permissions not specified in the command (so any existing non-specified permissions
        // remain unchanged)
        PlayerHandler playerHandler = claimChunk.getPlayerHandler();

        Map<String, Boolean> existingPermissions = playerHandler.getPermissions(chunk, other);
        if (existingPermissions == null) {
            // The accessor has no permissions on this chunk, so use defaults for any permissions
            // not specified in the command
            existingPermissions = Utils.getDefaultPermissionsMap();
        }
        existingPermissions.forEach(permissions::putIfAbsent);

        playerHandler.changePermissions(chunk, other, permissions);
        return true;
    }

    public void checkAccess(Player p) {
        checkAccess(p, null);
    }

    public void checkAccess(Player p, String playerToQuery) {
        if (!Utils.hasPerm(p, true, "access")) {
            Utils.toPlayer(p, claimChunk.getMessages().accessNoPerm);
            return;
        }

        ChunkPos chunk = new ChunkPos(p.getLocation().getChunk());

        // I humbly apologize for the nightmarish labyrinth of nested if statements that follows
        if (playerToQuery != null) {
            // Get permissions for a single player
            UUID playerToQueryId = claimChunk.getPlayerHandler().getUUID(playerToQuery);
            if (playerToQueryId != null) {
                if (playerToQueryId.equals(claimChunk.getChunkHandler().getOwner(chunk))) {
                    // The given player owns this chunk
                    Utils.msg(
                            p,
                            claimChunk.getConfigHandler().getInfoColor()
                                    + " - "
                                    + claimChunk
                                            .getMessages()
                                            .checkAccessPlayerIsOwner
                                            .replace("%%PLAYER%%", playerToQuery));
                } else {
                    Map<String, Boolean> permissions =
                            claimChunk.getPlayerHandler().getPermissions(chunk, playerToQueryId);
                    if (permissions != null) {
                        // The given player has permissions on the chunk
                        Utils.msg(
                                p,
                                claimChunk.getConfigHandler().getInfoColor()
                                        + " - "
                                        + prepareCheckAccessPlayerHasAccessMsg(
                                                playerToQuery, permissions));
                    } else {
                        // The given player has no permissions on the chunk
                        Utils.msg(
                                p,
                                claimChunk.getConfigHandler().getInfoColor()
                                        + " - "
                                        + claimChunk
                                                .getMessages()
                                                .checkAccessPlayerNoAccess
                                                .replace("%%PLAYER%%", playerToQuery));
                    }
                }
            }
        } else {
            // Get permissions for all players with access
            Map<UUID, Map<String, Boolean>> allPlayerPermissions =
                    claimChunk.getPlayerHandler().getAllPlayerPermissions(chunk);
            if (allPlayerPermissions != null && !allPlayerPermissions.isEmpty()) {
                for (Map.Entry<UUID, Map<String, Boolean>> e : allPlayerPermissions.entrySet()) {
                    String playerWithAccessUsername =
                            claimChunk.getPlayerHandler().getUsername(e.getKey());
                    if (playerWithAccessUsername != null) {
                        // Output the permissions of everyone (other than the owner) with
                        // permissions on this chunk
                        Utils.msg(
                                p,
                                claimChunk.getConfigHandler().getInfoColor()
                                        + " - "
                                        + prepareCheckAccessPlayerHasAccessMsg(
                                                playerWithAccessUsername, e.getValue()));
                    }
                }
            } else {
                // No players (other than the owner) have permissions on this chunk
                Utils.msg(
                        p,
                        claimChunk.getConfigHandler().getInfoColor()
                                + " - "
                                + claimChunk.getMessages().checkAccessNoPlayersHaveAccess);
            }
        }
    }

    public void revokeAccess(Player p, String[] playersToRevoke, boolean allChunks) {
        if (!Utils.hasPerm(p, true, "access")) {
            Utils.toPlayer(p, claimChunk.getMessages().accessNoPerm);
            return;
        }

        if (playersToRevoke != null && 0 < playersToRevoke.length) {
            String message;
            ChunkPos[] chunks;
            if (allChunks) {
                // Revoke access to all the executing player's chunks
                chunks = claimChunk.getChunkHandler().getClaimedChunks(p.getUniqueId());
                message = claimChunk.getMessages().revokeAccessAllChunks;
            } else {
                // Revoke access only to the chunk the executor is currently standing in
                ChunkPos chunk = new ChunkPos(p.getLocation().getChunk());
                if (p.getUniqueId().equals(claimChunk.getChunkHandler().getOwner(chunk))) {
                    chunks = new ChunkPos[] {chunk};
                    message = claimChunk.getMessages().revokeAccessCurrentChunk;
                } else {
                    Utils.toPlayer(p, claimChunk.getMessages().giveNotYourChunk);
                    return;
                }
            }

            for (String username : playersToRevoke) {
                UUID userId = claimChunk.getPlayerHandler().getUUID(username);
                if (userId != null) {
                    for (ChunkPos c : chunks) {
                        claimChunk
                                .getPlayerHandler()
                                .changePermissions(c, userId, Utils.getAllFalsePermissionsMap());
                    }
                }
            }

            Utils.toPlayer(p, message);
        }
    }

    public void giveChunk(Player giver, Chunk chunk, String newOwner) {
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
            Utils.toPlayer(
                    giver, claimChunk.getMessages().giveNoPlayer.replace("%%PLAYER%%", newOwner));
            return;
        }

        // Get the receiving player's UUID
        UUID given = givenPly.getUniqueId();

        // Make sure the owner isn't trying to give the chunk to themselves
        if (giver.getUniqueId().equals(given)) {
            Utils.toPlayer(giver, claimChunk.getMessages().giveNotYourself);
            return;
        }

        // Make sure the receiving player doesn't have too many chunks already
        if (chunkHandler.getClaimed(given) >= rankHandler.getMaxClaimsForPlayer(givenPly)) {
            Utils.toPlayer(
                    giver, claimChunk.getMessages().giveChunksFull.replace("%%PLAYER%%", newOwner));
            return;
        }

        // Unclaim the chunk from the old owner
        chunkHandler.unclaimChunk(chunk.getWorld(), chunk.getX(), chunk.getZ());

        // Claim the chunk for the new owner
        ChunkPos newChunk =
                chunkHandler.claimChunk(chunk.getWorld(), chunk.getX(), chunk.getZ(), given);

        // Error check (it should never happen)
        if (newChunk == null) {
            Utils.toPlayer(giver, claimChunk.getMessages().giveError);
            Utils.err(
                    "Failed to give %s the chunk (%s, %s) in world %s from player %s",
                    newOwner,
                    chunk.getX(),
                    chunk.getZ(),
                    chunk.getWorld().getName(),
                    giver.getDisplayName());
            return;
        }

        // Tell the player they have given their chunk
        Utils.toPlayer(giver, claimChunk.getMessages().gaveChunk.replace("%%PLAYER%%", newOwner));

        // Tell the player (if they're online) that they have received a chunk
        Player onlineGiven = claimChunk.getServer().getPlayer(given);
        if (onlineGiven != null) {
            Utils.toPlayer(
                    onlineGiven,
                    claimChunk
                            .getMessages()
                            .givenChunk
                            .replace("%%PLAYER%%", giver.getDisplayName()));
        }
    }

    private String prepareCheckAccessPlayerHasAccessMsg(
            String playerUsername, Map<String, Boolean> permissions) {
        String message =
                claimChunk
                        .getMessages()
                        .checkAccessPlayerHasAccess
                        .replace("%%PLAYER%%", playerUsername);
        for (Map.Entry<String, Boolean> e : permissions.entrySet()) {
            message =
                    message.replace(
                            "%%" + e.getKey() + "%%",
                            e.getValue()
                                    ? claimChunk.getMessages().argTypeBoolTrue
                                    : claimChunk.getMessages().argTypeBoolFalse);
        }
        return message;
    }
}
