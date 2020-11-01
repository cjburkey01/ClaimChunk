package com.cjburkey.claimchunk.event;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.chunk.AutoClaimHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionHandler implements Listener {

    private final ClaimChunk claimChunk;

    public PlayerConnectionHandler(ClaimChunk claimChunk) {
        this.claimChunk = claimChunk;
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (claimChunk.chConfig().getBool("basic", "checkForUpdates")
                && claimChunk.isUpdateAvailable()
                && e.getPlayer().hasPermission("claimchunk.update")) {
            Utils.msg(e.getPlayer(),
                      String.format("&l&aAn update is available for ClaimChunk! Current version: &e%s&a | Latest version: &e%s&r",
                                    claimChunk.getVersion(),
                                    claimChunk.getAvailableVersion()));
        }
        claimChunk.getPlayerHandler().onJoin(e.getPlayer());
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        AutoClaimHandler.disable(e.getPlayer());
    }

}
