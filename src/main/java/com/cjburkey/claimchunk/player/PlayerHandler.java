package com.cjburkey.claimchunk.player;

import com.cjburkey.claimchunk.Config;
import com.cjburkey.claimchunk.data.n.IClaimChunkDataHandler;
import com.cjburkey.claimchunk.data.n.SimplePlayerData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.bukkit.entity.Player;

public class PlayerHandler {

    private final IClaimChunkDataHandler dataHandler;

    public PlayerHandler(IClaimChunkDataHandler dataHandler) {
        this.dataHandler = dataHandler;
    }

    public Collection<SimplePlayerData> getJoinedPlayers() {
        return dataHandler.getPlayers();
    }

    // Returns whether the player NOW has access
    public boolean toggleAccess(UUID owner, UUID player) {
        boolean newVal = !hasAccess(owner, player);
        dataHandler.setPlayerAccess(owner, player, newVal);
        return newVal;
    }

    public boolean hasAccess(UUID owner, UUID player) {
        return dataHandler.playerHasAccess(owner, player);
    }

    public UUID[] getAccessPermitted(UUID owner) {
        return dataHandler.getPlayersWithAccess(owner);
    }

    public boolean toggleAlerts(UUID uniqueId) {
        boolean newVal = !hasAlerts(uniqueId);
        dataHandler.setPlayerReceiveAlerts(uniqueId, newVal);
        return newVal;
    }

    public boolean hasAlerts(UUID newOwner) {
        return dataHandler.getPlayerReceiveAlerts(newOwner);
    }

    public void setChunkName(UUID player, String name) {
        dataHandler.setPlayerChunkName(player, name);
    }

    public void clearChunkName(UUID player) {
        setChunkName(player, null);
    }

    public String getChunkName(UUID player) {
        String chunkName = dataHandler.getPlayerChunkName(player);
        if (chunkName != null) return chunkName;
        return dataHandler.getPlayerUsername(player);
    }

    public boolean hasChunkName(UUID player) {
        return dataHandler.getPlayerChunkName(player) != null;
    }

    public String getUsername(UUID player) {
        return dataHandler.getPlayerUsername(player);
    }

    public UUID getUUID(String username) {
        return dataHandler.getPlayerUUID(username);
    }

    public void setLastJoinedTime(UUID player, long time) {
        dataHandler.setPlayerLastOnline(player, time);
    }

    public List<String> getJoinedPlayers(String start) {
        List<String> out = new ArrayList<>();
        for (SimplePlayerData ply : dataHandler.getPlayers()) {
            if (ply.lastIgn != null && ply.lastIgn.toLowerCase().startsWith(start.toLowerCase())) out.add(ply.lastIgn);
        }
        return out;
    }

    public void onJoin(Player ply) {
        if (!dataHandler.hasPlayer(ply.getUniqueId())) {
            dataHandler.addPlayer(ply.getUniqueId(),
                    ply.getName(),
                    Config.getBool("chunks", "defaultSendAlertsToOwner"));
        }
    }

}
