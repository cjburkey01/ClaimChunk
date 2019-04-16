package com.cjburkey.claimchunk.data;

import java.util.List;

/**
 * Classes implementing this interface represent some form of data management system.
 * For example, JSON and SQL Databases are two data management systems used by ClaimChunk
 *
 * @param <T> The type of data stored by this given system
 */
public interface IDataStorage<T> {

    /**
     * Provides a list with all of the data within this system.
     * There is no guarantee that this returns a mutable list, so it is assumed to be immutable.
     *
     * @return A list with all of the data within this system
     */
    List<T> getData();

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

}
