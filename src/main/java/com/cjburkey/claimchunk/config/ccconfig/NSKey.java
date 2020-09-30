package com.cjburkey.claimchunk.config.ccconfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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

    @SuppressWarnings("unused")
    public String categories() {
        return String.join(".", category);
    }

    @SuppressWarnings("unused")
    public List<String> parentCategory() {
        if (category.size() <= 0) return new ArrayList<>();
        return category.subList(1, category.size());
    }

    @SuppressWarnings("unused")
    public boolean isParent(@Nonnull List<String> otherCat) {
        // Check if the provided category is a parent of this one
        for (int i = 0; i < category.size() && i < otherCat.size(); i ++) {
            if (!otherCat.get(i).equals(category.get(i))) {
                return i > 0;
            }
        }

        return true;
    }

    @SuppressWarnings("unused")
    public @Nonnull List<String> getRelativeCat(@Nonnull List<String> otherCat) {
        // Keep track of the same path elements.
        List<String> same = new ArrayList<>(category());
        int sameCount = 0;
        for (int i = 0; i < same.size() && i < otherCat.size(); i ++) {
            if (!otherCat.get(i).equals(same.get(i))) break;
            sameCount ++;
        }

        // If there are no elements that are the same, the path must be
        // absolute
        if (sameCount == 0) {
            same.add(0, "<<");
            return same;
        }

        // Cut off the parts of the label that were the same
        same = same.subList(sameCount, same.size());

        // Add the up level symbols
        for (int i = 0; i < (category().size() - sameCount); i ++) {
            same.add(0, "<");
        }

        return same;
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
