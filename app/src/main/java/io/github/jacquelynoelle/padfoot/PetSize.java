package io.github.jacquelynoelle.padfoot;

public enum PetSize {
    TOY("Toy", "<12 pounds"),
    SMALL("Small", "12-25 pounds"),
    MEDIUM("Medium", "25-50 pounds"),
    LARGE("Large", "50-100 pounds"),
    EXTRA_LARGE("Extra large", ">100 pounds");

    private String label;
    private String description;

    PetSize(String label, String description) {
        this.label = label;
        this.description = description;
    }

    @Override
    public String toString() {
        return this.label;
    }

    public String getDescription() {
        return this.description;
    }
}