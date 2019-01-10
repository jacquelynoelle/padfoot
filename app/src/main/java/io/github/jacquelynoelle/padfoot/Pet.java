package io.github.jacquelynoelle.padfoot;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.Date;

@IgnoreExtraProperties
public class Pet {

    private String name;
    private String ownerID;
    private String size;
    private String breed;
    private String birthday;
    private ArrayList challenges;

    /* Two constructors:
     * - Default
     * - One argument (name) because that is the only required field upon sign up.
     */

    public Pet() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Pet(String name, String ownerID) {
        this.name = name;
        this.ownerID = ownerID;
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

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        if (size == null) {
            this.size = "Medium";
        } else {
            this.size = size;
        }
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public ArrayList getChallenges() {
        return challenges;
    }

    public void addChallenge(Challenge challenge) {
        challenges.add(challenge);
    }
}
