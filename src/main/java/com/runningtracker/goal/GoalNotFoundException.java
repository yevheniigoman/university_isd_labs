package com.runningtracker.goal;

/**
 * A Monthly Goal was requested for a Month that has no goal for the session's
 * account, or whose goal belongs to another account. Both cases are reported
 * identically so that existence is not disclosed (REQ-F-14, REQ-F-15).
 */
public class GoalNotFoundException extends RuntimeException {

    public GoalNotFoundException() {
        super("Goal not found.");
    }
}
