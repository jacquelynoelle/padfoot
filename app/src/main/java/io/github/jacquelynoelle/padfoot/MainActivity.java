package io.github.jacquelynoelle.padfoot;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private Button button;
    private EditText editText;
    private TextView displayText;
    private DatabaseReference database;
    private ChildEventListener eventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button_01);
        editText = findViewById(R.id.edit_text_01);
        displayText = findViewById(R.id.text_view_01);
        database = FirebaseDatabase.getInstance().getReference();
        // can do the reference separately to get focused parts

        // eventListener for updates to database
        eventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                Pet newPet = dataSnapshot.getValue(Pet.class); // not working when most recent data is not a Pet object
//                String newText = "Added pet: " + newPet.name;
//                displayText.setText(newText);
            }

            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
            public void onChildRemoved(DataSnapshot dataSnapshot) {}
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            public void onCancelled(DatabaseError databaseError) {}
        };

        database.addChildEventListener(eventListener);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String petName = editText.getText().toString();

//                String newPetID = writeNewPet(petName);
//                displayText.setText(petName);

                Pet newPet = new Pet(petName);

                database.push().setValue(newPet);

                editText.getText().clear();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.menu_ble).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_ble:
                final Intent intent = new Intent(this, DeviceScanActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }
}
