package com.runningtracker.training;

/**
 * A Training was requested by an identifier that does not exist or does not
 * belong to the session's account. Both cases are reported identically so that
 * existence is not disclosed (REQ-F-9, REQ-F-14).
 */
public class TrainingNotFoundException extends RuntimeException {

    public TrainingNotFoundException() {
        super("Training not found.");
    }
}
