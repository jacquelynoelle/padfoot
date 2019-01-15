package io.github.jacquelynoelle.padfoot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.firebase.ui.auth.AuthUI;

import io.github.jacquelynoelle.padfoot.bluetoothle.BLEScanActivity;
import io.github.jacquelynoelle.padfoot.bluetoothle.BLEService;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();
    private TextView displayText;
    private DatabaseReference database;
    private ChildEventListener eventListener;
    private Integer mStepCount;

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
                editProfileIntent.putExtra("petID", getIntent().getStringExtra("petID"));
                startActivity(editProfileIntent);
                break;
            case R.id.menu_sign_out:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                Intent signOutIntent = new Intent(MainActivity.this, SplashActivity.class);
                                startActivity(signOutIntent);
                            }
                        });
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onResume() {
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        attachDatabaseReadListener();
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
                mStepCount = dataSnapshot.getValue(Integer.class); // not working when most recent data is not a Pet object
                displayText.setText(mStepCount.toString());
            }

            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
            public void onChildRemoved(DataSnapshot dataSnapshot) {}
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            public void onCancelled(DatabaseError databaseError) {}
        };

        database.child("test-data").addChildEventListener(eventListener);
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
