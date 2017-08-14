package com.cjburkey.claimchunk.database;

/**
 * @since 0.0.6
 */
public class DatabaseException extends Exception {

    /**
     * Constructs a <code>DatabaseException</code> object with a given
     * <code>reason</code>.
     *
     * @param reason a description of the exception
     */
    public DatabaseException(String reason) {
        super(reason);
    }
}
