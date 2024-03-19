package com.example.blockchain;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class StudentStatus extends AppCompatActivity {
    ImageView guideCheck, projectCheck, placementCheck, adminCheck, accountsCheck;
    FirebaseAuth mAuth;
    String userEmail, convertedEmail;
    AppCompatButton backBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_status);

        guideCheck = findViewById(R.id.guide_check);
        projectCheck = findViewById(R.id.projectCheck);
        placementCheck = findViewById(R.id.placementCheck);
        adminCheck = findViewById(R.id.adminCheck);
        accountsCheck = findViewById(R.id.accountCheck);
        backBtn = findViewById(R.id.status_back_button);

        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()!=null){
            userEmail = Objects.requireNonNull(mAuth.getCurrentUser()).getEmail();
        }
        assert userEmail != null;
        convertedEmail = userEmail.replaceAll("\\.", "%7");

        DatabaseReference statusRef = FirebaseDatabase.getInstance().getReference("noc").child(convertedEmail);

        statusRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    StatusMaker(guideCheck,Integer.parseInt(snapshot.child("guide").getValue().toString()));
                    StatusMaker(projectCheck,Integer.parseInt(snapshot.child("projectcod").getValue().toString()));
                    StatusMaker(placementCheck,Integer.parseInt(snapshot.child("placement").getValue().toString()));
                    StatusMaker(accountsCheck,Integer.parseInt(snapshot.child("finance").getValue().toString()));
                    StatusMaker(accountsCheck,Integer.parseInt(snapshot.child("admin").getValue().toString()));

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        backBtn.setOnClickListener(v -> finish());

    }

    public void StatusMaker(ImageView imgHolder, int val){
        if(val == 0){
            Glide.with(getApplicationContext())
                    .load(R.drawable.cancel)
                    .error(R.drawable.cancel)
                    .placeholder(R.drawable.cancel)
                    .into(imgHolder);
        } else if(val == 1){
            Glide.with(getApplicationContext())
                    .load(R.drawable.wait)
                    .error(R.drawable.wait)
                    .placeholder(R.drawable.wait)
                    .into(imgHolder);
        } else if(val == 2){
            Glide.with(getApplicationContext())
                    .load(R.drawable.check)
                    .error(R.drawable.check)
                    .placeholder(R.drawable.check)
                    .into(imgHolder);
        }
    }
}