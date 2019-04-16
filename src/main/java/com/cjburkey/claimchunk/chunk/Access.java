package com.cjburkey.claimchunk.chunk;

import java.io.Serializable;
import java.util.UUID;
import org.bukkit.entity.Player;

/**
 * @author cjburkey
 * @deprecated DO NOT USE THIS, it is only for converting old data.
 */
@SuppressWarnings("DeprecatedIsStillUsed")
@Deprecated
public class Access implements Serializable {

    private static final long serialVersionUID = -4542563965031986866L;

    private final UUID owner;
    private final UUID allowed;

    @Deprecated
    public Access(UUID owner, UUID allowed) {
        this.owner = owner;
        this.allowed = allowed;
    }

    @Deprecated
    public Access(Player owner, UUID allowed) {
        this(owner.getUniqueId(), allowed);
    }

    @Deprecated
    public UUID getOwner() {
        return owner;
    }

    @Deprecated
    public UUID getAcessee() {
        return allowed;
    }

    @Override
    public String toString() {
        return owner.toString() + ';' + allowed.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((allowed == null) ? 0 : allowed.hashCode());
        result = prime * result + ((owner == null) ? 0 : owner.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Access other = (Access) obj;
        if (allowed == null) {
            if (other.allowed != null)
                return false;
        } else if (!allowed.equals(other.allowed))
            return false;
        if (owner == null) {
            return other.owner == null;
        } else return owner.equals(other.owner);
    }

}
