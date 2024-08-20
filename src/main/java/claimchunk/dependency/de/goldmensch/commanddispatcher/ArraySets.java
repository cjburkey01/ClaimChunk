package claimchunk.dependency.de.goldmensch.commanddispatcher;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ArraySets {
    public static @NotNull <T> T[] getBiggest(@NotNull Set<T[]> set) {
        @SuppressWarnings("unchecked")
        T[] biggest = (T[]) new Object[0];

        for (T[] c : set) {
            if (c.length > biggest.length) {
                biggest = c;
            }
        }

        return biggest;
    }
}
