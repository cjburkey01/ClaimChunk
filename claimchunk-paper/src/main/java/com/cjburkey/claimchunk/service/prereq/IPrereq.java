package com.cjburkey.claimchunk.service.prereq;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * An interface representing some class that performs a check.
 *
 * @since 0.0.20
 */
public interface IPrereq<T> {

    /**
     * Gets the priority of this prerequisite. A smaller number would be checked first.
     *
     * @return The signed integer weight of this prerequisite.
     * @since 0.0.20
     */
    int getWeight();

    /**
     * Determines whether the provided player should be able to perform the action. Note: this
     * method should not change the state of anything as it's currently unknown whether the action
     * could fail after this check. If a subsequent check fails, then the action does not occur, so
     * it's important to make sure nothing is changed in this method.
     *
     * @param data The prerequisite data.
     * @return Whether this prerequisite was met.
     * @since 0.0.20
     */
    boolean getPassed(@NotNull T data);

    /**
     * Gets the error message that should be displayed if this prerequisite is not met. If a
     * prerequisite is not met, the process stops there and displays the error message returned by
     * the failing check.
     *
     * @param data The prerequisite data.
     * @return An optional error message describing why the action has failed.
     * @since 0.0.20
     */
    default Optional<String> getErrorMessage(@NotNull T data) {
        return Optional.empty();
    }

    /**
     * If this prerequisite succeeds, it will override the current success message with this one if
     * it is supplied.
     *
     * @param data The prerequisite data.
     * @return An optional success message.
     * @since 0.0.20
     */
    default Optional<String> getSuccessMessage(@NotNull T data) {
        return Optional.empty();
    }

    /**
     * Called after the action has been successfully completed.
     *
     * @param data The prerequisite data.
     * @since 0.0.20
     */
    default void onSuccess(@NotNull T data) {}
}
