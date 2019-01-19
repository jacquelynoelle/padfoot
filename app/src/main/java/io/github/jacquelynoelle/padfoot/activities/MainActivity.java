package io.github.jacquelynoelle.padfoot.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import io.github.jacquelynoelle.padfoot.R;
import io.github.jacquelynoelle.padfoot.bluetoothle.BLEService;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();
    private TextView displayText;
    private DatabaseReference database;
    private ValueEventListener mStepCountListener;
    private ChildEventListener mHourlyStepCountListener;
    private ChildEventListener mWeeklyStepCountListener;
    private ArrayList<BarEntry> mHourlyEntries;
    private Integer mStepCount;
    private HashMap<String, Integer> mHourlySteps;
    private HashMap<String, Integer> mDailySteps;
    private String mPetID;
    private BarChart mHourlyChart;
    private Calendar mRightNow;
    private String mToday;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "onCreate");

        displayText = findViewById(R.id.tv_step_count);
        database = FirebaseDatabase.getInstance().getReference();

        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.app_file), Context.MODE_PRIVATE);
        mPetID = sharedPref.getString("petID", "test");

        mHourlySteps = new HashMap<>();
        mDailySteps = new HashMap<>();
        mHourlyChart = (BarChart) findViewById(R.id.chart_hourly);
        mRightNow = Calendar.getInstance();
        mHourlyEntries = new ArrayList<>();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.menu_connect).setVisible(true);
//        menu.findItem(R.id.menu_disconnect).setVisible(true);
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
//            case R.id.menu_disconnect:
//                final Intent bleDisconnectIntent = new Intent(this, BLEScanActivity.class);
//                startActivity(bleDisconnectIntent);
//                break;
            case R.id.menu_edit_profile:
                final Intent editProfileIntent = new Intent(this, EditProfileActivity.class);
//                editProfileIntent.putExtra("petID", getIntent().getStringExtra("petID"));
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
        mToday = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
        attachStepCountListener();
        attachHourlyStepCountListener();
        loadHourlyChart();
        loadWeeklyChart();
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(mGattUpdateReceiver);
        detachDatabaseReadListener();
        super.onPause();
    }

    private void attachStepCountListener() {
        mStepCountListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mStepCount = dataSnapshot.getValue(Integer.class);
                if (mStepCount != null) {
                    displayText.setText(Integer.toString(mStepCount));
                } else {
                    displayText.setText("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };

        String today = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());

        database.child("pets").child(mPetID).child("dailySteps").child(mToday).addValueEventListener(mStepCountListener);
    }

    private void attachHourlyStepCountListener() {
        mHourlyStepCountListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                int currentHourSteps = dataSnapshot.getValue(Integer.class) == null ? 0 : dataSnapshot.getValue(Integer.class);

                mHourlyEntries.add(new BarEntry(mHourlyEntries.size(), currentHourSteps));
//                mHourlyEntries.set(HOUROFADDEDCHILD, new BarEntry(HOUROFADDEDCHILD, currentHourSteps));

                mHourlyChart.invalidate();
                mHourlyChart.animateY(3000);
            }

            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                int currentHour = mRightNow.get(Calendar.HOUR_OF_DAY);
                int currentHourSteps = dataSnapshot.getValue(Integer.class) == null ? 0 : dataSnapshot.getValue(Integer.class);

                mHourlyEntries.set(currentHour, new BarEntry(currentHour, currentHourSteps));

//                mHourlyChart.invalidate();
            }
            public void onChildRemoved(DataSnapshot dataSnapshot) {}
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {}
            public void onCancelled(DatabaseError databaseError) {}
        };

        database.child("pets").child(mPetID).child("hourlySteps").addChildEventListener(mHourlyStepCountListener);
    }

    private void detachDatabaseReadListener() {
        if (mStepCountListener != null) {
            database.removeEventListener(mStepCountListener);
            mStepCountListener = null;
        }
        if (mHourlyStepCountListener != null) {
            database.removeEventListener(mHourlyStepCountListener);
            mHourlyStepCountListener = null;
        }
    }

    private void loadHourlyChart() {
        mHourlyChart.setTouchEnabled(true);
        mHourlyChart.setDragEnabled(false);
        mHourlyChart.setScaleEnabled(false);
        mHourlyChart.setScaleXEnabled(false);
        mHourlyChart.setScaleYEnabled(false);
        mHourlyChart.setPinchZoom(false);
        mHourlyChart.setDoubleTapToZoomEnabled(false);
        mHourlyChart.setDragDecelerationEnabled(false);

        XAxis xAxis = mHourlyChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setAxisMinimum(-1);
        xAxis.setAxisMaximum(24);
        xAxis.setLabelCount(6);

        mHourlyChart.getAxisLeft().setEnabled(false);
        mHourlyChart.getAxisRight().setEnabled(false);
        mHourlyChart.getAxisLeft().setAxisMinimum(0f);

        mHourlyChart.getLegend().setEnabled(false);   // Hide the legend

        mHourlyEntries.add(new BarEntry(0, 0));
        BarDataSet barDataSet = new BarDataSet(mHourlyEntries, "Cells");
        barDataSet.setHighlightEnabled(true);
        barDataSet.setDrawValues(false);
        barDataSet.setHighLightColor(ContextCompat.getColor(this, R.color.colorPrimaryLight));
        barDataSet.setHighLightAlpha(255);

        BarData data = new BarData(barDataSet);
        mHourlyChart.setData(data); // set the data and list of lables into chart

        int[] colors = new int[]{
                R.color.colorAccent,
                R.color.colorAccentLight,
        };
        barDataSet.setColors(ColorTemplate.createColors(getResources(), colors));


        Description description = new Description();
        description.setText("");
        mHourlyChart.setDescription(description); // set the description
        mHourlyChart.setDrawBorders(false);
    }


    private void loadWeeklyChart() {
        BarChart chart = (BarChart) findViewById(R.id.chart_weekly);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);
        chart.setScaleXEnabled(false);
        chart.setScaleYEnabled(false);
        chart.setPinchZoom(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.setDragDecelerationEnabled(false);

        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, 3));
        entries.add(new BarEntry(1, 1));
        entries.add(new BarEntry(2, 2));
        entries.add(new BarEntry(3, 3));
        entries.add(new BarEntry(4, 4));
        entries.add(new BarEntry(5, 5));
        entries.add(new BarEntry(6, 3));

        BarDataSet barDataSet = new BarDataSet(entries, "Cells");
        barDataSet.setHighlightEnabled(true);
        barDataSet.setDrawValues(false);
        barDataSet.setHighLightColor(ContextCompat.getColor(this, R.color.colorPrimaryLight));
        barDataSet.setHighLightAlpha(255);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setAxisMinimum(-0.5f);
        xAxis.setLabelCount(6);

        chart.getAxisLeft().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setAxisMinimum(0f);

        chart.getLegend().setEnabled(false);   // Hide the legend

        ArrayList<String> labels = new ArrayList<String>();
        labels.add("Dec");
        labels.add("Nov");
        labels.add("Oct");
        labels.add("Sep");
        labels.add("Aug");
        labels.add("Jul");
        labels.add("Jun");
        labels.add("May");
        labels.add("Apr");
        labels.add("Mar");
        labels.add("Feb");
        labels.add("Jan");

        BarData data = new BarData(barDataSet);
        chart.setData(data); // set the data and list of lables into chart

        Description description = new Description();
        description.setText("");
        chart.setDescription(description); // set the description
        chart.setDrawBorders(false);

        int[] colors = new int[]{
                R.color.colorAccent,
                R.color.colorAccentLight,
        };
        barDataSet.setColors(ColorTemplate.createColors(getResources(), colors));

        chart.invalidate();
        chart.animateY(3000);
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
