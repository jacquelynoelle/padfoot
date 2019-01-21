package io.github.jacquelynoelle.padfoot;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class DailyStepReset extends IntentService {

    // firebase database
    DatabaseReference mPetsReference;
    String mPetID;
    String mToday;

    public DailyStepReset() {
        super("DailyStepReset");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            mPetsReference = FirebaseDatabase.getInstance().getReference("pets");
            SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.app_file), Context.MODE_PRIVATE);
            mPetID = sharedPref.getString("petID", "test");
            mToday = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());

            // triggered at midnight, need to create new day entry and start step count at 0
            // important to have log of all days (even with no step count due to no connection) for graph purposes
            mPetsReference.child(mPetID).child("dailySteps").child(mToday).setValue(99);

            // reset hourly totals for current day
            for(int i = 0; i < 24; i++) {
                mPetsReference.child(mPetID).child("hourlySteps").child(Integer.toString(i)).setValue(0);
            }
        }
    }
}
