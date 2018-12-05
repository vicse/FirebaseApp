package com.ore.vicse.firebaseapp.adapter;


import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ore.vicse.firebaseapp.R;
import com.ore.vicse.firebaseapp.models.User;

import java.util.ArrayList;
import java.util.List;

public class UserRVAdapter extends RecyclerView.Adapter<UserRVAdapter.ViewHolder> {

    private static final String TAG = UserRVAdapter.class.getSimpleName();

    private List<User> users;

    public List<User> getUsers(){
        return users;
    }

    public void setUsers(List<User> users){
        this.users = users;
    }

    public UserRVAdapter(){
        this.users = new ArrayList<>();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView emailText,latText,lonText;

        ViewHolder(View itemView) {
            super(itemView);
            emailText = itemView.findViewById(R.id.user_email);
            latText = itemView.findViewById(R.id.user_lat);
            lonText = itemView.findViewById(R.id.user_lon);

        }


    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_user, viewGroup, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int position) {

        User user = users.get(position);

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange" + dataSnapshot.getKey());
                final User user = dataSnapshot.getValue(User.class);

                Double dLat = user.getLatitude();
                Double dLon = user.getLongitude();

               /* String stringLat = new Double(dLat).toString();
                String stringLon = new Double(dLon).toString();*/

                viewHolder.emailText.setText(user.getEmail());
                viewHolder.latText.setText(""+dLat);
                viewHolder.lonText.setText(""+dLon);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "onCancelled " + databaseError.getMessage(), databaseError.toException());
            }
        });

    }

    @Override
    public int getItemCount() {
        return this.users.size();
    }



}
