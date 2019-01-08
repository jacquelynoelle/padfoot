package io.github.jacquelynoelle.padfoot;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    private String name;
    private String petID;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPetID() {
        return petID;
    }

    public void setPetID(String petID) {
        this.petID = petID;
    }
}