package io.github.jacquelynoelle.padfoot;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;

@IgnoreExtraProperties
public class Pet {

    private String name;
    private String ownerID;
    private String breed;
    private PetSize size;
    private ArrayList challenges;

    public Pet() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Pet(String name) {
        this.name = name;
        this.breed = "Unknown";
        this.size = PetSize.MEDIUM;
    }

    public Pet(String name, String breed) {
        this.name = name;
        this.breed = breed;
        this.size = PetSize.MEDIUM;
    }

    public Pet(String name, String breed, PetSize size) {
        this.name = name;
        this.breed = breed;
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public PetSize getSize() {
        return size;
    }

    public void setSize(PetSize size) {
        this.size = size;
    }

    public ArrayList getChallenges() {
        return challenges;
    }

    public void addChallenge(Challenge challenge) {
        challenges.add(challenge);
    }
}
