package com.cjburkey.claimchunk.event;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.chunk.AutoClaimHandler;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Objects;

public class PlayerConnectionHandler implements Listener {

    private final ClaimChunk claimChunk;

    public PlayerConnectionHandler(ClaimChunk claimChunk) {
        this.claimChunk = claimChunk;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (claimChunk.getConfigHandler().getCheckForUpdates()
                && claimChunk.isUpdateAvailable()
                && e.getPlayer().hasPermission("claimchunk.update")) {
            BaseComponent bc =
                    new TextComponent(
                            Utils.toComponent(
                                    e.getPlayer(),
                                    "&l&aAn update is available for ClaimChunk! Current version: &e"
                                            + claimChunk.getVersion()
                                            + "&a | Latest version: "));
            TextComponent link =
                    new TextComponent(
                            Objects.requireNonNull(claimChunk.getAvailableVersion()).toString());
            link.setColor(ChatColor.YELLOW);
            link.setUnderlined(true);
            link.setHoverEvent(
                    new HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            new Text("Go to the ClaimChunk downloads page")));
            link.setClickEvent(
                    new ClickEvent(
                            ClickEvent.Action.OPEN_URL,
                            "https://github.com/cjburkey01/ClaimChunk/releases"));
            bc.addExtra(link);

            Utils.msg(e.getPlayer(), new TextComponent(bc));
        }
        claimChunk.getPlayerHandler().onJoin(e.getPlayer());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        claimChunk.getAdminOverrideHandler().remove(e.getPlayer().getUniqueId());
        AutoClaimHandler.disable(e.getPlayer());
        claimChunk.getPlayerHandler().setLastJoinedTime(e.getPlayer().getUniqueId(), System.currentTimeMillis());
    }
}
