package io.github.jacquelynoelle.padfoot;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;

public class ProfileActivity extends AppCompatActivity {

    Spinner petSizeSpinner;
    AutoCompleteTextView breedTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        petSizeSpinner = findViewById(R.id.pet_size_dropdown);
        breedTextView = findViewById(R.id.breed_edit_text);

        petSizeSpinner.setAdapter(new ArrayAdapter<PetSize>(this,
                android.R.layout.simple_spinner_item, PetSize.values()));
        breedTextView.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(R.array.breeds_array)));
    }
}
