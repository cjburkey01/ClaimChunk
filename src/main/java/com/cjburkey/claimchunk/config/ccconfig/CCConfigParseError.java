package com.cjburkey.claimchunk.config.ccconfig;

public final class CCConfigParseError {
    
    public final int startLine;
    public final int startIndex;
    public final int endLine;
    public final int endIndex;
    public final String source;
    public final String cause;

    public CCConfigParseError(int startLine, int startIndex, int endLine, int endIndex, String source, String cause) {
        this.startLine = startLine;
        this.startIndex = startIndex;
        this.endLine = endLine;
        this.endIndex = endIndex;
        this.source = source;
        this.cause = cause;
    }

}
