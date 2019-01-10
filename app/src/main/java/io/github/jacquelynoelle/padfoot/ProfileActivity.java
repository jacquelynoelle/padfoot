package io.github.jacquelynoelle.padfoot;

import android.content.Intent;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Date;

public class ProfileActivity extends AppCompatActivity {

    EditText nameEditText;
    Spinner petSizeSpinner;
    AutoCompleteTextView breedACTextView;
    DatePicker birthdayPicker;
    Button submitButton;
    FirebaseDatabase database;
    DatabaseReference usersReference;
    DatabaseReference petsReference;
    String petID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        database = FirebaseDatabase.getInstance();
        usersReference = database.getReference("users");
        petsReference = database.getReference("pets");

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

        submitButton = findViewById(R.id.b_profile_submit);

        submitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Pet pet = createPet();
                pushPetToFirebase(pet);

                // send to main step count activity
                Intent setupProfile = new Intent();
                setupProfile.setClass(ProfileActivity.this, MainActivity.class);
                setupProfile.putExtra("petID", petID);
                startActivity(setupProfile);
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.menu_ble).setVisible(true);
        menu.findItem(R.id.sign_out_menu).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_ble:
                final Intent intent = new Intent(this, DeviceScanActivity.class);
                startActivity(intent);
                break;
            case R.id.sign_out_menu:
                AuthUI.getInstance().signOut(this);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
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
        if (!petBreed.equals("")) {
            newPet.setBreed("Dog");
        }
        newPet.setBirthday(petBirthday);

        return newPet;
    }

    private void pushPetToFirebase(Pet newPet) {
        // push pet to firebase
        DatabaseReference petReference = petsReference.push();
        petID = petReference.getKey();
        petReference.setValue(newPet);

        // update petID in user
        usersReference.child(newPet.getOwnerID()).child("petID").setValue(petID);
    }
}
