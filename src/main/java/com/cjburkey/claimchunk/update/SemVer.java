package com.cjburkey.claimchunk.update;

import java.util.Objects;
import javax.annotation.Nullable;

public class SemVer implements Comparable<SemVer> {

    @SuppressWarnings("WeakerAccess")
    public final int major;

    @SuppressWarnings("WeakerAccess")
    public final int minor;

    @SuppressWarnings("WeakerAccess")
    public final int patch;

    @SuppressWarnings("WeakerAccess")
    public final String marker;

    private SemVer(int major, int minor, int patch, @Nullable String marker) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.marker = ((marker == null) ? null : marker.toUpperCase());
    }

    public static SemVer fromString(final String version) {
        final String[] split = version.trim().split("\\.");
        if (split.length != 3) throw INVALID_SEMVER(version);
        try {
            final String[] patchMarker = split[2].trim().split("-");
            if (patchMarker.length < 1 || patchMarker.length > 2) throw INVALID_SEMVER(version);

            final int major = Integer.parseInt(split[0].trim());
            final int minor = Integer.parseInt(split[1].trim());
            final int patch = Integer.parseInt(patchMarker[0].trim());
            final String marker = ((patchMarker.length == 2) ? patchMarker[1].trim() : null);
            return new SemVer(major, minor, patch, marker);
        } catch (NumberFormatException e) {
            throw INVALID_SEMVER(version);
        }
    }

    private static IllegalArgumentException INVALID_SEMVER(String input) {
        return new IllegalArgumentException("Invalid SemVer format: " + input);
    }

    @Override
    public int compareTo(SemVer o) {
        if (o.major > major) return -1;
        if (o.major < major) return 1;

        if (o.minor > minor) return -1;
        if (o.minor < minor) return 1;

        return Integer.compare(patch, o.patch);
    }

    public boolean isNewerThan(SemVer other) {
        return compareTo(other) > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SemVer semVer = (SemVer) o;
        return major == semVer.major &&
                minor == semVer.minor &&
                patch == semVer.patch &&
                Objects.equals(marker, semVer.marker);
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch, marker);
    }

    @Override
    public String toString() {
        if (marker != null) return String.format("%s.%s.%s-%s", major, minor, patch, marker);
        return String.format("%s.%s.%s", major, minor, patch);
    }

}
