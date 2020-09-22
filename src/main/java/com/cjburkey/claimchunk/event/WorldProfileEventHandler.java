package com.cjburkey.claimchunk.event;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.config.ClaimChunkWorldProfile;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.projectiles.ProjectileSource;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class WorldProfileEventHandler implements Listener {

    private final ClaimChunk claimChunk;

    public WorldProfileEventHandler(ClaimChunk claimChunk) {
        this.claimChunk = claimChunk;
    }

    @EventHandler
    public void onEntityInteraction(PlayerInteractEntityEvent event) {
        if (event == null || event.isCancelled()) return;

        // Check if the player can interact with this entity
        onEntityEvent(() -> event.setCancelled(true),
                      event.getPlayer(),
                      event.getRightClicked(),
                      ClaimChunkWorldProfile.EntityAccessType.INTERACT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event == null || event.isCancelled()) return;

        // Get the entity that damaged this one
        Entity damager = event.getDamager();

        // If the cause of the damage is a projectile and the projectile's
        // shooter is a player, get the player
        if (damager instanceof Projectile) {
            ProjectileSource projectile = ((Projectile) damager).getShooter();
            damager = (projectile instanceof Player) ? (Player) projectile : null;
        }

        // If the entity damage comes from a natural cause, we don't need to
        // try to handle this event
        if (damager == null || damager.getType() != EntityType.PLAYER) return;
        Player damagingPlayer = (Player) damager;

        // Check if the player can damage this entity
        onEntityEvent(() -> event.setCancelled(true),
                      damagingPlayer,
                      event.getEntity(),
                      ClaimChunkWorldProfile.EntityAccessType.DAMAGE);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event == null || event.isCancelled()) return;

        // Check if the player can break this block
        onBlockEvent(() -> event.setCancelled(true),
                     event.getPlayer(),
                     event.getBlock(),
                     ClaimChunkWorldProfile.BlockAccessType.BREAK);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event == null || event.isCancelled()) return;

        // Check if the player can place this block
        onBlockEvent(() -> event.setCancelled(true),
                     event.getPlayer(),
                     event.getBlock(),
                     ClaimChunkWorldProfile.BlockAccessType.PLACE);
    }

    @EventHandler
    public void onBlockInteraction(PlayerInteractEvent event) {
        if (event == null
                || event.getClickedBlock() == null
                || event.getClickedBlock().getType() == Material.AIR
                || event.useInteractedBlock() == Event.Result.DENY) {
            return;
        }

        // Check if the player can interact with this block
        onBlockEvent(() -> event.setUseInteractedBlock(Event.Result.DENY),
                     event.getPlayer(),
                     event.getClickedBlock(),
                     ClaimChunkWorldProfile.BlockAccessType.INTERACT);
    }

    private void onEntityEvent(@Nonnull Runnable cancel,
                               @Nullable Player player,
                               @Nonnull Entity entity,
                               @Nonnull ClaimChunkWorldProfile.EntityAccessType accessType) {
        // Get necessary information
        final UUID ply = player != null ? player.getUniqueId() : null;
        final UUID chunkOwner = claimChunk.getChunkHandler().getOwner(entity.getLocation().getChunk());
        final boolean isOwner = (chunkOwner != null && chunkOwner.equals(ply));
        final boolean isOwnerOrAccess = isOwner || (chunkOwner != null && ply != null && claimChunk.getPlayerHandler().hasAccess(chunkOwner, ply));

        // Get the profile for this world
        ClaimChunkWorldProfile profile = claimChunk.getProfileManager().getProfile(entity.getWorld().getName());

        // Delegate event cancellation to the world profile
        if (profile != null && !profile.canAccessEntity(chunkOwner != null, isOwnerOrAccess, entity, accessType)) {
            cancel.run();
        }
    }

    private void onBlockEvent(@Nonnull Runnable cancel,
                              @Nullable Player player,
                              @Nonnull Block block,
                              @Nonnull ClaimChunkWorldProfile.BlockAccessType accessType) {
        // Get necessary information
        final UUID ply = player != null ? player.getUniqueId() : null;
        final UUID chunkOwner = claimChunk.getChunkHandler().getOwner(block.getLocation().getChunk());
        final boolean isOwner = (chunkOwner != null && player != null && chunkOwner.equals(ply));
        final boolean isOwnerOrAccess = isOwner || (chunkOwner != null && ply != null && claimChunk.getPlayerHandler().hasAccess(chunkOwner, ply));

        // Get the profile for this world
        ClaimChunkWorldProfile profile = claimChunk.getProfileManager().getProfile(block.getWorld().getName());

        // Delegate event cancellation to the world profile
        if (profile != null && !profile.canAccessBlock(chunkOwner != null, isOwnerOrAccess, block, accessType)) {
            cancel.run();
        }
    }

}
