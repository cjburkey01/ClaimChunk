package com.cjburkey.claimchunk.event;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.config.ClaimChunkWorldProfile;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.UUID;

public class WorldProfileEventHandler implements Listener {

    private final ClaimChunk claimChunk;

    public WorldProfileEventHandler(ClaimChunk claimChunk) {
        this.claimChunk = claimChunk;
    }

    @EventHandler
    public void onEntityInteraction(PlayerInteractEntityEvent event) {
        if (event == null || event.isCancelled()) return;

        // Get necessary information
        final Chunk chunk = event.getRightClicked().getLocation().getChunk();
        final UUID chunkOwner = claimChunk.getChunkHandler().getOwner(chunk);
        final boolean hasAccess = chunkOwner != null && claimChunk.getPlayerHandler().hasAccess(chunkOwner, event.getPlayer().getUniqueId());

        // Get the profile for this world
        ClaimChunkWorldProfile profile = claimChunk.getProfileManager().getProfile(event.getRightClicked().getWorld().getName());

        // Delegate event cancellation to the world profile
        if (profile != null && !profile.onEntityEvent(chunkOwner, event.getPlayer(), hasAccess, event.getRightClicked(), ClaimChunkWorldProfile.EntityAccessType.INTERACT)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {

    }
}
