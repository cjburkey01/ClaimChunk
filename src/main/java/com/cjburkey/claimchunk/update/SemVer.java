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
        // If the other major is larger than this one, this one is older
        if (o.major > major) return -1;
        // If this major is larger, this one is newer
        if (o.major < major) return 1;

        // Same as major handling
        if (o.minor > minor) return -1;
        if (o.minor < minor) return 1;

        // Same as major/minor handling
        if (o.patch > patch) return -1;
        if (o.patch < patch) return 1;

        boolean thisIsFixMarker = marker != null && marker.toUpperCase().startsWith("FIX");
        boolean otherIsFixMarker = o.marker != null && o.marker.toUpperCase().startsWith("FIX");

        // If the other version has a "FIX" marker but this version has no marker, the fix is newer.
        if (marker == null && o.marker != null && otherIsFixMarker) {
            return -1;
        }
        // If the other version doesn't have a marker but this one has a fix marker, this one is
        // newer.
        if (o.marker == null && marker != null && thisIsFixMarker) {
            return 1;
        }

        if (marker != null) {
            // If both versions have a marker, do string comparison (they should end with numbers to
            // differentiate them, such as RC1, RC2, etc.)
            if (o.marker != null) {
                // If this version has a fix marker but the other one doesn't, this one is newer
                if (thisIsFixMarker && !otherIsFixMarker) {
                    return 1;
                }
                // If this version doesn't have a fix marker but the other one does, the other
                // version is newer.
                if (otherIsFixMarker && !thisIsFixMarker) {
                    return -1;
                }

                return marker.compareTo(o.marker);
            }

            // If there is a marker on this one but not a marker on the other one, this one is OLDER
            return -1;
        }

        // If the other version has a marker but this one does not (and it's not a FIX marker), this
        // version is newer.
        if (o.marker != null) {
            return 1;
        }

        // Versions are equal
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
