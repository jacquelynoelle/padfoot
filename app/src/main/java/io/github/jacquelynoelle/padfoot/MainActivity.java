package io.github.jacquelynoelle.padfoot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.firebase.ui.auth.AuthUI;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();
    private TextView displayText;
    private DatabaseReference database;
    private ChildEventListener eventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "onCreate");

        displayText = findViewById(R.id.tv_step_count);
        database = FirebaseDatabase.getInstance().getReference();
        // can do the reference separately to get focused parts
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
                final Intent intent = new Intent(this, BLEScanActivity.class);
                startActivity(intent);
                break;
            case R.id.sign_out_menu:
                AuthUI.getInstance().signOut(this);
                Intent signOutIntent = new Intent(this, LoginActivity.class);
                startActivity(signOutIntent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onResume() {
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(mGattUpdateReceiver);
        detachDatabaseReadListener();
        super.onPause();
    }

    private void attachDatabaseReadListener() {
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
    }

    private void detachDatabaseReadListener() {
        if (eventListener != null) {
            database.removeEventListener(eventListener);
            eventListener = null;
        }
    }

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BLEService.ACTION_GATT_CONNECTED.equals(action)) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        R.string.connected,
                        Toast.LENGTH_SHORT);
                toast.show();
            } else if (BLEService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        R.string.disconnected,
                        Toast.LENGTH_SHORT);
                toast.show();
            } else if (BLEService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "BLE service discovered",
                        Toast.LENGTH_SHORT);
                toast.show();
            } else if (BLEService.ACTION_DATA_AVAILABLE.equals(action)) {
                Toast toast = Toast.makeText(getApplicationContext(),
                       "Data streaming",
                        Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BLEService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BLEService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BLEService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BLEService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
