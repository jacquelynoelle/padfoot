package io.github.jacquelynoelle.padfoot;

public enum PetSize {
    TOY("Up to 12 lbs"),
    SMALL("12 to 25 lbs"),
    MEDIUM("25 to 50 lbs"),
    LARGE("50 to 100 lbs"),
    EXTRA_LARGE("Over 100 lbs");

    private String description;

    PetSize(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}