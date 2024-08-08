package com.cjburkey.claimchunk.flags;

import java.io.File;
import java.util.HashMap;

public class PermFlags {

    private final File flagsFile;
    private final HashMap<String, PermFlag> flagMap = new HashMap<>();

    public PermFlags(File flagsFile) {
        this.flagsFile = flagsFile;
    }
}
