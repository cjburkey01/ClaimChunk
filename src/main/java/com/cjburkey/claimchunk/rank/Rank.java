package com.cjburkey.claimchunk.rank;

import java.util.Objects;

/**
 * Created by CJ Burkey on 2019/04/25
 */
public class Rank {

    public final String name;
    final String permName;
    final int claims;

    public Rank(String name, int claims) {
        this.name = name;
        permName = "claimchunk.claim." + name;
        this.claims = claims;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rank rank = (Rank) o;
        return claims == rank.claims &&
                Objects.equals(name, rank.name);
    }

    public int hashCode() {
        return Objects.hash(name, claims);
    }

}
