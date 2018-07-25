package com.cjburkey.claimchunk.cmd;

public class Argument {

    private String arg;
    private TabCompletion tab;

    public Argument(String arg, TabCompletion tab) {
        this.arg = arg;
        this.tab = tab;
    }

    public String getArgument() {
        return arg;
    }

    public TabCompletion getCompletion() {
        return tab;
    }

    public String toString() {
        return arg;
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((arg == null) ? 0 : arg.hashCode());
        result = prime * result + ((tab == null) ? 0 : tab.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Argument other = (Argument) obj;
        if (arg == null) {
            if (other.arg != null)
                return false;
        } else if (!arg.equals(other.arg))
            return false;
        if (tab != other.tab)
            return false;
        return true;
    }

    public static enum TabCompletion {

        NONE, COMMAND, ONLINE_PLAYER, OFFLINE_PLAYER,

    }

}