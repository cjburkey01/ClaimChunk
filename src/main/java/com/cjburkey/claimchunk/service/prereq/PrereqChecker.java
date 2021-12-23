package com.cjburkey.claimchunk.service.prereq;

import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class PrereqChecker<T extends IPrereq<E>, E> {

    // Sort the prerequisites according to their weights
    public final PriorityQueue<T> prereqs =
            new PriorityQueue<>(Comparator.comparingInt(T::getWeight));

    public void check(
            E prereqData,
            @Nullable String defaultSuccessMessage,
            IPrereqAction onError,
            IPrereqAction onSuccess) {
        String successOutput = defaultSuccessMessage;

        // Check all the prerequisites
        for (T prereq : prereqs) {
            if (!prereq.getPassed(prereqData)) {
                // Call the error handler
                onError.call(prereq.getErrorMessage(prereqData));
                return;
            }

            // Get and update (if present) the message about the success
            Optional<String> successMessage = prereq.getSuccessMessage(prereqData);
            if (successMessage.isPresent()) {
                successOutput = successMessage.get();
            }
        }

        // Call the success handler
        onSuccess.call(Optional.ofNullable(successOutput));

        // Run success methods
        for (T prereq : prereqs) {
            prereq.onSuccess(prereqData);
        }
    }

    @FunctionalInterface
    public interface IPrereqAction {

        void call(@SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<String> message);
    }
}
