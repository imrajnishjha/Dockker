package com.example.blockchain;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth mAuth ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        new Handler().postDelayed(() -> {
            if(mAuth.getCurrentUser() != null) startActivity(new Intent(MainActivity.this, StudentDashboard.class));
            else startActivity(new Intent(MainActivity.this, Login.class));
            finish();
        },1000);



    }
}
