package com.cjburkey.claimchunk.update;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A nice little semantic versioning class. Feel free to use this in your own projects if you want
 * :)
 */
public record SemVer(int major, int minor, int patch, String marker) implements Comparable<SemVer> {

    public static @NotNull SemVer fromString(@NotNull String version)
            throws IllegalArgumentException {
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
            final String marker =
                    ((patchMarker.length == 2) ? patchMarker[1].trim().toUpperCase() : null);
            return new SemVer(major, minor, patch, marker);
        } catch (Exception e) {
            throw INVALID_SEMVER(version, e);
        }
    }

    private static IllegalArgumentException INVALID_SEMVER(String input) {
        return new IllegalArgumentException("Invalid SemVer format: " + input);
    }

    private static IllegalArgumentException INVALID_SEMVER(String input, Throwable cause) {
        return new IllegalArgumentException("Invalid SemVer format: " + input, cause);
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

        // Quick hack fix, if I release 0.0.25 and then 0.0.25-FIX1, I want FIX1 to represent a
        // newer version because I've decided semver is a fuck and I can do what i want
        if (marker == null && o.marker != null && !o.marker.startsWith("FIX")) {
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
