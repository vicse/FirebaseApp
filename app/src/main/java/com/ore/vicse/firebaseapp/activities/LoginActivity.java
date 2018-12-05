package com.ore.vicse.firebaseapp.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ore.vicse.firebaseapp.R;
import com.ore.vicse.firebaseapp.models.User;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    private Double latitude;
    private Double longitude;

    private ProgressBar progressBar;
    private View loginPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        loginPanel = findViewById(R.id.login_panel);

        initFirebaseAuth();

        // Init FirebaseAuthStateListener
        initFirebaseAuthStateListener();

        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                Toast.makeText(LoginActivity.this, "Localización:"+ "Latitud"+location.getLatitude() + latitude+",Longitud: "+longitude, Toast.LENGTH_LONG).show();
            }
        });

    }

    private FirebaseAuth mAuth;

    private EditText emailInput;
    private EditText passwordInput;

    private void initFirebaseAuth() {
        // initialize the FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        emailInput = (EditText) findViewById(R.id.email_input);
        passwordInput = (EditText) findViewById(R.id.password_input);
    }

    public void callLogin(View view) {

        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "You must complete these fields", Toast.LENGTH_SHORT).show();
            return;
        }
        loginPanel.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        // Sign In user
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmailAndPassword:onComplete:" + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            loginPanel.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            Log.e(TAG, "signInWithEmailAndPassword:failed", task.getException());
                            Toast.makeText(LoginActivity.this, "Username and/or password invalid", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void callRegister(View view) {
        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "You must complete these fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Using a weak password", Toast.LENGTH_SHORT).show();
            return;
        }

        loginPanel.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "createUserWithEmailAndPassword:onComplete:" + task.isSuccessful());
                if (!task.isSuccessful()) {
                    loginPanel.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "createUserWithEmailAndPassword:failed", task.getException());
                    Toast.makeText(LoginActivity.this, "Register failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private FirebaseAuth.AuthStateListener mAuthListener;

    private void initFirebaseAuthStateListener() {
        // and the AuthStateListener method so you can track whenever the user signs in or out
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                if (currentUser != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + currentUser.getUid());

                    Toast.makeText(LoginActivity.this, "Welcome " + currentUser.getEmail(), Toast.LENGTH_SHORT).show();

                    final User user = new User();
                    user.setUid(currentUser.getUid());
                    user.setDisplayName(currentUser.getDisplayName());
                    user.setEmail(currentUser.getEmail());
                    user.setLatitude(latitude);
                    user.setLongitude(longitude);

                    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
                    usersRef.child(user.getUid()).setValue(user)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Log.d(TAG, "onSuccess"+user.getLongitude()+"...."+user.getLatitude());
                                        Toast.makeText(LoginActivity.this, "langitude" +user.getLongitude() + "longitude: "+user.getLongitude(), Toast.LENGTH_LONG).show();
                                    }else{
                                        Log.d(TAG, "onFailure", task.getException());
                                    }
                                }
                            });

                    // Go MainActivity
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

}
