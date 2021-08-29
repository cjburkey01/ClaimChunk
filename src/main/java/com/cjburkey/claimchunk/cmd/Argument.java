package com.cjburkey.claimchunk.cmd;

import java.util.Objects;

@Deprecated
public class Argument {

    private final String arg;
    private final TabCompletion tab;

    /**
     * Create an argument with the provided name and tab completion policy.
     *
     * @param arg The name of the argument.
     * @param tab The tab completion policy.
     */
    public Argument(String arg, TabCompletion tab) {
        this.arg = arg;
        this.tab = tab;
    }

    /**
     * Get the name of the argument.
     *
     * @return The name of the argument.
     */
    public String getArgument() {
        return arg;
    }

    /**
     * Get the tab completion policy.
     *
     * @return The tab completion policy.
     */
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

        /**
         * No tab completion should occur.
         */
        NONE,

        /**
         * Tab completion should include claim chunk commands.
         */
        COMMAND,

        /**
         * Tab completion should include all online players.
         */
        ONLINE_PLAYER,

        /**
         * Tab completion should include all players that have joined the server.
         */
        OFFLINE_PLAYER,

        /**
         * Tab completion should be either `true` or `false`.
         */
        BOOLEAN,

    }

}
