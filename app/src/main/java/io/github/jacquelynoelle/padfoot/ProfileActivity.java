package io.github.jacquelynoelle.padfoot;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;

import com.firebase.ui.auth.AuthUI;

public class ProfileActivity extends AppCompatActivity {

    Spinner petSizeSpinner;
    AutoCompleteTextView breedTextView;
    Button button;

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

        button = findViewById(R.id.profile_submit);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // set up push form fields to database
                Intent setupProfile = new Intent();
                setupProfile.setClass(ProfileActivity.this, MainActivity.class);
                startActivity(setupProfile);
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
}
