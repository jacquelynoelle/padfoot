package io.github.jacquelynoelle.padfoot;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.Date;

@IgnoreExtraProperties
public class Pet {

    private String name;
    private String ownerID;
    private PetSize size;
    private String breed;
    private Date birthday;
    private ArrayList challenges;

    /* Two constructors:
     * - Default
     * - One argument (name) because that is the only required field upon sign up.
     */

    public Pet() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Pet(String name) {
        this.name = name;
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

    public PetSize getSize() {
        return size;
    }

    public void setSize(PetSize size) {
        if (size == null) {
            this.size = PetSize.MEDIUM;
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

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public ArrayList getChallenges() {
        return challenges;
    }

    public void addChallenge(Challenge challenge) {
        challenges.add(challenge);
    }
}
