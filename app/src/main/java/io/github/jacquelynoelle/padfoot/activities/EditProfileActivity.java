package io.github.jacquelynoelle.padfoot.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import io.github.jacquelynoelle.padfoot.R;
import io.github.jacquelynoelle.padfoot.models.Pet;
import io.github.jacquelynoelle.padfoot.models.PetSize;

public class EditProfileActivity extends AppCompatActivity {

    private final static String TAG = EditProfileActivity.class.getSimpleName();

    EditText nameEditText;
    Spinner petSizeSpinner;
    AutoCompleteTextView breedACTextView;
    DatePicker birthdayPicker;
    Spinner stepGoalSpinner;
    Button updateButton;
    FirebaseDatabase database;
    DatabaseReference petsReference;
    DatabaseReference currentPetReference;
    String mPetID;

    public interface FirebasePetCallback {
        void onCallback(Pet pet);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(getString(R.string.profile));
        setContentView(R.layout.activity_profile);

        database = FirebaseDatabase.getInstance();
        petsReference = database.getReference("pets");

        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.app_file), Context.MODE_PRIVATE);
        mPetID = sharedPref.getString("petID", "test");
        currentPetReference = petsReference.child(mPetID);

        nameEditText = findViewById(R.id.et_name);
        petSizeSpinner = findViewById(R.id.sp_pet_size);
        breedACTextView = findViewById(R.id.ac_breed);
        birthdayPicker = findViewById(R.id.dp_birthday);
        stepGoalSpinner = findViewById(R.id.sp_stepgoal);

        final ArrayAdapter<PetSize> petSizeAdapter = new ArrayAdapter<PetSize>(this,
                android.R.layout.simple_spinner_item, PetSize.values());
        petSizeSpinner.setAdapter(petSizeAdapter);
        petSizeSpinner.setSelection(petSizeAdapter.getPosition(PetSize.MEDIUM)); // default to Medium

        breedACTextView.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(R.array.breeds_array)));

        Integer[] stepGoalChoices = { 8000, 12000, 16000, 20000 };
        final ArrayAdapter<Integer> stepGoalAdapter = new ArrayAdapter<Integer>(this,
                android.R.layout.simple_spinner_item, stepGoalChoices);
        stepGoalSpinner.setAdapter(stepGoalAdapter);
        stepGoalSpinner.setSelection(stepGoalAdapter.getPosition(12000)); // default to 12000 TODO

        updateButton = findViewById(R.id.b_profile_submit);
        updateButton.setText(R.string.update);

        updateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Pet pet = createPet();
                updatePetData(pet);

                // send to main step count activity
                Intent updateProfileIntent = new Intent();
                updateProfileIntent.setClass(EditProfileActivity.this, MainActivity.class);
                startActivity(updateProfileIntent);
                finish();
            }
        });

        getCurrentDetailsFromFirebase(new FirebasePetCallback() {
            @Override
            public void onCallback(Pet pet) {
                nameEditText.setText(pet.getName());
                breedACTextView.setText(pet.getBreed());

                int year = 2019;
                int month = 1;
                int day = 1;

                try {
                    GregorianCalendar birthday = new GregorianCalendar();

                    SimpleDateFormat format =
                            new SimpleDateFormat("yyyy-MM-dd");
                    Date parsed = format.parse(pet.getBirthday());
                    birthday.setTime(parsed);

                    year = birthday.get(Calendar.YEAR);
                    month = birthday.get(Calendar.MONTH);
                    day = birthday.get(Calendar.DAY_OF_MONTH);

                }
                catch(ParseException pe) {
                    Log.e(TAG, "Parse exception on pet birthday");
                }

                birthdayPicker.updateDate(year, month, day);

                PetSize size;

                switch (pet.getSize()) {
                    case "Toy":
                        size = PetSize.TOY;
                        break;
                   case "Small":
                        size = PetSize.SMALL;
                        break;
                   case "Medium":
                        size = PetSize.MEDIUM;
                        break;
                   case "Large":
                        size = PetSize.LARGE;
                        break;
                   case "Extra Large":
                        size = PetSize.EXTRA_LARGE;
                        break;
                    default:
                        size = PetSize.MEDIUM; // default to Medium
                }

                petSizeSpinner.setSelection(petSizeAdapter.getPosition(size));
                stepGoalSpinner.setSelection(stepGoalAdapter.getPosition(pet.getStepGoal()));
            }
        });
    }

    private void getCurrentDetailsFromFirebase(final FirebasePetCallback callback) {
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Pet pet = dataSnapshot.getValue(Pet.class);
                callback.onCallback(pet);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "onReturningUserLogin: onCancelled", databaseError.toException());
            }
        };
        currentPetReference.addListenerForSingleValueEvent(listener);
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
        String ownerID = getIntent().getStringExtra("userID");
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

    private void updatePetData(Pet newPet) {
        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.app_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("petName", newPet.getName());
        editor.putString("petSize", newPet.getSize());
        editor.putString("petBreed", newPet.getBreed());
        editor.putString("petBirthday", newPet.getBirthday());
        editor.putInt("petStepGoal", newPet.getStepGoal());
        editor.apply();

        HashMap petMap = newPet.toMap();
        currentPetReference.updateChildren(petMap);
    }
}
