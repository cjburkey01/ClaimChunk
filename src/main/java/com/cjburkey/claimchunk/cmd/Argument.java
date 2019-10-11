package com.cjburkey.claimchunk.cmd;

import java.util.Objects;

public class Argument {

    private final String arg;
    private final TabCompletion tab;

    public Argument(String arg, TabCompletion tab) {
        this.arg = arg;
        this.tab = tab;
    }

    public String getArgument() {
        return arg;
    }

    @SuppressWarnings("WeakerAccess")
    public TabCompletion getCompletion() {
        return tab;
    }

    @Override
    public String toString() {
        return arg;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Argument argument = (Argument) o;
        return Objects.equals(arg, argument.arg) && tab == argument.tab;
    }

    @Override
    public int hashCode() {
        return Objects.hash(arg, tab);
    }

    public enum TabCompletion {

        NONE, COMMAND, ONLINE_PLAYER, OFFLINE_PLAYER, BOOLEAN

    }

}
