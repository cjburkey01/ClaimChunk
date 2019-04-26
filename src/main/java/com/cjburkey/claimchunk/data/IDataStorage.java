package com.cjburkey.claimchunk.data;

import java.util.Collection;
import java.util.Iterator;
import javax.annotation.Nonnull;

/**
 * Classes implementing this interface represent some form of data management system.
 * For example, JSON and SQL Databases are two data management systems used by ClaimChunk
 *
 * @param <T> The type of data stored by this given system
 */
public interface IDataStorage<T> extends Iterable<T> {

    /**
     * Provides a collection with all of the data within this system.
     * There is no guarantee that this returns a mutable collection, so it is assumed to be immutable.
     *
     * @return A collection with all of the data within this system
     */
    Collection<T> getData();

    /**
     * Adds the provided data into this data management system.
     * The system is not required to save its data to the disk/network after this operation,
     * that can only be guaranteed by manually calling {@link #saveData()}.
     *
     * @param data The data to be added to this system
     */
    void addData(T data);

    /**
     * Saves all unsaved changed in this system into offline storage.
     *
     * @throws Exception Any error that may occur during saving, usually a network/IO error
     */
    void saveData() throws Exception;

    /**
     * Loads the offline cache into this system.
     * The old data in this system will be lost after this call.
     *
     * @throws Exception Any error that may occur during loading, usually a network/IO error
     */
    void reloadData() throws Exception;

    /**
     * Empties all the data in this system without saving before or after.
     */
    void clearData();

    @Nonnull
    default Iterator<T> iterator() {
        return getData().iterator();
    }

}
