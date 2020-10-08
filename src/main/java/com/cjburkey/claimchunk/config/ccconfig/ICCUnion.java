package com.cjburkey.claimchunk.config.ccconfig;

import javax.annotation.Nonnull;

/**
 * Represents a structure that can be merged with another structure.
 * 
 * @param <Type> The type of the other structure with which this structure can be merged.
 */
public interface ICCUnion<Type> {

    /**
     * Marge another structure into this one. This should overwrite values in this structure with duplicates from the
     * other structure.
     * 
     * @param other The structure to be merged into this one.
     */
    void union(@Nonnull Type other);
    
}
