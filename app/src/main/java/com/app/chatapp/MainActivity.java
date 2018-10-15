package com.app.chatapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int CODE_LOGIN = 100;
    protected static final String prefKey = "com.app.chatapp";
    protected static final String prefAuthUIDKey = "com.app.chatapp.firebaseauth.user.uid";

    private String userUID;
    private String userPhoneNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Check sign in
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            saveMyUID(auth.getCurrentUser().getUid());
            userPhoneNo = auth.getCurrentUser().getPhoneNumber();
        } else {
            startActivityForResult(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(Arrays.asList(
                            new AuthUI.IdpConfig.PhoneBuilder().build()
                    )).build(), CODE_LOGIN);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void saveMyUID(String UID) {
        userUID = UID;
        SharedPreferences pref = this.getSharedPreferences(prefKey, Context.MODE_PRIVATE);
        pref.edit().putString(prefAuthUIDKey, userUID).apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODE_LOGIN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            // Successfully signed in
            if (resultCode == RESULT_OK) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    saveMyUID(user.getUid());
                    userPhoneNo = user.getPhoneNumber();
                }
                Log.wtf(TAG, "Login success.");
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show();
                } else if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, "No Network.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Unknown Login Error", Toast.LENGTH_SHORT).show();
                    Log.wtf(TAG, "Sign-in error: ", response.getError());
                }
                finishAffinity();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
