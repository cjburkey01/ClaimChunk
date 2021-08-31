package com.cjburkey.claimchunk.data.newdata;

import com.cjburkey.claimchunk.Utils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.regex.Pattern;

public final class BackupCleaner {

    public static boolean deleteBackups(
            @NotNull File folder, @NotNull Pattern namePattern, long maxAgeInMinutes) {
        try {
            // If it's not a directory, we can't loop over its contents.
            if (!folder.isDirectory()) {
                return false;
            }

            // Get a list of the child files, and if it isn't valid, we can't list the files.
            File[] files = folder.listFiles();
            if (files == null) {
                return false;
            }

            // Get the current time in milliseconds
            long currentTime = System.currentTimeMillis();

            // Loop through all the files in the folder.
            for (File file : files) {
                // If the file isn't a standard file (like a folder), or its name doesn't match the
                // provided
                // pattern,
                // skip it.
                if (!file.isFile() || !namePattern.matcher(file.getName()).matches()) {
                    continue;
                }

                // Get the time in milliseconds since the file was modified
                long modified = file.lastModified();

                // The modified time is 0 for an error; if an error occurs, we don't want to delete
                // the file
                // just in
                // case.
                // Check if the last modified time was more than `maxAgeInMinutes` ago from the time
                // this
                // method was
                // invoked.
                if (modified > 0L && (currentTime - modified) >= 60000 * maxAgeInMinutes) {
                    Utils.debug("Deleting old backup file: %s", file.getName());

                    // Try to delete the file
                    if (file.delete()) {
                        Utils.debug("Deleted old backup successfully");
                    } else {
                        Utils.err("Failed to delete backup file: %s", file.getName());
                        Utils.err("Telling Java to delete the file when the server exits.");

                        // Try to delete this file again, but only once the server has been closed.
                        file.deleteOnExit();
                    }
                }
            }

            // Success
            return true;
        } catch (Exception e) {
            // Print the error
            Utils.err("Failed to delete old backups in folder: %s", folder.toString());
            e.printStackTrace();
        }

        // An error occurred
        return false;
    }
}
