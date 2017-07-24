package com.cjburkey.claimchunk.event;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import com.cjburkey.claimchunk.ClaimChunk;

public class PlayerJoinHandler implements Listener {
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		ClaimChunk.getInstance().getPlayers().onJoin(e.getPlayer());
	}
	
}