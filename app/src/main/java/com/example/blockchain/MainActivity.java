package com.example.blockchain;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth mAuth ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        new Handler().postDelayed(() -> {
            if(mAuth.getCurrentUser() != null) {
                String userEmail = Objects.requireNonNull(mAuth.getCurrentUser()).getEmail();
                String convertedEmail = userEmail.replaceAll("\\.", "%7");
                FirebaseDatabase.getInstance().getReference("user").child(convertedEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int type = Integer.parseInt(snapshot.child("type").getValue().toString());
                        if(type == 0){
                            startActivity(new Intent(MainActivity.this, StudentDashboard.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        }
                        else
                            startActivity(new Intent(MainActivity.this, AdminDashboard.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
            else startActivity(new Intent(MainActivity.this, Login.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));

        },1000);



    }
}
