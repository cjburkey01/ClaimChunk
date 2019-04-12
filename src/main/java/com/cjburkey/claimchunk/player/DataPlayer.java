package com.cjburkey.claimchunk.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.bukkit.entity.Player;

public class DataPlayer implements Cloneable {

    public UUID player;
    public String lastIgn;
    final List<UUID> permitted = new ArrayList<>();
    String chunkName;
    public long lastOnlineTime;
    public boolean unclaimedAllChunks;

    private DataPlayer(DataPlayer clone) {
        this.player = clone.player;
        this.lastIgn = clone.lastIgn;
        this.permitted.addAll(clone.permitted);
        this.chunkName = clone.chunkName;
        this.lastOnlineTime = clone.lastOnlineTime;
        this.unclaimedAllChunks = clone.unclaimedAllChunks;
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
        return new DataPlayer(this);
    }

}
