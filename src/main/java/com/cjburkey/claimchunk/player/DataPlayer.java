package com.cjburkey.claimchunk.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.bukkit.entity.Player;

public class DataPlayer {

    public UUID player;
    public String lastIgn;
    List<UUID> permitted = new ArrayList<>();
    String chunkName;
    public long lastOnlineTime;
    public boolean unclaimedAllChunks;

    private DataPlayer() {
    }

    DataPlayer(Player player, UUID... permitted) {
        this.player = player.getUniqueId();
        lastIgn = player.getName();
        Collections.addAll(this.permitted, permitted);
        chunkName = null;
    }

    DataPlayer(UUID id, String name) {
        this.player = id;
        this.lastIgn = name;
        chunkName = null;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public DataPlayer clone() {
        DataPlayer ret = new DataPlayer();
        ret.player = player;
        ret.lastIgn = lastIgn;
        ret.permitted = permitted;
        ret.chunkName = chunkName;
        return ret;
    }

}
