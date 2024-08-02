package com.cjburkey.claimchunk.update;

import com.cjburkey.claimchunk.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;
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
// Note: I had to use the GitHub /tags api because /releases/latest was always
//       a 404 for me? Not sure why.
public class UpdateChecker {

    private static Gson gson;

    private static String getRequest(URI uri)
            throws URISyntaxException, InterruptedException, IOException {
        // Create the HTTP connection handler (basically?)
        try (HttpClient client =
                HttpClient.newBuilder()
                        .version(HttpClient.Version.HTTP_1_1)
                        .followRedirects(HttpClient.Redirect.NORMAL)
                        .connectTimeout(Duration.ofSeconds(10))
                        .build()) {
            // Create the request we're going to send
            HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
            // Send the request using our client
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.body() == null) {
                throw new NullPointerException("Response to update check body is null");
            }
            return response.body();
        }
    }

    private static GithubRelease[] getRepoTags(URI uri)
            throws URISyntaxException, InterruptedException, IOException {
        String rawJson = getRequest(uri);
        return getGson().fromJson(rawJson, GithubRelease[].class);
    }

    private static GithubRelease[] getRepoTags(String uri)
            throws URISyntaxException, InterruptedException, IOException {
        return getRepoTags(URI.create(uri));
    }

    private static GithubRelease[] getRepoTags(String repoOwner, String repoName)
            throws URISyntaxException, InterruptedException, IOException {
        return getRepoTags(
                String.format("https://api.github.com/repos/%s/%s/tags", repoOwner, repoName));
    }

    @SuppressWarnings("SameParameterValue")
    public static SemVer getLatestRelease(String repoOwner, String repoName)
            throws URISyntaxException, InterruptedException, IOException {
        int attempts = 0;

        do {
            var tag =
                    Arrays.stream(getRepoTags(repoOwner, repoName)).max(Comparator.naturalOrder());
            if (tag.isPresent()) {
                return tag.get().getSemVer();
            }
            Utils.warn("No release tags returned from GitHub? Trying again...");
            attempts++;
        } while (attempts < 3);

        throw new RuntimeException(
                "No GitHub releases showing up from API call after 3 attempts! I give up :shrug:");
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
        private transient SemVer semVer;

        // Gets the semantic version for this release
        private @NotNull SemVer getSemVer() {
            if (name == null) throw new RuntimeException("GitHub release name was null?");
            if (semVer == null) {
                try {
                    semVer = SemVer.fromString(name);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to parse version from GitHub release", e);
                }
            }
            return semVer;
        }

        @Override
        public String toString() {
            try {
                return getSemVer().toString();
            } catch (Exception e) {
                //noinspection CallToPrintStackTrace
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
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
            return 0;
        }
    }
}
