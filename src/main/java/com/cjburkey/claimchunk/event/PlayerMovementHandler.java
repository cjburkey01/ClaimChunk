package com.cjburkey.claimchunk.event;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Config;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.chunk.AutoClaimHandler;
import com.cjburkey.claimchunk.chunk.ChunkHandler;
import com.cjburkey.claimchunk.cmd.MainHandler;
import com.cjburkey.claimchunk.player.PlayerHandler;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMovementHandler implements Listener {

    @SuppressWarnings("unused")
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (e != null && !e.isCancelled()) {
            Chunk prev = e.getFrom().getChunk();
            Chunk to = Objects.requireNonNull(e.getTo()).getChunk();
            if (prev != to) {
                if (AutoClaimHandler.inList(e.getPlayer())) {
                    MainHandler.claimChunk(e.getPlayer(), to);
                }
                ChunkHandler ch = ClaimChunk.getInstance().getChunkHandler();
                boolean lastClaimed = ch.isClaimed(prev.getWorld(), prev.getX(), prev.getZ());
                if (ch.isClaimed(to.getWorld(), to.getX(), to.getZ())) {
                    if (lastClaimed) {
                        UUID prevOwner = ch.getOwner(prev.getWorld(), prev.getX(), prev.getZ());
                        UUID newOwner = ch.getOwner(to.getWorld(), to.getX(), to.getZ());
                        if (!prevOwner.equals(newOwner)) {
                            showTitle(e.getPlayer(), to);
                        }
                    } else {
                        showTitle(e.getPlayer(), to);
                    }
                } else {
                    if (lastClaimed) {
                        Utils.toPlayer(e.getPlayer(), true, Config.getColor("infoColor"), Utils.getMsg("chunkLeave"));
                    }
                }
            }
        }
    }

    private void showTitle(Player player, Chunk newChunk) {
        if (!Config.getBool("chunks", "displayNameOfOwner")) {
            return;
        }
        UUID newOwner = ClaimChunk.getInstance().getChunkHandler().getOwner(newChunk.getWorld(), newChunk.getX(),
                newChunk.getZ());
        if (!newOwner.equals(player.getUniqueId())) {
            PlayerHandler nh = ClaimChunk.getInstance().getPlayerHandler();
            String newName = (nh.hasChunkName(newOwner)) ? nh.getChunkName(newOwner) : nh.getUsername(newOwner);
            String text = ((newName == null)
                    ? Utils.getMsg("unknownChunkOwner")
                    : Utils.getMsg("chunkOwner").replaceAll(Pattern.quote("%%PLAYER%%"), newName));
            Utils.toPlayer(player, true, Config.getColor("infoColor"), text);
        } else {
            Utils.toPlayer(player, true, Config.getColor("infoColor"), Utils.getMsg("chunkSelf"));
        }
    }

}
