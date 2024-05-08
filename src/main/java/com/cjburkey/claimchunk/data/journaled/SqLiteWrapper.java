package com.cjburkey.claimchunk.data.journaled;

import lombok.Getter;

import java.io.File;

public class SqLiteWrapper {

    private final File dbFile;

    public SqLiteWrapper(File dbFile) {
        this.dbFile = dbFile;
    }
}
