package com.cjburkey.claimchunk.config.ccconfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Stack;
import java.util.regex.Pattern;

public class CCConfigParser {

    private static final int REGEX_FLAGS = Pattern.COMMENTS                     // This makes the regex a lot easier to read :D
                                           | Pattern.UNICODE_CHARACTER_CLASS    // UTF-8 support
                                           | Pattern.DOTALL                     // Allow '.' to include newlines if possible
                                           | Pattern.UNIX_LINES                 // Single '\n' lines
                                           | Pattern.MULTILINE                  // Matcher can run multiple times on the input
                                           ;
    
    // The comment regex
    private static final String COMMENT = "^ \\s*? # \\s*? (.*?) \\s*? $";
    private static final Pattern COMMENT_PAT = Pattern.compile(COMMENT, REGEX_FLAGS);
    
    // The label regex
    private static final String IDENTIFIER = "<{1,2} | [a-zA-Z0-9_\\-]+ [a-zA-Z0-9_\\-.]* [a-zA-Z0-9_\\-]+";
    private static final String LABEL = "^ \\s*? (" + IDENTIFIER + ") \\s*? : \\s*? $";
    private static final Pattern LABEL_PAT = Pattern.compile(LABEL, REGEX_FLAGS);
    
    // The property value definition regex
    private static final String PROPERTY = "^ \\s*? (" + IDENTIFIER + ") \\s*? (.*?) \\s*? ; \\s*? $";
    private static final Pattern PROPERTY_PAT = Pattern.compile(PROPERTY, REGEX_FLAGS);

    public void parse(@Nonnull CCConfig config, @Nonnull InputStream input/*, boolean keepComments*/) throws IOException {
        // Read the input into a string and parse that
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            parse(config, reader.lines().collect(Collectors.joining("\n"))/*, keepComments*/);
        }
    }
    
    public List<CCConfigParseError> parse(@Nonnull CCConfig config, @Nonnull String input/*, boolean keepComments*/) {
        // Keep track of errors as we go
        final ArrayList<CCConfigParseError> errors = new ArrayList<>();
        
        // Keep track of the current stack of categories for the fully
        // qualified name
        final Stack<String> category = new Stack<>();
        
        // Parsing information
        int currentPos = 0;
        int currentLine = 0;
        final Matcher labelMatcher = LABEL_PAT.matcher(input);
        final Matcher propertyMatcher = PROPERTY_PAT.matcher(input);
        final Matcher commentMatcher = COMMENT_PAT.matcher(input);
        
        // Loop until there is no input (or only one character what can you do
        // with that)
        while (currentPos < input.length()) {
            // Try to match this line to a comment
            if (commentMatcher.find(currentPos)) {
                // Get the result of the comment match
                final MatchResult matchResult = labelMatcher.toMatchResult();
                
                // Increment the line and index counts by the number of lines
                // and characters, respectively, this match spans
                currentLine += (int) matchResult.group().codePoints().filter(i -> i == '\n').count();
                currentPos =  matchResult.end();
            }
            
            // Try to match this line to a label
            if (labelMatcher.find(currentPos)) {
                // Get the result of the label match
                final MatchResult matchResult = labelMatcher.toMatchResult();
                
                // Determine the new positions
                final int newLine = currentLine + (int) matchResult.group().codePoints().filter(i -> i == '\n').count();
                final int newPos = matchResult.end();

                // Verify the group count
                if (matchResult.groupCount() < 1) {
                    // Push the new category onto the stack
                    category.push(matchResult.group(1));
                } else {
                    // Record the fact that captures were unexpected
                    errors.add(new CCConfigParseError(currentLine, currentPos, newLine, newPos, matchResult.group(), "label_parser_received_more_than_one_capture_group"));
                }
                
                // Increment the line and index counts by the number of lines
                // and characters, respectively, this match spans
                currentLine = newLine;
                currentPos = newPos;
            }
            
            // Try to match this line to a property
            if (propertyMatcher.find(currentPos)) {
                // Get the result of the property match
                final MatchResult matchResult = propertyMatcher.toMatchResult();
                
                // Determine the new positions
                final int newLine = currentLine + (int) matchResult.group().codePoints().filter(i -> i == '\n').count();
                final int newPos = matchResult.end();

                // Verify the group count
                if (matchResult.groupCount() < 2) {
                    // Assign the new information
                    config.set(namespacedKey(category, matchResult.group(1)), matchResult.group(2));
                } else {
                    // Record the fact that the capture count was unexpected
                    errors.add(new CCConfigParseError(currentLine, currentPos, newLine, newPos, matchResult.group(), "property_parser_received_more_than_one_capture_group"));
                }
                
                // Increment the line and index counts by the number of lines
                // and characters, respectively, this match spans
                currentLine += (int) matchResult.group().codePoints().filter(i -> i == '\n').count();
                currentPos = matchResult.end();
            }
            
            // If nothing could be matched, look for a new line to skip to
            int nextNewline = input.indexOf('\n', currentPos);
            if (nextNewline > -1) {
                // Determine the new matcher location
                final int newLine = currentLine + 1;
                final int newPos = nextNewline + 1;
                
                // Record the fact that this line couldn't be parsed correctly
                errors.add(new CCConfigParseError(currentLine, currentPos, newLine, newPos, input.substring(currentPos, newPos), "failed_to_parse_line"));
                
                // Update the matcher position
                currentLine = newLine;
                currentPos = nextNewline + 1;
            } else {
                // The end of input has been reached
                break;
            }
        }
        
        // Return all of the errors matched;
        return errors;
    }

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
