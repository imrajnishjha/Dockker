package com.example.blockchain;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Pattern;

public class ForgotPassword extends AppCompatActivity {

    EditText userEmailEdt;
    AppCompatButton resetPassBackBtn,resetPassSubmitBtn;
    TextInputEditText restPassword, confResetPass;
    TextInputLayout resetLayout, conLayout;
    RelativeLayout progressBar;
    int type = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        userEmailEdt = findViewById(R.id.reset_pass_username_edtTxt);
        restPassword = findViewById(R.id.reset_pass_password_edtTxt);
        confResetPass = findViewById(R.id.reset_pass_confirm_password_edtTxt);
        userEmailEdt = findViewById(R.id.reset_pass_username_edtTxt);
        resetPassBackBtn = findViewById(R.id.reset_pass_back_btn);
        resetPassSubmitBtn = findViewById(R.id.resetPass_btn);
        resetLayout = findViewById(R.id.reset_pass_layout);
        conLayout = findViewById(R.id.reset_conf_pass_layout);
        progressBar = findViewById(R.id.user_resetPass_progressBarRL);

        resetPassBackBtn.setOnClickListener(v-> finish());

        resetPassSubmitBtn.setOnClickListener(v->resetPass(userEmailEdt,restPassword,confResetPass,resetLayout,conLayout,progressBar));
    }

    private void resetPass( EditText userEmail, TextInputEditText pass, TextInputEditText confPass,TextInputLayout resetLayout, TextInputLayout conLayout, RelativeLayout progressBar){
        if(type == 0){
            String email = userEmail.getText().toString().toLowerCase();
            if(TextUtils.isEmpty(email)){
                userEmail.setError("Email cannot be empty!");
                userEmail.requestFocus();
            } else if(!isValidEmailId(email.trim())){
                userEmail.setError("Enter valid email");
                userEmail.requestFocus();
            } else {
                progressBar.setVisibility(View.VISIBLE);
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("user");
                userRef.child(email.replaceAll("\\.", "%7")).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            type = 1;
                            userEmail.setVisibility(View.GONE);
                            resetLayout.setVisibility(View.VISIBLE);
                            conLayout.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);

                        } else {
                            userEmail.setError("Email doesn't exists!");
                            userEmail.requestFocus();
                            progressBar.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        }
        else {
            if(TextUtils.isEmpty(pass.getText().toString())){
                pass.setError("Enter the password");
                pass.requestFocus();
            } else if(TextUtils.isEmpty(confPass.getText().toString())){
                confPass.setError("Enter confirm Password");
                confPass.requestFocus();
            } else if(!pass.getText().toString().equals(confPass.getText().toString())){
                confPass.setError("Confirm password doesn't match");
                confPass.requestFocus();
            } else{
                progressBar.setVisibility(View.VISIBLE);
                FirebaseAuth mAuth = FirebaseAuth.getInstance();

                mAuth.fetchSignInMethodsForEmail(userEmail.getText().toString()).addOnCompleteListener( task ->{
                    if(task.isSuccessful()){
                        if (task.getResult().getSignInMethods().size() > 0){
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if(user != null){
                                user.updatePassword(pass.getText().toString()).addOnCompleteListener(updatePasswordTask ->{
                                    if (updatePasswordTask.isSuccessful()) {
                                        // Password updated successfully
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(getApplicationContext(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // Password update failed
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(getApplicationContext(), "Failed to update password", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener( f->{
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(getApplicationContext(), "Failed to update password", Toast.LENGTH_SHORT).show();
                                });
                            }
                        }

                    }
                }).addOnFailureListener( f->{
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Failed to update password", Toast.LENGTH_SHORT).show();
                });
            }
        }
    }

    private boolean isValidEmailId(String email){

        return Pattern.compile("^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$").matcher(email).matches();
    }
}