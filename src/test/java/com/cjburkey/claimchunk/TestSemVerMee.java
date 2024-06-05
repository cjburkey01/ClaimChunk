package com.cjburkey.claimchunk;

import com.cjburkey.claimchunk.update.SemVer;

import org.junit.jupiter.api.Test;

class TestSemVerMee {

    @Test
    void testVersions() {
        SemVer v0022 = new SemVer(0, 0, 22, null);
        SemVer v0024 = new SemVer(0, 0, 24, null);
        SemVer v0024FIX1 = new SemVer(0, 0, 24, "FIX1");
        SemVer v0024FIX2 = new SemVer(0, 0, 24, "FIX2");
        SemVer v0024RC1 = new SemVer(0, 0, 24, "RC1");
        SemVer v0024RC2 = new SemVer(0, 0, 24, "RC2");

        assert v0024.isNewerThan(v0022);
        assert v0024FIX1.isNewerThan(v0024);
        assert v0024FIX1.isNewerThan(v0024RC1);
        assert v0024FIX2.isNewerThan(v0024FIX1);
        assert v0024RC2.isNewerThan(v0024RC1);
        assert !v0024RC1.isNewerThan(v0024RC2);
    }
}
