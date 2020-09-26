package com.cjburkey.claimchunk.config.ccconfig;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class NSKey {

    private final ArrayList<String> category;
    public final String key;

    public NSKey(@Nullable String namespacedKey) {
        category = new ArrayList<>();
        if (namespacedKey == null) {
            key = "";
            return;
        }

        String[] split = namespacedKey.split("\\.");
        if (split.length <= 0) {
            key = "";
        } else {
            key = split[split.length - 1];
            category.addAll(Arrays.asList(split)
                                  .subList(0, split.length - 1));
        }
    }

    @SuppressWarnings("unused")
    public List<String> category() {
        return category;
    }

    public String categories() {
        return String.join("", category);
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        for (String cat : category) output.append(cat).append('.');
        return output.append(key).toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NSKey nsKey = (NSKey) o;
        return category.equals(nsKey.category) && Objects.equals(key, nsKey.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(category, key);
    }
}
