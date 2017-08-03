package com.cjburkey.claimchunk.event;

import java.io.IOException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import com.cjburkey.claimchunk.ClaimChunk;

public class PlayerJoinHandler implements Listener {
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		try {
			ClaimChunk.getInstance().getPlayerHandler().onJoin(e.getPlayer());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
}