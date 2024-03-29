package com.cjburkey.claimchunk.update;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A nice little semantic versioning class. Feel free to use this in your own projects if you want
 * :)
 */
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
        try {
            final String[] split = version.trim().split("\\.");
            if (split.length != 3) {
                throw INVALID_SEMVER(version);
            }
            final String[] patchMarker = split[2].trim().split("-");
            if (patchMarker.length < 1 || patchMarker.length > 2) {
                throw INVALID_SEMVER(version);
            }

            final int major = Integer.parseInt(split[0].trim());
            final int minor = Integer.parseInt(split[1].trim());
            final int patch = Integer.parseInt(patchMarker[0].trim());
            final String marker = ((patchMarker.length == 2) ? patchMarker[1].trim() : null);
            return new SemVer(major, minor, patch, marker);
        } catch (Exception e) {
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

        if (o.patch > patch) return -1;
        if (o.patch < patch) return 1;

        if (marker != null && o.marker != null) {
            return marker.compareTo(o.marker);
        }

        if (marker == null && o.marker != null) {
            return 1;
        }

        if (marker != null) {
            return -1;
        }

        return 0;
    }

    public boolean isNewerThan(SemVer other) {
        return compareTo(other) > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SemVer semVer = (SemVer) o;
        return major == semVer.major
                && minor == semVer.minor
                && patch == semVer.patch
                && Objects.equals(marker, semVer.marker);
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
