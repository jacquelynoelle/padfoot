package io.github.jacquelynoelle.padfoot;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Pet {

    public String name;

    public Pet() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Pet(String name) {
        this.name = name;
    }

}
