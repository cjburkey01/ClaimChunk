package com.cjburkey.claimchunk.update;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;

// A not-too-flexible GitHub update checker designed by yours truly!
public class UpdateChecker {

    public static final String repoOwner = "cjburkey01";
    public static final String repoName = "ClaimChunk";

    private static Gson gson;

    private static String getRequest(URL url)
            throws URISyntaxException, InterruptedException, IOException {
        // Create the HTTP connection handler (basically?)
        HttpClient client =
                HttpClient.newBuilder()
                        .version(HttpClient.Version.HTTP_1_1)
                        .followRedirects(HttpClient.Redirect.NORMAL)
                        .connectTimeout(Duration.ofSeconds(10))
                        .build();
        // Create the request we're going to send
        HttpRequest request = HttpRequest.newBuilder().uri(url.toURI()).GET().build();
        // Send the request using our client
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.body() == null) {
            throw new NullPointerException("Response to update check body is null");
        }
        return response.body();
    }

    private static GithubRelease[] getRepoReleases(URL url)
            throws URISyntaxException, InterruptedException, IOException {
        String rawJson = getRequest(url);
        return getGson().fromJson(rawJson, GithubRelease[].class);
    }

    private static GithubRelease[] getRepoReleases(String url)
            throws URISyntaxException, InterruptedException, IOException {
        return getRepoReleases(new URL(url));
    }

    private static GithubRelease[] getRepoReleases()
            throws URISyntaxException, InterruptedException, IOException {
        return getRepoReleases(
                String.format("https://api.github.com/repos/%s/%s/releases", repoOwner, repoName));
    }

    @SuppressWarnings("SameParameterValue")
    public static SemVer getLatestRelease()
            throws URISyntaxException, InterruptedException, IOException {
        GithubRelease[] tags = getRepoReleases();
        if (tags.length == 0) return null;
        if (tags.length > 1) Arrays.sort(tags, new GithubTagComparator());
        return tags[tags.length - 1].semVer;
    }

    private static Gson getGson() {
        if (gson == null) gson = new GsonBuilder().create();
        return gson;
    }

    @SuppressWarnings("CallToPrintStackTrace")
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
