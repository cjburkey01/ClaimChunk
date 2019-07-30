package com.cjburkey.claimchunk.update;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

    private static String getRequest(String url) throws IOException {
        URL https_url = new URL(url);
        HttpsURLConnection connection = (HttpsURLConnection) https_url.openConnection();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            return br.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }

    private static GithubTag[] getRepoTags(String repoOwner, String repoName) throws IOException {
        String rawJson = getRequest(String.format("https://api.github.com/repos/%s/%s/tags", repoOwner, repoName));
        return getGson().fromJson(rawJson, GithubTag[].class);
    }

    @SuppressWarnings("SameParameterValue")
    public static SemVer getLatestTag(String repoOwner, String repoName) throws IOException {
        GithubTag[] tags = getRepoTags(repoOwner, repoName);
        if (tags.length == 0) return null;
        if (tags.length > 1) Arrays.sort(tags, new GithubTagComparator());
        return tags[tags.length - 1].getSemVer();
    }

    private static Gson getGson() {
        if (gson == null) gson = new GsonBuilder().create();
        return gson;
    }

    private static class GithubTag implements Comparable<GithubTag> {

        private String name;

        private SemVer semVer;

        private GithubTag(String name) {
            this.name = name;
        }

        private SemVer getSemVer() {
            try {
                if (semVer == null) semVer = SemVer.fromString(name);
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

        @SuppressWarnings("NullableProblems")
        @Override
        public int compareTo(GithubTag o) {
            try {
                return getSemVer().compareTo(o.getSemVer());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        }

    }

    private static class GithubTagComparator implements Comparator<GithubTag> {

        @Override
        public int compare(GithubTag o1, GithubTag o2) {
            return o1.compareTo(o2);
        }

    }

}
