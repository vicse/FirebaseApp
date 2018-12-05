package com.ore.vicse.firebaseapp.activities;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ore.vicse.firebaseapp.R;
import com.ore.vicse.firebaseapp.adapter.UserRVAdapter;
import com.ore.vicse.firebaseapp.models.User;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private FirebaseAnalytics mFirebaseAnalytics;

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final UserRVAdapter userRVAdapter = new UserRVAdapter();
        recyclerView.setAdapter(userRVAdapter);

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                Log.d(TAG, "onChildAdded " + dataSnapshot.getKey());

                //Obtenidndo nuevo user de firebase
                String postKey = dataSnapshot.getKey();
                final User addedUser = dataSnapshot.getValue(User.class);
                Log.d(TAG, "addedUser " + addedUser);

                //Actualizando adapter datasource
                List<User> users = userRVAdapter.getUsers();
                users.add(0, addedUser);
                userRVAdapter.notifyDataSetChanged();


            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                Log.d(TAG, "onChildChanged " + dataSnapshot.getKey());

                //obteniendo user modificado de firebase
                String userKey = dataSnapshot.getKey();
                User changeUser = dataSnapshot.getValue(User.class);
                Log.d(TAG, "changedUser" +changeUser);

                //Actualizando adapter datasource
                List<User> users = userRVAdapter.getUsers();
                int index = users.indexOf(changeUser);
                if(index != -1){
                    users.set(index, changeUser);
                }
                userRVAdapter.notifyDataSetChanged();

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved" + dataSnapshot.getKey());

                String userKey = dataSnapshot.getKey();
                User removedUser = dataSnapshot.getValue(User.class);
                Log.d(TAG, "removedUser" +removedUser);

                List<User> users = userRVAdapter.getUsers();
                users.remove(removedUser);
                userRVAdapter.notifyDataSetChanged();

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                Log.d(TAG, "onChildMoved" +dataSnapshot.getKey());

                User movedUser = dataSnapshot.getValue(User.class);
                String userKey = dataSnapshot.getKey();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "onCancelled" + databaseError.getMessage(), databaseError.toException());
            }
        };
        usersRef.addChildEventListener(childEventListener);



        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Bundle bundle = new Bundle();
        bundle.putString("fullname","Ore Soto Vicse");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle);

        mFirebaseAnalytics.setUserProperty("username", "osvicse");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                callLogout(null);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void callLogout(View view){
        Log.d(TAG, "Ssign out user");
        FirebaseAuth.getInstance().signOut();
        finish();
    }
}
