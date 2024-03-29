package com.example.blockchain;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;

import android.content.Intent;
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
    CardView guideCard, projectCard, placementCard, adminCard, accountCard;
    FirebaseAuth mAuth;
    String userEmail, convertedEmail;
    AppCompatButton backBtn;
    int statusGuide = -1, statusProject = -1, statusPlacement = -1, statusFinance = -1, statusAdmin = -1;
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

        guideCard = findViewById(R.id.guideCard);
        projectCard = findViewById(R.id.project_cod_card);
        placementCard = findViewById(R.id.placement_card);
        adminCard = findViewById(R.id.adminCard);
        accountCard = findViewById(R.id.accoutnCard);

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
                    statusGuide = StatusMaker(guideCheck,Integer.parseInt(snapshot.child("guide").getValue().toString()));
                    statusProject = StatusMaker(projectCheck,Integer.parseInt(snapshot.child("projectcod").getValue().toString()));
                    statusPlacement = StatusMaker(placementCheck,Integer.parseInt(snapshot.child("placement").getValue().toString()));
                    statusFinance = StatusMaker(accountsCheck,Integer.parseInt(snapshot.child("finance").getValue().toString()));
                    statusAdmin = StatusMaker(adminCheck,Integer.parseInt(snapshot.child("admin").getValue().toString()));
                    Log.d("oio", "onDataChange: "+statusPlacement);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        backBtn.setOnClickListener(v -> finish());

        guideCard.setOnClickListener(v ->{
            Log.d("oio", "onCreate: "+statusGuide);
            if(statusGuide != 1 && statusGuide != -1) startActivity(new Intent(getApplicationContext(), NocStatusDescription.class).putExtra("status",statusGuide).putExtra("type",0));
        });
        projectCard.setOnClickListener(v -> {
            if(statusProject != 1 && statusGuide != -1) startActivity(new Intent(getApplicationContext(), NocStatusDescription.class).putExtra("status",statusProject).putExtra("type",1));
        });
        placementCard.setOnClickListener(v -> {
            Log.d("oio", "onCreate: "+statusPlacement);
            if(statusPlacement != 1 && statusGuide != -1) startActivity(new Intent(getApplicationContext(), NocStatusDescription.class).putExtra("status",statusPlacement).putExtra("type",2));
        });
        accountCard.setOnClickListener(v -> {
            if(statusFinance != 1 && statusGuide != -1) startActivity(new Intent(getApplicationContext(), NocStatusDescription.class).putExtra("status",statusFinance).putExtra("type",3));
        });
        adminCard.setOnClickListener(v -> {
            if(statusAdmin != 1 && statusGuide != -1) startActivity(new Intent(getApplicationContext(), NocStatusDescription.class).putExtra("status",statusAdmin).putExtra("type",4));
        });

    }
    public int StatusMaker(ImageView imgHolder, int val){
        int cardStatus = -1;
        if(val == 0){
            cardStatus = 0;
            Glide.with(getApplicationContext())
                    .load(R.drawable.cancel)
                    .error(R.drawable.cancel)
                    .placeholder(R.drawable.cancel)
                    .into(imgHolder);
        } else if(val == 1){
            cardStatus = 1;
            Glide.with(getApplicationContext())
                    .load(R.drawable.wait)
                    .error(R.drawable.wait)
                    .placeholder(R.drawable.wait)
                    .into(imgHolder);
        } else if(val == 2){
            cardStatus = 2;
            Glide.with(getApplicationContext())
                    .load(R.drawable.check)
                    .error(R.drawable.check)
                    .placeholder(R.drawable.check)
                    .into(imgHolder);
        }
        return cardStatus;
    }
}