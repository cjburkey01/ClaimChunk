package com.cjburkey.claimchunk.config.ccconfig;

import com.cjburkey.claimchunk.Utils;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CCConfigParser {

    private static final int REGEX_FLAGS = Pattern.COMMENTS
                                           | Pattern.UNICODE_CHARACTER_CLASS
                                           | Pattern.DOTALL
                                           | Pattern.UNIX_LINES
                                           | Pattern.MULTILINE;

    private static final Pattern CATEGORY_PAT = Pattern.compile("^ \\s* ([A-Za-z0-9_\\-]+) \\s* : \\s* $", REGEX_FLAGS);
    private static final Pattern PROP_PAT = Pattern.compile("^ \\s* ([A-Za-z0-9_\\-]+) \\s* = \\s* (.*?) \\s* ; \\s* $", REGEX_FLAGS);

    public static void parse(@Nonnull final CCConfig config,
                             @Nonnull InputStream input) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            // Keep track of the current line contents and index
            String line;
            int lineIndex = 0;

            // The stack of current categories. This will allow duplicates that
            Stack<String> categories = new Stack<>();
            do {
                // Read in a new line
                line = reader.readLine();

                // Make sure the line is valid, trim it, and make sure it's
                // not empty
                while (line != null && !(line = line.trim()).isEmpty()) {
                    // Check if this line is a comment. If it is, just skip to
                    // the next line
                    if (line.startsWith("#")) {
                        // Increment the line
                        line = nextLine(line);
                        lineIndex++;
                        continue;
                    }

                    // Try to read a category
                    String category = tryParseCategory(line);
                    if (category != null) {
                        // Add the category to the stack
                        categories.push(category);

                        // Increment the line
                        line = nextLine(line);
                        lineIndex++;
                        continue;
                    }

                    // If it fails, try to read a property
                    Pair<String, String> property = tryParseProperty(line);
                    if (property != null) {
                        // Assign the value in the config
                        config.set(namespacedKey(categories, property.t), property.k);

                        // Increment the line
                        line = nextLine(line);
                        lineIndex++;
                        continue;
                    }

                    // If that fails, we have an error on this line
                    Utils.err("Error reading line #%s \"%s\". It will be ignored.",
                              lineIndex,
                              line);
                }
            } while (line != null);
        }
    }

    private static String nextLine(String line) {
        // Find the first character of the next line
        int nextLine = line.indexOf('\n') + 1;

        // If there is no newline, add one to the end of the
        // line and tell the parser to use the whole line
        if (nextLine <= 0) {
            line += '\n';
            nextLine = Integer.MAX_VALUE;
        }

        // Remove the first line, the comment line
        return line.substring(Integer.min(line.length(), nextLine)).trim();
    }

    private static String namespacedKey(Stack<String> category, String key) {
        // If the category is none, the value is just under the key
        if (category.isEmpty()) {
            return key;
        }

        // Create an empty string builder
        StringBuilder builder = new StringBuilder(key);

        // Build up the category-named key.
        while (!category.isEmpty()) {
            builder.insert(0, '.');
            builder.insert(0, category.pop());
        }

        // Convert the builder back into a string
        return builder.toString();
    }

    private static String tryParseCategory(String line) {
        // Try to match and get the category name
        Matcher matcher = CATEGORY_PAT.matcher(line);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private static Pair<String, String> tryParseProperty(String line) {
        // Try to match and get the property name and value
        Matcher matcher = PROP_PAT.matcher(line);
        if (matcher.find()) {
            return new Pair<>(matcher.group(1), matcher.group(2));
        }
        return null;
    }

    private static class Pair<T, K> {

        private final T t;
        private final K k;

        private Pair(T t, K k) {
            this.t = t;
            this.k = k;
        }

    }

}
