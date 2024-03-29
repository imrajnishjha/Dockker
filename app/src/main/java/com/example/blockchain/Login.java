package com.example.blockchain;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class Login extends AppCompatActivity {

    TextView StudentReg,ForgotPass;
    AppCompatButton loginBackBtn,loginBtn;
    FirebaseAuth mAuth;
    EditText loginEmail;
    TextInputEditText loginPassword;
    RelativeLayout progressbar;
    DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("user");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        StudentReg = findViewById(R.id.login_new_user_tv);
        ForgotPass = findViewById(R.id.login_forgot_password_tv);
        loginBackBtn = findViewById(R.id.login_back_btn);
        loginBtn = findViewById(R.id.login_btn);
        progressbar = findViewById(R.id.user_login_progressBarRL);
        mAuth = FirebaseAuth.getInstance();

        //Methodology
        loginBackBtn.setOnClickListener(view -> finish());

        loginBtn.setOnClickListener(view -> loginUser(progressbar));

        StudentReg.setOnClickListener(view -> startActivity(new Intent(this, StudentRegistration.class)));
        ForgotPass.setOnClickListener(view -> startActivity(new Intent(this, ForgotPassword.class)));
    }

    private void loginUser(RelativeLayout progressbar){
        loginEmail = findViewById(R.id.login_username_edtTxt);
        loginPassword = findViewById(R.id.login_password_edtTxt);
        String login_email = loginEmail.getText().toString().toLowerCase();
        String login_password = Objects.requireNonNull(loginPassword.getText()).toString();

        if(TextUtils.isEmpty(login_email)){
            loginEmail.setError("Email cannot be empty!");
            loginEmail.requestFocus();
        }else if(TextUtils.isEmpty(login_password)){
            loginPassword.setError("Password cannot be empty!");
            loginPassword.requestFocus();
        } else {
            progressbar.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(login_email, login_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        dRef.child(login_email.replaceAll("\\.","%7")).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String usertype =  Objects.requireNonNull(snapshot.child("type").getValue()).toString();
                                if(usertype.equals("0")){
                                    startActivity(new Intent(Login.this,StudentDashboard.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK));
                                    finish();
                                    Toast.makeText(Login.this, "You are logged in!", Toast.LENGTH_SHORT).show();
                                } else if(usertype.equals("1")){
                                    startActivity(new Intent(Login.this, AdminDashboard.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK));
                                    finish();
                                    Toast.makeText(Login.this, "You are logged in!", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    } else {
                        Toast.makeText(Login.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    progressbar.setVisibility(View.GONE);
                }
            });
        }

    }
}