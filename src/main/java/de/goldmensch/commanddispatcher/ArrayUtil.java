package de.goldmensch.commanddispatcher;

import org.jetbrains.annotations.NotNull;

public class ArrayUtil {

    private ArrayUtil() {}

    public static <T> boolean startWith(@NotNull T[] r, @NotNull T[] s) {
        if (s.length > r.length) return false;
        for (int i = 0; i < s.length; i++) {
            if (!r[i].equals(s[i])) {
                return false;
            }
        }
        return true;
    }

    public static @NotNull String[] toLowerCase(@NotNull String[] a) {
        var lowerArray = new String[a.length];
        for (int i = 0; i < a.length; i++) {
            lowerArray[i] = a[i].toLowerCase();
        }
        return lowerArray;
    }

    public static @NotNull String buildString(@NotNull String[] a) {
        var builder = new StringBuilder();
        for (String c : a) {
            builder.append(c);
            builder.append(" ");
        }
        return builder.toString().trim();
    }
}
