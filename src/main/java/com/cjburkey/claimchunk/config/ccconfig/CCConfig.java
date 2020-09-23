package com.cjburkey.claimchunk.config.ccconfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CCConfig {

    private static final String NULL_STR = "null";

    private final HashMap<String, String> values = new HashMap<>();
    private final String defaultString;

    @SuppressWarnings("unused")
    public CCConfig(String defaultString) {
        this.defaultString = defaultString;
    }

    public CCConfig(boolean defaultNull) {
        this(defaultNull ? null : "");
    }

    /**
     * Empties this config of all its properties
     */
    public void clear() {
        values.clear();
    }

    /**
     * Reads the input stream until the end and builds a config from the valid lines.
     * All of the properties are read into this config.
     *
     * @param inputStream The source of config.
     * @throws IOException Input reading error.
     */
    public void deserialize(InputStream inputStream) throws IOException {
        CCConfigParser.parse(this, inputStream);
    }

    public void serialize(OutputStream outputStream) {

    }

    /**
     * Assigns the value of the key in this config to the provided value as a string.
     *
     * @param key The key for which to update the value. This should not be null.
     * @param value The new value for the key. This may be null.
     * @param <T> The type of the new value.
     */
    public <T> void set(@Nonnull String key, @Nullable T value) {
        values.put(key, value == null ? NULL_STR : value.toString());
    }

    /**
     * Retrieves the string value for the provided key within this config.
     * If the key doesn't exist, the defaultString is returned.
     *
     * @param key The key for which to retrieve the value. This should not be null.
     * @return The value for the provided key or defaultString if
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
     * Retrieve a set of all of the properties within this config.
     *
     * @return A set of key-value entries for properties.
     */
    public Set<Map.Entry<String, String>> values() {
        return values.entrySet();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("{");
        for (HashMap.Entry<String, String> property : values.entrySet()) {
            builder.append(' ');
            builder.append(' ');
            builder.append(property.getKey());
            builder.append(' ');
            builder.append('=');
            builder.append(' ');
            builder.append(property.getValue());
            builder.append(',');
            builder.append('\n');
        }
        builder.append('}');
        return builder.toString();
    }

}
