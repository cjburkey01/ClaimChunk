package com.cjburkey.claimchunk.database;

/**
 * @since 0.0.6
 */
class DatabaseException extends Exception {

    private static final long serialVersionUID = -3885910772192057111L;

    /**
     * Constructs a <code>DatabaseException</code> object with a given
     * <code>reason</code>.
     *
     * @param reason a description of the exception
     */
    DatabaseException(String reason) {
        super(reason);
    }

}
