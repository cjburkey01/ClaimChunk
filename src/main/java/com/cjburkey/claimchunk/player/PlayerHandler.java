package com.cjburkey.claimchunk.player;

import com.cjburkey.claimchunk.Config;
import com.cjburkey.claimchunk.data.n.IClaimChunkDataHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerHandler {

    private final IClaimChunkDataHandler dataHandler;

    public PlayerHandler(IClaimChunkDataHandler dataHandler) {
        this.dataHandler = dataHandler;
    }

    /**
     * Toggles the supplied players access to the owner's chunks.
     *
     * @param owner  The chunk owner.
     * @param player The player to toggle access.
     * @return Whether or not the player NOW has access.
     */
    public boolean toggleAccess(UUID owner, UUID player) {
        if (hasAccess(owner, player)) {
            takeAccess(owner, player);
            return false;
        }
        giveAccess(owner, player);
        return true;
    }

    private void giveAccess(UUID owner, UUID player) {
        if (!hasAccess(owner, player)) {
            DataPlayer a = getPlayer(owner);
            if (a != null) a.permitted.add(player);
        }
    }

    public UUID[] getAccessPermitted(UUID owner) {
        DataPlayer a = getPlayer(owner);
        UUID[] out = new UUID[0];
        if (a != null) return a.permitted.toArray(out);
        return out;
    }

    private void takeAccess(UUID owner, UUID player) {
        if (hasAccess(owner, player)) {
            DataPlayer a = getPlayer(owner);
            if (a != null) a.permitted.remove(player);
        }
    }

    public boolean hasAccess(UUID owner, UUID player) {
        DataPlayer a = getPlayer(owner);
        if (a != null) {
            return a.permitted.contains(player)
                    || (Config.getBool("protection", "disableOfflineProtect") && Bukkit.getPlayer(owner) == null);
        }
        return false;
    }

    public void clearChunkName(UUID player) {
        DataPlayer a = getPlayer(player);
        if (a != null) a.chunkName = null;
    }

    public void setChunkName(UUID player, String name) {
        DataPlayer a = getPlayer(player);
        if (a != null) a.chunkName = name;
    }

    public String getChunkName(UUID player) {
        DataPlayer ply = getPlayer(player);
        if (ply != null && hasChunkName(player)) return ply.chunkName;
        return getUsername(player);
    }

    public boolean hasChunkName(UUID player) {
        return getPlayer(player) != null && getPlayer(player).chunkName != null;
    }

    public String getUsername(UUID player) {
        DataPlayer a = getPlayer(player);
        if (a != null) return a.lastIgn;
        return null;
    }

    public UUID getUUID(String username) {
        for (DataPlayer ply : dataHandler.getPlayers()) {
            if (ply.lastIgn != null && ply.lastIgn.equals(username)) return ply.player;
        }
        return null;
    }

    public DataPlayer[] getJoinedPlayers() {
        return dataHandler.getPlayers().toArray(new DataPlayer[0]);
    }

    public List<String> getJoinedPlayers(String start) {
        List<String> out = new ArrayList<>();
        for (DataPlayer ply : dataHandler.getPlayers()) {
            if (ply.lastIgn != null && ply.lastIgn.toLowerCase().startsWith(start.toLowerCase())) out.add(ply.lastIgn);
        }
        return out;
    }

    public void onJoin(Player ply) {
        if (getPlayer(ply.getUniqueId()) == null) dataHandler.addPlayer(new DataPlayer(ply));
    }

    public DataPlayer getPlayer(UUID owner) {
        if (dataHandler.hasPlayer(owner)) return dataHandler.getPlayer(owner);
        return null;
    }

}
