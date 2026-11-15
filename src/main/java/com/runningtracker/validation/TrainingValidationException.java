package com.runningtracker.validation;

/**
 * A submitted Training field violated a domain rule (REQ-F-5, REQ-F-6,
 * REQ-F-7). The message is user-facing and is returned as the {@code Error}
 * body when the field is rejected over HTTP.
 */
public class TrainingValidationException extends RuntimeException {

    public TrainingValidationException(String message) {
        super(message);
    }
}
