package com.cjburkey.claimchunk.event;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.chunk.AutoClaimHandler;

public class PlayerConnectionHandler implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        ClaimChunk.getInstance().getPlayerHandler().onJoin(e.getPlayer());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        AutoClaimHandler.disable(e.getPlayer());
    }

}