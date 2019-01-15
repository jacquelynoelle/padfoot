package io.github.jacquelynoelle.padfoot.models;

public enum PetSize {
    TOY("Toy"),
    SMALL("Small"),
    MEDIUM("Medium"),
    LARGE("Large"),
    EXTRA_LARGE("Extra large");

    private String label;

    PetSize(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return this.label;
    }
}