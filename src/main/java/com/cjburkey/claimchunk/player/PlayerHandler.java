package com.cjburkey.claimchunk.player;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.chunk.ChunkPlayerPermissions;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.data.newdata.IClaimChunkDataHandler;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerHandler {

    private final IClaimChunkDataHandler dataHandler;
    private final ClaimChunk claimChunk;

    public PlayerHandler(IClaimChunkDataHandler dataHandler, ClaimChunk claimChunk) {
        this.dataHandler = dataHandler;
        this.claimChunk = claimChunk;
    }

    public Collection<SimplePlayerData> getJoinedPlayers() {
        return dataHandler.getPlayers();
    }

    public List<String> getJoinedPlayersFromName(String start) {
        List<String> out = new ArrayList<>();
        for (SimplePlayerData ply : dataHandler.getPlayers()) {
            if (ply.lastIgn() != null
                    && ply.lastIgn().toLowerCase().startsWith(start.toLowerCase())) {
                out.add(ply.lastIgn());
            }
        }
        return out;
    }

    public boolean hasPermission(String permission, ChunkPos chunk, UUID player) {
        Map<String, Boolean> permissions = getPermissions(chunk, player);
        return permissions != null && permissions.getOrDefault(permission, false);
    }

    public boolean toggleAlerts(UUID player) {
        boolean newVal = !hasAlerts(player);
        dataHandler.setPlayerReceiveAlerts(player, newVal);
        return newVal;
    }

    public boolean hasAlerts(UUID owner) {
        return dataHandler.getPlayerReceiveAlerts(owner);
    }

    public Map<String, Boolean> getPermissions(ChunkPos chunk, UUID player) {
        Map<UUID, ChunkPlayerPermissions> permissionsOnChunk =
                dataHandler.getPlayersWithAccess(chunk);
        if (permissionsOnChunk != null && permissionsOnChunk.containsKey(player)) {
            return permissionsOnChunk.get(player).toPermissionsMap();
        }
        // Player has no permissions on the given chunk
        return null;
    }

    public Map<String, Boolean> getDefaultPermissions(UUID player) {
        return dataHandler.getDefaultPermissionsForPlayer(player);
    }

    public Map<UUID, Map<String, Boolean>> getAllPlayerPermissions(ChunkPos chunk) {
        // Get all players with permissions on the given chunk, and what permissions they have
        Map<UUID, ChunkPlayerPermissions> permissionsOnChunk =
                dataHandler.getPlayersWithAccess(chunk);
        if (permissionsOnChunk != null) {
            return permissionsOnChunk.entrySet().stream()
                    .collect(
                            Collectors.toMap(
                                    Map.Entry::getKey, e -> e.getValue().toPermissionsMap()));
        }
        return null;
    }

    public void changePermissions(ChunkPos chunk, UUID accessor, Map<String, Boolean> permissions) {
        if (permissions.values().stream().noneMatch(v -> v)) {
            // All permissions are false, so remove the accessor's access entirely
            dataHandler.takePlayerAccess(chunk, accessor);
        } else {
            ChunkPlayerPermissions permissionsObject =
                    ChunkPlayerPermissions.fromPermissionsMap(permissions);
            dataHandler.givePlayerAccess(chunk, accessor, permissionsObject);
        }
    }

    public void setChunkName(UUID owner, String name) {
        dataHandler.setPlayerChunkName(owner, name);
    }

    public void clearChunkName(UUID owner) {
        setChunkName(owner, null);
    }

    @Nullable
    public String getChunkName(UUID owner) {
        String chunkName = dataHandler.getPlayerChunkName(owner);
        if (chunkName != null) return chunkName;
        return dataHandler.getPlayerUsername(owner);
    }

    public boolean hasChunkName(UUID owner) {
        return dataHandler.getPlayerChunkName(owner) != null;
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

    public void setMaxClaims(UUID player, int maxClaims) {
        dataHandler.setPlayerExtraMaxClaims(player, maxClaims);
    }

    // Use negative to take
    public void addOrTakeMaxClaims(UUID player, int claimsToAdd) {
        if (claimsToAdd > 0) dataHandler.addPlayerExtraMaxClaims(player, claimsToAdd);
        else if (claimsToAdd < 0) dataHandler.takePlayerExtraMaxClaims(player, -claimsToAdd);
    }

    public int getMaxClaims(UUID player) {
        return dataHandler.getPlayerExtraMaxClaims(player);
    }

    public void onJoin(Player ply) {
        UUID uuid = ply.getUniqueId();
        if (dataHandler.hasPlayer(uuid)) {
            dataHandler.setPlayerLastOnline(uuid, System.currentTimeMillis());
        } else {
            dataHandler.addPlayer(
                    uuid,
                    ply.getName(),
                    claimChunk.getConfigHandler().getDefaultSendAlertsToOwner(),
                    0);
        }
    }
}
