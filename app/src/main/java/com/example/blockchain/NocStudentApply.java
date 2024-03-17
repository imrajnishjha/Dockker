package com.example.blockchain;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class NocStudentApply extends AppCompatActivity {

    ImageView guideCheck, projectCheck, placementCheck, adminCheck, accountsCheck;
    FirebaseAuth mAuth;
    String userEmail, convertedEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noc_student_apply);

        guideCheck = findViewById(R.id.guide_check);
        projectCheck = findViewById(R.id.projectCheck);
        placementCheck = findViewById(R.id.placementCheck);
        adminCheck = findViewById(R.id.adminCheck);
        accountsCheck = findViewById(R.id.accountCheck);

        mAuth = FirebaseAuth.getInstance();
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()!=null){
            userEmail = Objects.requireNonNull(mAuth.getCurrentUser()).getEmail();
        }
        assert userEmail != null;
        convertedEmail = userEmail.replaceAll("\\.", "%7");

        DatabaseReference statusRef = FirebaseDatabase.getInstance().getReference("noc").child(convertedEmail);


    }
}