package io.github.jacquelynoelle.padfoot;

import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

public class SplashActivity extends AppCompatActivity {

    private final static String TAG = SplashActivity.class.getSimpleName();

    // firebase auth
    public static final int RC_SIGN_IN = 2;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private User currentUser;

    // firebase database
    FirebaseDatabase database;
    DatabaseReference usersReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.FullScreen);
        setContentView(R.layout.activity_splash);

//        final int SPLASH_DURATION = 500;  // show splash for .5s
//        showSplashScreen(SPLASH_DURATION);

        // initialize firebase database and references
        database = FirebaseDatabase.getInstance();
        usersReference = database.getReference("users");

        // initialize firebase auth instance
        mFirebaseAuth = FirebaseAuth.getInstance();

        // firebase auth
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                Log.i(TAG, "onAuthStateChanged");
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.i(TAG, "onAuthStateChanged: user is signed in - start");
                    onReturningUserLogin(user, new FirebaseStringCallback() {
                        @Override
                        public void onCallback(String value) {
                            Log.i("TAG", "onCallback");
                            currentUser = new User(user.getDisplayName(), value);
                            routeUser(user);
                        }
                    });
                    Log.i(TAG, "onAuthStateChanged: user is signed in - finish");
                } else {
                    // User is signed out
                    Log.i(TAG, "onAuthStateChanged: user is not signed in");
                    onSignedOutCleanup();
                    final List<AuthUI.IdpConfig> providers = Arrays.asList(
                            new AuthUI.IdpConfig.GoogleBuilder().build(),
                            new AuthUI.IdpConfig.EmailBuilder().build()
                    );
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setTheme(R.style.AppTheme)
                                    .setLogo(R.mipmap.padfoot_foreground)
                                    .setIsSmartLockEnabled(!BuildConfig.DEBUG /* credentials */, true /* hints */)
                                    .setAvailableProviders(providers)
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
    }

    private void onReturningUserLogin(final FirebaseUser firebaseUser, final FirebaseStringCallback callback) {
        Log.i(TAG, "onReturningUserLogin: start");
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(TAG, "onReturningUserLogin: onDataChange - start");
                String petID = dataSnapshot.child("petID").getValue(String.class);
                callback.onCallback(petID);
                Log.i(TAG, "onReturningUserLogin: onDataChange - finish");

//                String name = dataSnapshot.child("name").getValue(String.class);
//                currentUser.setName(name);
//
//                String petID = dataSnapshot.child("petID").getValue(String.class);
//                currentUser.setPetID(petID);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "onReturningUserLogin: onCancelled", databaseError.toException());
            }
        };
        usersReference.child(firebaseUser.getUid()).addListenerForSingleValueEvent(listener);
        Log.i(TAG, "onReturningUserLogin: finish");
    }

    private void onSignedOutCleanup() {
        currentUser = null;
    }

    private void routeUser(FirebaseUser firebaseUser) {
        // TODO: Check BLE connection
        if (currentUser.getPetID() != null) {
            Log.i(TAG, "routeUser: already setup pet profile");
            Intent signedInIntent = new Intent(SplashActivity.this, MainActivity.class);
            signedInIntent.putExtra("userID", firebaseUser.getUid());
            signedInIntent.putExtra("petID", currentUser.getPetID());
            startActivity(signedInIntent);
        } else {
            Log.i(TAG, "routeUser: already setup pet profile");
            Intent loginNewUser = new Intent(SplashActivity.this, ProfileActivity.class);
            loginNewUser.putExtra("userID", firebaseUser.getUid());
            startActivity(loginNewUser);
        }
        Log.i(TAG, "routeUser: finish");
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        finish();
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // Sign-in succeeded, set up the UI
                IdpResponse response = IdpResponse.fromResultIntent(data);
                final FirebaseUser user = mFirebaseAuth.getCurrentUser();
//                FirebaseUserMetadata metadata = user.getMetadata();
                if (response.isNewUser()) {
                    Log.i(TAG, "onActivityResult: new user");
                    currentUser = new User(user.getDisplayName(), null);
                    usersReference.child(user.getUid()).setValue(currentUser);
                    routeUser(user);
                } else {
                    Log.i(TAG, "onActivityResult: existing user");
                    onReturningUserLogin(user, new FirebaseStringCallback() {
                        @Override
                        public void onCallback(String value) {
                            Log.i("TAG", "onCallback");
                            currentUser = new User(user.getDisplayName(), value);
                            routeUser(user);
                        }
                    });
                }
            } else if (resultCode == RESULT_CANCELED) {
                // Sign in was canceled by the user, finish the activity
                Log.i(TAG, "onActivityResult: login cancelled");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

//    private void showSplashScreen(int duration) {
//        Handler handler = new Handler();
//
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Intent loginIntent = new Intent(SplashActivity.this, LoginActivity.class);
//                startActivity(loginIntent);
//                finish();
//            }
//            }, duration);
//        }
//    }
}