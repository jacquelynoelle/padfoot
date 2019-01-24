package io.github.jacquelynoelle.padfoot.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.StackedValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import io.github.jacquelynoelle.padfoot.DailyStepReset;
import io.github.jacquelynoelle.padfoot.R;
import io.github.jacquelynoelle.padfoot.ValueFormatter;
import io.github.jacquelynoelle.padfoot.bluetoothle.BLEService;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    private TextView displayText;
    private DatabaseReference database;
    private String mPetID;

    private Integer mStepCount;
    private ValueEventListener mStepCountListener;

    private BarChart mHourlyChart;
    private ArrayList<BarEntry> mHourlyEntries;
    private HashMap<String, Integer> mHourlySteps;
    private ChildEventListener mHourlyStepCountListener;

    private BarChart mWeeklyChart;
    private ArrayList<BarEntry> mWeeklyEntries;
    private HashMap<String, Integer> mDailySteps;
    private ChildEventListener mWeeklyStepCountListener;

    private Calendar mRightNow;
    private String mToday;

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    private static final int REQUEST_CODE = 0; // for alarm

    private boolean mConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        displayText = findViewById(R.id.tv_step_count);
        database = FirebaseDatabase.getInstance().getReference();

        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.app_file), Context.MODE_PRIVATE);
        mPetID = sharedPref.getString("petID", "test");

        mHourlyChart = findViewById(R.id.chart_hourly);
        mHourlyEntries = new ArrayList<>();
        mHourlySteps = new HashMap<>();

        mWeeklyChart = findViewById(R.id.chart_weekly);
        mWeeklyEntries = new ArrayList<>();
        mDailySteps = new HashMap<>();

        mRightNow = Calendar.getInstance();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (!mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        }
        menu.findItem(R.id.menu_edit_profile).setVisible(true);
        menu.findItem(R.id.menu_sign_out).setVisible(true);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        }
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
                final Intent bleDisconnectIntent = new Intent(this, BLEService.class);
                bleDisconnectIntent.setAction(BLEService.ACTION_GATT_DISCONNECT);
                startService(bleDisconnectIntent);
                break;
            case R.id.menu_edit_profile:
                final Intent editProfileIntent = new Intent(this, EditProfileActivity.class);
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
        loadBackground();

        mToday = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
        mHourlyEntries.clear();
        mWeeklyEntries.clear();
        attachStepCountListener();
        attachHourlyStepCountListener();
        attachWeeklyStepCountListener();
        loadHourlyChart();
        loadWeeklyChart();

        startDailyStepCountRefresh();
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(mGattUpdateReceiver);
        detachDatabaseReadListeners();
        super.onPause();
    }

    private void loadBackground() {
        HorizontalScrollView background = findViewById(R.id.background);
        WindowManager window = (WindowManager)getSystemService(WINDOW_SERVICE);
        Display display = window.getDefaultDisplay();
        Drawable portrait = getDrawable(R.mipmap.padfoot_home);
        int num = display.getRotation();
        if (num == 1 || num == 3) {
            background.setBackgroundColor(getColor(R.color.colorPrimaryLight));

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(1000, 1000);
            lp.setMargins(0, 0, 80, 0);

            CardView card1 = findViewById(R.id.card_steps);
            card1.setLayoutParams(lp);
            CardView card2 = findViewById(R.id.card_hourly);
            card2.setLayoutParams(lp);
            CardView card3 = findViewById(R.id.card_weekly);
            card3.setLayoutParams(lp);
        } else {
            background.setBackground(portrait);
        }
    }

    private void startDailyStepCountRefresh() {
        alarmMgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, DailyStepReset.class);
        alarmIntent = PendingIntent.getService(this, 0, intent, 0);

        // Set the alarm to start at midnight
        Calendar alarmCalendar = Calendar.getInstance();
        alarmCalendar.setTimeInMillis(System.currentTimeMillis());
        alarmCalendar.set(Calendar.HOUR_OF_DAY, 23);
        alarmCalendar.set(Calendar.MINUTE, 59);
        alarmCalendar.set(Calendar.SECOND, 59);

        int dailyInterval = 1000 * 60 * 60 * 24;

        // setRepeating() interval on 24 hour cycle at midnight
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, alarmCalendar.getTimeInMillis() + 1000,
                dailyInterval, alarmIntent);
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

        database.child("pets").child(mPetID).child("dailySteps").child(mToday).addValueEventListener(mStepCountListener);
    }

    private void attachHourlyStepCountListener() {
        mHourlyStepCountListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                int currentHour = mRightNow.get(Calendar.HOUR_OF_DAY);
                int currentHourSteps = dataSnapshot.getValue(Integer.class) == null ? 0 : dataSnapshot.getValue(Integer.class);

                mHourlyEntries.add(new BarEntry(mHourlyEntries.size(), currentHourSteps));

                if (previousChildName == null) {
                    mHourlyEntries.remove(0);
                }

                if (currentHourSteps > mHourlyChart.getAxisLeft().getAxisMaximum()) {
                    mHourlyChart.getAxisLeft().setAxisMaximum(currentHourSteps + 100);
                }

                mHourlyChart.notifyDataSetChanged();
                mHourlyChart.invalidate();
                mHourlyChart.highlightValue(currentHour, 0);

                mHourlyChart.animateY(3000);
            }

            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                int currentHour = mRightNow.get(Calendar.HOUR_OF_DAY);
                int currentHourSteps = dataSnapshot.getValue(Integer.class) == null ? 0 : dataSnapshot.getValue(Integer.class);

                mHourlyChart.getAxisLeft().resetAxisMaximum();
                mHourlyEntries.set(currentHour, new BarEntry(currentHour, currentHourSteps));

                // only set the data here because the chart would be reloading too often
                // chart will reload on resume because it will trigger new child added
            }
            public void onChildRemoved(DataSnapshot dataSnapshot) {}
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {}
            public void onCancelled(DatabaseError databaseError) {}
        };

        database.child("pets").child(mPetID).child("hourlySteps").addChildEventListener(mHourlyStepCountListener);
    }

    private void attachWeeklyStepCountListener() {
        mWeeklyStepCountListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                int currentDaySteps = dataSnapshot.getValue(Integer.class) == null ? 0 : dataSnapshot.getValue(Integer.class);

                if (mWeeklyEntries.size() == 1 && mWeeklyEntries.get(0).getY() == -1) {
                    mWeeklyEntries.remove(0);
                }

                mWeeklyEntries.add(new BarEntry(mWeeklyEntries.size(), currentDaySteps));

                if (mWeeklyEntries.size() > 7) {
                    mWeeklyEntries.remove(0);
                }

                if (currentDaySteps > mWeeklyChart.getAxisLeft().getAxisMaximum()) {
                    mWeeklyChart.getAxisLeft().setAxisMaximum(currentDaySteps + 100);
                }

                mWeeklyChart.notifyDataSetChanged();
                mWeeklyChart.invalidate();
                mWeeklyChart.highlightValue(mWeeklyEntries.size() - 1, 0);

                mWeeklyChart.animateY(3000);
            }

            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                final int TODAY = 6;
                int currentDaySteps = dataSnapshot.getValue(Integer.class) == null ? 0 : dataSnapshot.getValue(Integer.class);

                mWeeklyChart.getAxisLeft().resetAxisMaximum();
                mWeeklyEntries.set(TODAY, new BarEntry(TODAY, currentDaySteps));

                // only set the data here because the chart would be reloading too often
                // chart will reload on resume because it will trigger new child added
            }
            public void onChildRemoved(DataSnapshot dataSnapshot) {}
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {}
            public void onCancelled(DatabaseError databaseError) {}
        };

        DatabaseReference dailyStepsRef = database.child("pets").child(mPetID).child("dailySteps");
        Query lastWeek = dailyStepsRef.orderByKey().limitToLast(7);
        lastWeek.addChildEventListener(mWeeklyStepCountListener);
    }

    private void detachDatabaseReadListeners() {
        if (mStepCountListener != null) {
            database.removeEventListener(mStepCountListener);
            mStepCountListener = null;
        }
        if (mHourlyStepCountListener != null) {
            database.removeEventListener(mHourlyStepCountListener);
            mHourlyStepCountListener = null;
        }
        if (mWeeklyStepCountListener != null) {
            database.removeEventListener(mWeeklyStepCountListener);
            mWeeklyStepCountListener = null;
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
        xAxis.setAxisMinimum(-0.5f);
        xAxis.setAxisMaximum(24.5f);

        mHourlyChart.getAxisLeft().setEnabled(false);
        mHourlyChart.getAxisRight().setEnabled(false);
        mHourlyChart.getAxisLeft().setAxisMinimum(0f);
        mHourlyChart.getAxisLeft().setAxisMaximum(500f);

        mHourlyChart.getLegend().setEnabled(false);   // Hide the legend

        mHourlyEntries.add(new BarEntry(0, -1)); // Add a single placeholder until data comes back from Firebase

        BarDataSet barDataSet = new BarDataSet(mHourlyEntries, "Cells");
        barDataSet.setHighlightEnabled(true);
        barDataSet.setValueFormatter(new ValueFormatter());
        barDataSet.setDrawValues(true);
        barDataSet.setValueTextSize(6);
        barDataSet.setValueTextColor(ContextCompat.getColor(this, R.color.colorLightGrey));
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
        mWeeklyChart.setTouchEnabled(true);
        mWeeklyChart.setDragEnabled(false);
        mWeeklyChart.setScaleEnabled(false);
        mWeeklyChart.setScaleXEnabled(false);
        mWeeklyChart.setScaleYEnabled(false);
        mWeeklyChart.setPinchZoom(false);
        mWeeklyChart.setDoubleTapToZoomEnabled(false);
        mWeeklyChart.setDragDecelerationEnabled(false);

        XAxis xAxis = mWeeklyChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setAxisMinimum(-0.5f);
        xAxis.setAxisMaximum(6.5f);

        mWeeklyChart.getAxisLeft().setEnabled(false);
        mWeeklyChart.getAxisRight().setEnabled(false);
        mWeeklyChart.getAxisLeft().setAxisMinimum(0f);
        mWeeklyChart.getAxisLeft().setAxisMaximum(1000f);

        mWeeklyChart.getLegend().setEnabled(false);   // Hide the legend

        mWeeklyEntries.add(new BarEntry(0, -1));

        BarDataSet barDataSet = new BarDataSet(mWeeklyEntries, "Cells");
        barDataSet.setHighlightEnabled(true);
        barDataSet.setValueFormatter(new ValueFormatter());
        barDataSet.setDrawValues(true);
        barDataSet.setValueTextSize(8);
        barDataSet.setValueTextColor(ContextCompat.getColor(this, R.color.colorLightGrey));
        barDataSet.setHighLightColor(ContextCompat.getColor(this, R.color.colorPrimaryLight));
        barDataSet.setHighLightAlpha(255);

        BarData data = new BarData(barDataSet);
        mWeeklyChart.setData(data); // set the data and list of lables into mWeeklyChart

        int[] colors = new int[]{
                R.color.colorAccent,
                R.color.colorAccentLight,
        };
        barDataSet.setColors(ColorTemplate.createColors(getResources(), colors));

        Description description = new Description();
        description.setText("");
        mWeeklyChart.setDescription(description); // set the description
        mWeeklyChart.setDrawBorders(false);
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
            if (BLEService.ACTION_GATT_DISCONNECTED.equals(action)) {
                if (mConnected) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            R.string.disconnected,
                            Toast.LENGTH_SHORT);
                    toast.show();
                    mConnected = false;
                }
            } else if (BLEService.ACTION_DATA_AVAILABLE.equals(action)) {
                if (!mConnected) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            R.string.connected,
                            Toast.LENGTH_SHORT);
                    toast.show();
                    mConnected = true;
                }
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
