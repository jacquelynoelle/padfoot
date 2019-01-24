package io.github.jacquelynoelle.padfoot.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import io.github.jacquelynoelle.padfoot.R;
import io.github.jacquelynoelle.padfoot.models.Pet;
import io.github.jacquelynoelle.padfoot.models.PetSize;

public class ProfileActivity extends AppCompatActivity {

    EditText nameEditText;
    Spinner petSizeSpinner;
    AutoCompleteTextView breedACTextView;
    DatePicker birthdayPicker;
    Spinner stepGoalSpinner;
    Button submitButton;
    FirebaseDatabase database;
    DatabaseReference usersReference;
    DatabaseReference petsReference;
    String petID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(getString(R.string.profile_setup));
        setContentView(R.layout.activity_profile);

        database = FirebaseDatabase.getInstance();
        usersReference = database.getReference("users");
        petsReference = database.getReference("pets");

        nameEditText = findViewById(R.id.et_name);
        petSizeSpinner = findViewById(R.id.sp_pet_size);
        breedACTextView = findViewById(R.id.ac_breed);
        birthdayPicker = findViewById(R.id.dp_birthday);
        stepGoalSpinner = findViewById(R.id.sp_stepgoal);

        petSizeSpinner.setAdapter(new ArrayAdapter<PetSize>(this,
                android.R.layout.simple_spinner_item, PetSize.values()));
        petSizeSpinner.setSelection(2); // default to Medium

        breedACTextView.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(R.array.breeds_array)));

        Integer[] stepGoalChoices = { 8000, 12000, 16000, 20000 };
        final ArrayAdapter<Integer> stepGoalAdapter = new ArrayAdapter<Integer>(this,
                android.R.layout.simple_spinner_item, stepGoalChoices);
        stepGoalSpinner.setAdapter(stepGoalAdapter);
        stepGoalSpinner.setSelection(stepGoalAdapter.getPosition(12000)); // default to 12000

        submitButton = findViewById(R.id.b_profile_submit);

        submitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Pet pet = createPet();
                pushPetToFirebase(pet);

                // send to main step count activity
                Intent setupProfile = new Intent();
                setupProfile.setClass(ProfileActivity.this, MainActivity.class);
//                setupProfile.putExtra("petID", petID);
                startActivity(setupProfile);
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.menu_connect).setVisible(false);
        menu.findItem(R.id.menu_disconnect).setVisible(false);
        menu.findItem(R.id.menu_edit_profile).setVisible(false);
        menu.findItem(R.id.menu_sign_out).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sign_out:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                Intent signOutIntent = new Intent(ProfileActivity.this, SplashActivity.class);
                                startActivity(signOutIntent);
                            }
                        });
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private String getDateFromDatePicker(DatePicker datePicker){
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth() + 1;
        int year =  datePicker.getYear();

        String date = year + "-" + month + "-" + day;

        return date;
    }

    private Pet createPet() {
        // get data from form fields
        String petName = nameEditText.getText().toString();
        String petSize = petSizeSpinner.getSelectedItem().toString();
        String petBreed = breedACTextView.getText().toString();
        String petBirthday = getDateFromDatePicker(birthdayPicker);
        int petStepGoal = (Integer) stepGoalSpinner.getSelectedItem();

        // create Pet object
        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.app_file), Context.MODE_PRIVATE);
        String ownerID = sharedPref.getString("userID", FirebaseAuth.getInstance().getCurrentUser().getUid());
        Pet newPet = new Pet(petName, ownerID);
        newPet.setSize(petSize);
        if (petBreed.equals("")) {
            petBreed = "Dog";
        }
        newPet.setBreed(petBreed);
        newPet.setBirthday(petBirthday);
        newPet.setStepGoal(petStepGoal);

        return newPet;
    }

    private void pushPetToFirebase(Pet newPet) {
        // push pet to firebase
        DatabaseReference petReference = petsReference.push();
        petID = petReference.getKey();
        petReference.setValue(newPet);

        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.app_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("petID", petID);
        editor.putString("petName", newPet.getName());
        editor.putString("petSize", newPet.getSize());
        editor.putString("petBreed", newPet.getBreed());
        editor.putString("petBirthday", newPet.getBirthday());
        editor.putInt("petStepGoal", newPet.getStepGoal());

        // create hourly step count placeholders and clear out any previous user's data from pref
        for (int i = 0; i < 24; i++) {
            petReference.child("hourlySteps").child(Integer.toString(i)).setValue(0);
            String hourKey = "hour_" + i;
            editor.putInt(hourKey, 0);
        }

        editor.apply();

        // update petID in user
        usersReference.child(newPet.getOwnerID()).child("petID").setValue(petID);
    }
}
