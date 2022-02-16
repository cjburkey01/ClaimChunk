package com.cjburkey.claimchunk.config.ccconfig;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CCConfigParser {

    private static final int REGEX_FLAGS =
            Pattern.COMMENTS // This makes the regex a lot easier to read :D
                    | Pattern.UNICODE_CHARACTER_CLASS // UTF-8 support
                    | Pattern.UNIX_LINES // Single '\n' lines
            ;

    // The comment regex
    private static final String COMMENT = "^ \\s*? \\# \\s*? (.*?) \\s*? $";

    private static final Pattern COMMENT_PAT = Pattern.compile(COMMENT, REGEX_FLAGS);

    // The label regex
    private static final String IDENTIFIER = "[\\w\\-@]+ [\\w\\-.@]*?";
    private static final String LABEL = "^ \\s*? (" + IDENTIFIER + ") \\s*? : \\s*? $";
    private static final Pattern LABEL_PAT = Pattern.compile(LABEL, REGEX_FLAGS);

    // The property value definition regex
    private static final String PROPERTY =
            "^ \\s*? (" + IDENTIFIER + ") \\s*? (.*?) \\s*? ; \\s*? $";
    private static final Pattern PROPERTY_PAT = Pattern.compile(PROPERTY, REGEX_FLAGS);

    public List<CCConfigParseError> parse(@NotNull CCConfig config, @NotNull String input) {
        // Keep track of errors as we go
        final ArrayList<CCConfigParseError> errors = new ArrayList<>();

        // Parsing information
        int currentPos = 0;
        int currentLine = 0;
        final List<String> currentLabel = new ArrayList<>();
        final Matcher commentMatcher = COMMENT_PAT.matcher(input);
        final Matcher labelMatcher = LABEL_PAT.matcher(input);
        final Matcher propertyMatcher = PROPERTY_PAT.matcher(input);

        final String[] lines = input.split("\n");

        // Loop until there is no input (or only one character what can you do
        // with that)
        for (String line : lines) {
            // Trim the line
            if ((line = line.trim()).isEmpty()) {
                // Skip empty lines
                continue;
            }

            int previousLine = currentLine;
            int previousPos = currentPos;
            currentPos += line.length();
            currentLine++;

            // Try to match a comment
            commentMatcher.reset(line);
            if (commentMatcher.find()) {
                // Skip comments
                continue;
            }

            // Try to match a label
            labelMatcher.reset(line);
            if (labelMatcher.find()) {
                // Update the current label
                currentLabel.clear();
                Collections.addAll(currentLabel, labelMatcher.group(1).trim().split("\\."));
                continue;
            }

            // Try to match a property
            propertyMatcher.reset(line);
            if (propertyMatcher.find()) {
                String key = String.join(".", currentLabel) + "." + propertyMatcher.group(1).trim();
                String value = propertyMatcher.group(2).trim();

                // Update the config value
                config.set(key, value);

                continue;
            }

            // Add an error for lines that fail to parse
            errors.add(
                    new CCConfigParseError(
                            previousLine,
                            previousPos,
                            currentLine,
                            currentPos,
                            line,
                            "failed_to_parse_line"));
        }

        // Return all of the errors matched;
        return errors;
    }

    @SuppressWarnings("unused")
    private static String namespacedKey(Collection<String> categories, String key) {
        // If the category is none, the value is just under the key
        if (categories.isEmpty()) {
            return key;
        }

        // Create an empty string builder
        StringBuilder builder = new StringBuilder(key);

        // Build up the category-named key.
        for (String category : categories) {
            builder.append(category);
            builder.append('.');
        }

        // Convert the builder back into a string
        return builder.toString();
    }
}
