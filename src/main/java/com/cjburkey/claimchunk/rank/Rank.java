package com.cjburkey.claimchunk.rank;

import java.util.HashMap;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

public class Rank {

    int claims;
    private final String name;
    private transient Permission perm;

    Rank(String name, int claims) {
        this.name = name;
        this.claims = claims;
    }

    Permission getPerm() {
        if (perm == null) {
            perm = new Permission("claimchunk.claim." + name, "CLAIMCHUNK", PermissionDefault.FALSE, new HashMap<>());
            PluginManager pm = Bukkit.getServer().getPluginManager();
            if (pm.getPermission(getPerm().getName()) == null) pm.addPermission(getPerm());
        }
        return perm;
    }

    @Override
    public String toString() {
        return String.format("%s: %s claims", name, claims);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rank rank = (Rank) o;
        return claims == rank.claims &&
                Objects.equals(name, rank.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, claims);
    }

}
