package com.cjburkey.claimchunk.config.ccconfig;

import org.jetbrains.annotations.NotNull;

/**
 * Represents an object that may be serialized into and from a ClaimChunk
 * config file.
 */
public interface ICCConfigSerializable {

    /**
     * Load the value at the provided key into this instance of this class,
     * assuming it is possible.
     *
     * @param config The ClaimChunk config from which to load this value.
     * @param key The key to search.
     */
    void fromCCConfig(@NotNull CCConfig config, @NotNull String key);

    /**
     * Stores this object's value at the particular key inside the provided
     * ClaimChunk config.
     *
     * @param config The ClaimChunk config to which to insert this value.
     * @param key The key at which to enter this value.
     */
    void toCCConfig(@NotNull CCConfig config, @NotNull String key);

}
