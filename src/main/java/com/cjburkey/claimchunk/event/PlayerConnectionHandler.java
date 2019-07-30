package com.cjburkey.claimchunk.event;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Config;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.chunk.AutoClaimHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@SuppressWarnings("unused")
public class PlayerConnectionHandler implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (Config.getBool("basic", "checkForUpdates")
                && ClaimChunk.getInstance().isUpdateAvailable()
                && e.getPlayer().hasPermission("claimchunk.update")) {
            Utils.msg(e.getPlayer(),
                    String.format("&l&aAn update is available for ClaimChunk! Current version: %s | Latest version: %s",
                            ClaimChunk.getInstance().getVersion(), ClaimChunk.getInstance().getAvailableVersion()));
        }
        ClaimChunk.getInstance().getPlayerHandler().onJoin(e.getPlayer());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        AutoClaimHandler.disable(e.getPlayer());
    }

}
