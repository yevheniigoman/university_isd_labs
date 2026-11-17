package com.runningtracker.validation;

/**
 * A submitted Monthly Goal field violated a rule of REQ-F-15. The message is
 * user-facing and is returned as the {@code Error} body when the request is
 * rejected over HTTP.
 */
public class GoalValidationException extends RuntimeException {

    public GoalValidationException(String message) {
        super(message);
    }
}
