package com.cjburkey.claimchunk.player;

import com.cjburkey.claimchunk.data.n.SimplePlayerData;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DataPlayer implements Cloneable {

    public final UUID player;
    public final String lastIgn;
    public final Set<UUID> permitted;
    public String chunkName;
    public long lastOnlineTime;
    public boolean alert;

    public DataPlayer(UUID player,
                      String lastIgn,
                      Set<UUID> permitted,
                      String chunkName,
                      long lastOnlineTime,
                      boolean alert) {
        this.player = player;
        this.lastIgn = lastIgn;
        this.permitted = new HashSet<>(permitted);
        this.chunkName = chunkName;
        this.lastOnlineTime = lastOnlineTime;
        this.alert = alert;
    }

    private DataPlayer(DataPlayer clone) {
        this(clone.player,
                clone.lastIgn,
                clone.permitted,
                clone.chunkName,
                clone.lastOnlineTime,
                clone.alert);
    }

    public SimplePlayerData toSimplePlayer() {
        return new SimplePlayerData(player, lastIgn, lastOnlineTime);
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public DataPlayer clone() {
        return new DataPlayer(this);
    }

}
