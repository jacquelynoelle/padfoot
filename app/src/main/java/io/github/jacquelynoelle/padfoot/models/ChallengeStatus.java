package io.github.jacquelynoelle.padfoot.models;

public enum ChallengeStatus {
    COMMITTED("Committed"),
    IN_PROGRESS("In progress"),
    INCOMPLETE("Incomplete"),
    COMPLETE("Complete");

    private String status;

    ChallengeStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return this.status;
    }
}