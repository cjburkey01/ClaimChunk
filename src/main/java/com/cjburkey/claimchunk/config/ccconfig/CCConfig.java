package com.cjburkey.claimchunk.config.ccconfig;

import com.cjburkey.claimchunk.Utils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CCConfig {

    private static final String NULL_STR = "null";

    private static final Pattern LIST_PAT = Pattern.compile("\\[\\s*?(.*?)\\s*?]");

    protected final HashMap<String, String> values = new HashMap<>();
    public String headerComment;
    public String defaultString;

    public CCConfig(@Nullable String headerComment, @Nullable String defaultString) {
        this.headerComment = headerComment;
        this.defaultString = defaultString;
    }

    /**
     * Empties this config of all its properties
     */
    @SuppressWarnings("unused")
    public void clear() {
        values.clear();
    }

    /**
     * Assigns the value of the key in this config to the provided value as a string.
     *
     * @param key The key for which to update the value. This should not be null.
     * @param value The new value for the key. This may be null.
     * @param <T> The type of the new value.
     */
    public <T> void set(@Nonnull String key, @Nullable T value) {
        // Normalize the null value
        String valStr = (value == null) ? NULL_STR : value.toString();
        
        // If the NULL_STR constant is null, then we might as well not add it
        if (valStr != null) values.put(key, valStr);
    }

    public <T> void setList(@Nonnull String key, @Nonnull Collection<T> list) {
        set(key, list.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.joining(", ", "[ ", " ]")));
    }

    /**
     * Get the value of the header comment initialized with this config. The comment of parsed config files will *not*
     * be saved.
     *
     * @return The header comment, or {@code ""} if empty.
     */
    public String headerComment() {
        return headerComment;
    }

    /**
     * Retrieves the string value for the provided key within this config.
     * If the key doesn't exist, the defaultString is returned.
     *
     * @param key The key for which to retrieve the value. This should not be null.
     * @return The value for the provided key or defaultString if the key wasn't found.
     */
    public String getStr(@Nonnull String key) {
        return values.getOrDefault(key, defaultString);
    }

    /**
     * Tries to parse an integer stored in this config with the given key.
     *
     * @param key The key for which to retrieve the integer. This should not be null.
     * @param defaultValue The default value to be returned if the integer isn't found.
     * @return The integer value for the provided key, or the provided default value if the key isn't found.
     */
    public int getInt(@Nonnull String key, int defaultValue) {
        try {
            return Integer.parseInt(getStr(key));
        } catch (Exception ignored) {}
        return defaultValue;
    }

    /**
     * Tries to parse a floating point number stored in this config with the given key.
     *
     * @param key The key for which to retrieve the float. This should not be null.
     * @param defaultValue The default value to be returned if the float isn't found.
     * @return The floating point value for the provided key, or the provided default value if the key isn't found.
     */
    @SuppressWarnings("unused")
    public float getFloat(@Nonnull String key, float defaultValue) {
        try {
            return Float.parseFloat(getStr(key));
        } catch (Exception ignored) {}
        return defaultValue;
    }

    /**
     * Tries to parse a boolean stored in this config with the given key.
     *
     * @param key The key for which to retrieve the boolean. This should not be null.
     * @param defaultValue The default value to be returned if the boolean isn't found.
     * @return The boolean value for the provided key, or the provided default value if the key isn't found.
     */
    public boolean getBool(@Nonnull String key, boolean defaultValue) {
        try {
            return Boolean.parseBoolean(getStr(key));
        } catch (Exception ignored) {}
        return defaultValue;
    }

    /**
     * Tries to parse a list from the value of a given key.
     *
     * @param key The key for which to retrieve the list. This should not be null.
     * @return A list representing the elements of the given key, or an empty list if the key is not found.
     */
    public List<String> getStrList(@Nonnull String key) {
        if (hasValue(key)) {
            // Get the raw value from the config and try to match a list
            // pattern against it
            String rawCfgStr = getStr(key);
            Matcher matcher = LIST_PAT.matcher(rawCfgStr);
            String listContents = null;
            if (matcher.find()) {
                try {
                    listContents = matcher.group(1);
                } catch (Exception e) {
                    Utils.err("Regex parsing error while parsing list \"%s\" for key \"%s\":", rawCfgStr, key);
                    e.printStackTrace();
                }
            }

            // Make sure the list was valid
            if (listContents == null) {
                Utils.err("Failed to parse list \"%s\" for key \"%s\"", rawCfgStr, key);
                return Collections.emptyList();
            }

            // Map the string contents to their values
            return Arrays.stream(listContents.split(","))
                    .map(String::trim)
                    .filter(str -> !str.isEmpty())
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    /**
     * Retrieves a set of all of the properties within this config.
     *
     * @return A set of key-value entries for properties.
     */
    public Set<Map.Entry<String, String>> values() {
        return values.entrySet();
    }

    /**
     * Checks whether the given key is contained within this config.
     *
     * @param key The key for which to determine existence.
     * @return Whether the given key has a non-null value within this config.
     */
    public boolean hasValue(@Nonnull String key) {
        return values.containsKey(key);
    }

    /**
     * Serializes this config into a string format.
     * 
     * @return The config in a string format.
     */
    @Override
    public String toString() {
        // Don't try to alphabetize by the keys of the properties because it
        // will take a lot of possibly unnecessary effort by default.
        return toString(false, 2, 1);
    }

    /**
     * Serialize this config into a string format.
     * 
     * @param alphabetize Whether or not to alphabetize the keys of the properties.
     * @param indentSpaces The number of spaces to indent each property.
     * @param surroundingSpaces The number of spaces around the equals sign.
     * @return The config in a string format.
     */
    public String toString(boolean alphabetize, int indentSpaces, int surroundingSpaces) {
        // Initialize the string and the values
        StringBuilder builder = new StringBuilder("{");
        Collection<HashMap.Entry<String, String>> properties = alphabetize
                // Sort the list if it should be alphabetized
                ? values.entrySet().stream().sorted(((o1, o2) -> {
                        if (o1 == o2) return 0;
                        if (o1 == null || o1.getKey() == null) return -1;
                        if (o2 == null || o2.getKey() == null) return 1;
                        return o1.getKey().compareTo(o2.getKey());
                    })).collect(Collectors.toList())
                // Return the unsorted set if the collection doesn't need to be
                // in order
                : values.entrySet();
        
        // Append each value
        for (HashMap.Entry<String, String> property : properties) {
            // Indent
            for (int i = 0; i < indentSpaces; i++) builder.append(' ');
            
            // Key
            builder.append(getOrDefault(property.getKey(), NULL_STR));
            
            // Equal sign and surrounding spaces
            for (int i = 0; i < surroundingSpaces; i++) builder.append(' ');
            builder.append('=');
            for (int i = 0; i < surroundingSpaces; i++) builder.append(' ');
            
            // The value, cap, and end of line
            builder.append(getOrDefault(property.getValue(), defaultString));
            builder.append(';');
            builder.append('\n');
        }
        
        // Cap the values off and return it
        builder.append('}');
        return builder.toString();
    }

    private static <T> T getOrDefault(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

}
