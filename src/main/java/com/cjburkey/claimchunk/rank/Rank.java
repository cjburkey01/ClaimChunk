package com.cjburkey.claimchunk.rank;

import java.util.Objects;

public class Rank {

    public final String name;
    transient final String permName;
    int claims;

    Rank(String name, int claims) {
        this.name = name;
        permName = "claim." + name;
        this.claims = claims;
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
