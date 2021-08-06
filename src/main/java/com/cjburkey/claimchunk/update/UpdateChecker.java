package com.cjburkey.claimchunk.update;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;
import javax.net.ssl.HttpsURLConnection;

// A not-too-flexible GitHub update checker designed by yours truly!
// Note: I had to use the GitHub /tags api because /releases/latest was always
//       a 404 for me? Not sure why.
public class UpdateChecker {

    private static Gson gson;

    private static String getRequest(URL url) throws IOException {
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            return br.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }

    private static GithubRelease[] getRepoReleases(URL url) throws IOException {
        String rawJson = getRequest(url);
        return getGson().fromJson(rawJson, GithubRelease[].class);
    }

    private static GithubRelease[] getRepoReleases(String url) throws IOException {
        return getRepoReleases(new URL(url));
    }

    private static GithubRelease[] getRepoReleases(String repoOwner, String repoName) throws IOException {
        return getRepoReleases(String.format("https://api.github.com/repos/%s/%s/releases", repoOwner, repoName));
    }

    @SuppressWarnings("SameParameterValue")
    public static SemVer getLatestRelease(String repoOwner, String repoName) throws IOException {
        GithubRelease[] tags = getRepoReleases(repoOwner, repoName);
        if (tags.length == 0) return null;
        if (tags.length > 1) Arrays.sort(tags, new GithubTagComparator());
        return tags[tags.length - 1].semVer;
    }

    private static Gson getGson() {
        if (gson == null) gson = new GsonBuilder().create();
        return gson;
    }

    private static class GithubRelease implements Comparable<GithubRelease> {

        // Assigned while reading from JSON response
        @SuppressWarnings("FieldMayBeFinal")
        private String name = null;

        // Lazily initialized
        private SemVer semVer;

        // Gets the semantic version for this release
        private SemVer getSemVer() {
            try {
                if (semVer == null && name != null) {
                    semVer = SemVer.fromString(name);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return semVer;
        }

        @Override
        public String toString() {
            try {
                return getSemVer().toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return name;
        }

        @Override
        public int compareTo(@Nullable GithubRelease o) {
            if (o == null) {
                return 0;
            }
            try {
                return getSemVer().compareTo(o.getSemVer());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        }

    }

    private static class GithubTagComparator implements Comparator<GithubRelease> {

        @Override
        public int compare(GithubRelease o1, GithubRelease o2) {
            return o1.compareTo(o2);
        }

    }

}
