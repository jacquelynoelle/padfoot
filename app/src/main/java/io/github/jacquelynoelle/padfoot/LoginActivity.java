package io.github.jacquelynoelle.padfoot;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;

import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    // firebase auth
    public static final int RC_SIGN_IN = 2;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private String mUsername;
    public static final String ANONYMOUS = "anonymous";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initialize firebase auth instance
        mFirebaseAuth = FirebaseAuth.getInstance();

        // firebase auth
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    onSignedInInitialize(user);
                    // TODO: Check for pet profile setup and BLE connection
                    Intent signedInIntent = new Intent(LoginActivity.this, MainActivity.class);
                    signedInIntent.putExtra("username", mUsername);
                    startActivity(signedInIntent);
                } else {
                    // User is signed out
                    onSignedOutCleanup();
                    final List<AuthUI.IdpConfig> providers = Arrays.asList(
                            new AuthUI.IdpConfig.GoogleBuilder().build(),
                            new AuthUI.IdpConfig.EmailBuilder().build()
                    );
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(!BuildConfig.DEBUG /* credentials */, true /* hints */)
                                    .setAvailableProviders(providers)
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
    }

    private void onSignedInInitialize(FirebaseUser user) {
        mUsername = user.getDisplayName();
    }

    private void onSignedOutCleanup() {
        mUsername = ANONYMOUS;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // Sign-in succeeded, set up the UI
                // TODO: Check for pet profile setup and BLE connection
                FirebaseUser user = mFirebaseAuth.getCurrentUser();
                FirebaseUserMetadata metadata = user.getMetadata();
                onSignedInInitialize(user);
                if (metadata.getCreationTimestamp() == metadata.getLastSignInTimestamp()) {
                    Intent loginNewUser = new Intent(LoginActivity.this, ProfileActivity.class);
                    loginNewUser.putExtra("username", mUsername);
                    startActivity(loginNewUser);
                    // Toast.makeText(this, "Welcome to Padfoot!", Toast.LENGTH_SHORT).show();
                } else {
                    Intent loginExistingUser = new Intent(LoginActivity.this, MainActivity.class);
                    loginExistingUser.putExtra("username", mUsername);
                    startActivity(loginExistingUser);
                    // String greeting = "Welcome back, " + mUsername;
                    // Toast.makeText(this, greeting, Toast.LENGTH_SHORT).show();
                }
            } else if (resultCode == RESULT_CANCELED) {
                // Sign in was canceled by the user, finish the activity
                Toast.makeText(this, "Sign in canceled", Toast.LENGTH_SHORT).show();
                finish();
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
}