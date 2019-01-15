package io.github.jacquelynoelle.padfoot.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import io.github.jacquelynoelle.padfoot.R;
import io.github.jacquelynoelle.padfoot.bluetoothle.BLEScanActivity;
import io.github.jacquelynoelle.padfoot.models.Pet;
import io.github.jacquelynoelle.padfoot.models.PetSize;

public class EditProfileActivity extends AppCompatActivity {

    private final static String TAG = EditProfileActivity.class.getSimpleName();

    EditText nameEditText;
    Spinner petSizeSpinner;
    AutoCompleteTextView breedACTextView;
    DatePicker birthdayPicker;
    Button updateButton;
    FirebaseDatabase database;
    DatabaseReference petsReference;
    DatabaseReference currentPetReference;
    String petID;

    public interface FirebasePetCallback {
        void onCallback(Pet pet);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(getString(R.string.profile_setup));
        setContentView(R.layout.activity_profile);

        database = FirebaseDatabase.getInstance();
        petsReference = database.getReference("pets");
        petID = getIntent().getStringExtra("petID");
        currentPetReference = petsReference.child(petID);

        nameEditText = findViewById(R.id.et_name);
        petSizeSpinner = findViewById(R.id.sp_pet_size);
        breedACTextView = findViewById(R.id.ac_breed);
        birthdayPicker = findViewById(R.id.dp_birthday);

        petSizeSpinner.setSelection(3); // default to Medium
        petSizeSpinner.setAdapter(new ArrayAdapter<PetSize>(this,
                android.R.layout.simple_spinner_item, PetSize.values()));
        breedACTextView.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(R.array.breeds_array)));

        updateButton = findViewById(R.id.b_profile_submit);
        updateButton.setText(R.string.update);

        updateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Pet pet = createPet();
                updatePetInFirebase(pet);

                // send to main step count activity
                Intent updateProfileIntent = new Intent();
                updateProfileIntent.setClass(EditProfileActivity.this, MainActivity.class);
                updateProfileIntent.putExtra("petID", petID);
                startActivity(updateProfileIntent);
                finish();
            }
        });

        getCurrentDetailsFromFirebase(new FirebasePetCallback() {
            @Override
            public void onCallback(Pet pet) {
                nameEditText.setText(pet.getName());
                breedACTextView.setText(pet.getBreed());

                Date parsed;
                Calendar birthday = new Calendar() {
                    @Override
                    protected void computeTime() {

                    }

                    @Override
                    protected void computeFields() {

                    }

                    @Override
                    public void add(int field, int amount) {

                    }

                    @Override
                    public void roll(int field, boolean up) {

                    }

                    @Override
                    public int getMinimum(int field) {
                        return 0;
                    }

                    @Override
                    public int getMaximum(int field) {
                        return 0;
                    }

                    @Override
                    public int getGreatestMinimum(int field) {
                        return 0;
                    }

                    @Override
                    public int getLeastMaximum(int field) {
                        return 0;
                    }
                };
                int year = 2019;
                int month = 1;
                int day = 1;

                try {
                    // TODO fix date gathering from calendar
                    SimpleDateFormat format =
                            new SimpleDateFormat("yyyy-m-d");
                    parsed = format.parse(pet.getBirthday());
                    birthday.setTime(parsed);
                    year = birthday.get(Calendar.YEAR);
                    month = birthday.get(Calendar.MONTH);
                    day = birthday.get(Calendar.DAY_OF_MONTH);
                }
                catch(ParseException pe) {
                    Log.e(TAG, "Parse exception on pet birthday");
                }

                birthdayPicker.updateDate(year, month, day);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.menu_connect).setVisible(true);
        menu.findItem(R.id.menu_disconnect).setVisible(true);
        menu.findItem(R.id.menu_edit_profile).setVisible(true);
        menu.findItem(R.id.menu_sign_out).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:
                final Intent bleConnectIntent = new Intent(this, BLEScanActivity.class);
                startActivity(bleConnectIntent);
                break;
            case R.id.menu_disconnect:
                final Intent bleDisconnectIntent = new Intent(this, BLEScanActivity.class);
                startActivity(bleDisconnectIntent);
                break;
            case R.id.menu_edit_profile:
                final Intent editProfileIntent = new Intent(this, EditProfileActivity.class);
                startActivity(editProfileIntent);
                break;
            case R.id.menu_sign_out:
                AuthUI.getInstance().signOut(this);
                Intent signOutIntent = new Intent(this, SplashActivity.class);
                startActivity(signOutIntent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
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
        int month = datePicker.getMonth();
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

        // create Pet object
        String ownerID = getIntent().getStringExtra("userID");
        Pet newPet = new Pet(petName, ownerID);
        newPet.setSize(petSize);
        if (petBreed.equals("")) {
            petBreed = "Dog";
        }
        newPet.setBreed(petBreed);
        newPet.setBirthday(petBirthday);

        return newPet;
    }

    private void updatePetInFirebase(Pet newPet) {
        HashMap petMap = newPet.toMap();
        currentPetReference.updateChildren(petMap);
    }
}
