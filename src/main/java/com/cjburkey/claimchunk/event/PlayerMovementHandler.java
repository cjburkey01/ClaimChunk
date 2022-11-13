package com.cjburkey.claimchunk.event;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.chunk.AutoClaimHandler;
import com.cjburkey.claimchunk.chunk.ChunkHandler;
import com.cjburkey.claimchunk.player.PlayerHandler;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashSet;
import java.util.UUID;

public class PlayerMovementHandler implements Listener {

    private final ClaimChunk claimChunk;
    private final HashSet<UUID> previouslyDetected = new HashSet<>();

    public PlayerMovementHandler(ClaimChunk claimChunk) {
        this.claimChunk = claimChunk;
    }

    // TODO: MAKE THIS MORE EFFICIENT
    // TODO: MOVE THE MESSAGES LOGIC INTO THE MESSAGES CLASS

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (e != null && !e.isCancelled() && e.getTo() != null) {
            // Get the previous and current chunks
            Chunk prev = e.getFrom().getChunk();
            Chunk to = e.getTo().getChunk();

            // Make sure the player moved into a new chunk
            if (prev.getX() != to.getX() || prev.getZ() != to.getZ()) {
                // If the claim is currently auto-claiming, try to claim this chunk
                if (AutoClaimHandler.inList(e.getPlayer())) {
                    claimChunk.getMainHandler().claimChunk(e.getPlayer(), to);
                    return;
                }

                ChunkHandler ch = claimChunk.getChunkHandler();

                // Send our shiny new "enter new chunk" event
                Bukkit.getPluginManager()
                        .callEvent(
                                new PlayerEnterChunkEvent(
                                        e.getPlayer(),
                                        prev,
                                        to,
                                        ch.getOwner(prev),
                                        ch.getOwner(to)));
            }
        }
    }

    // Listen for the event (which is only an event because it might be useful)
    // For example, another plugin wanting to change or disable this message
    // could catch and cancel this event.
    @EventHandler
    public void onPlayerEnterChunk(PlayerEnterChunkEvent e) {
        // If this player has already received a message recently, give them a breath
        if (e != null && !e.isCancelled() && !previouslyDetected.contains(e.player.getUniqueId())) {
            final Chunk to = e.nextChunk;

            final boolean lastClaimed = e.previousOwner != null;

            // Check if the new chunk is already claimed
            if (e.nextOwner != null) {
                // If the new chunk and the previous chunk were claimed, check if the owners
                // differ
                if (lastClaimed) {
                    // Only display the new chunk's owner if they differ from the previous
                    // chunk's owner
                    if (!e.chunksHaveSameOwner) {
                        showTitle(e.player, to);
                    }
                } else {
                    // Show the player the chunk's owner
                    showTitle(e.player, to);
                }
            } else {
                // The player entered an unclaimed chunk from a claimed chunk
                if (lastClaimed) {
                    String name = claimChunk.getPlayerHandler().getChunkName(e.previousOwner);
                    String msg;
                    if (e.isPlayerPreviousOwner) {
                        msg = claimChunk.getMessages().chunkLeaveSelf;
                    } else if (name == null) {
                        msg = claimChunk.getMessages().chunkLeaveUnknown;
                    } else {
                        msg = claimChunk.getMessages().chunkLeave.replace("%%PLAYER%%", name);
                    }
                    if (!msg.isBlank()) {
                        toPlayer(e.player, msg);
                    }
                }
            }
        }
    }

    private void showTitle(Player player, Chunk newChunk) {
        // Get the UUID of the new chunk owner
        UUID newOwner =
                claimChunk
                        .getChunkHandler()
                        .getOwner(newChunk.getWorld(), newChunk.getX(), newChunk.getZ());

        // Check if this player doesn't own the new chunk
        if (newOwner != null && !player.getUniqueId().equals(newOwner)) {
            // Get the name of the chunks for the owner of this chunk and display it
            PlayerHandler ph = claimChunk.getPlayerHandler();
            String newName = ph.getChunkName(newOwner);
            String text =
                    ((newName == null)
                            ? claimChunk.getMessages()
                                    .unknownChunkOwner // Something probably went wrong with the
                            // PlayerHandler
                            : claimChunk.getMessages().chunkOwner.replace("%%PLAYER%%", newName));
            showTitleRaw(true, player, text);

            // Send a message to the chunk owner if possible
            if (!Utils.hasPerm(player, false, "invis") && ph.hasAlerts(newOwner)) {
                Player owner = Bukkit.getPlayer(newOwner);
                if (owner != null) {
                    if (owner.canSee(player)
                            || !claimChunk.getConfigHandler().getHideAlertsForVanishedPlayers()) {
                        showTitleRaw(
                                false,
                                owner,
                                claimChunk
                                        .getMessages()
                                        .playerEnterChunk
                                        .replace("%%PLAYER%%", player.getDisplayName()));
                    }
                }
            }
        } else {
            // This chunk is owned by this player
            showTitleRaw(true, player, claimChunk.getMessages().chunkSelf);
        }
    }

    private void showTitleRaw(boolean isOwnerDisplay, Player player, String msg) {
        if ((claimChunk.getConfigHandler().getDisplayNameOfOwner() || !isOwnerDisplay)
                && !msg.isBlank()) {
            toPlayer(player, msg);
        }
    }

    // Wrapper method that also schedules a delayed task to prevent chat spam.
    private void toPlayer(Player player, String msg) {
        Utils.toPlayer(player, msg);
        final UUID ply = player.getUniqueId();
        // Run a SYNC (main thread) task to remove the given user's UUID from
        // the cache
        claimChunk
                .getServer()
                .getScheduler()
                .scheduleSyncDelayedTask(
                        claimChunk,
                        () -> previouslyDetected.remove(ply),
                        claimChunk.getConfigHandler().getChunkEnterExitSpamDelay());
    }
}
