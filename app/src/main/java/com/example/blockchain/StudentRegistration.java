package com.example.blockchain;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Pattern;

public class StudentRegistration extends AppCompatActivity {
    EditText userName,userEmail,userId,userOrg,userPassword;
    Spinner userType,userDeptType;
    AppCompatButton registrationBackBtn,userRegisterBtn;
    CheckBox registrationCheckBox;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    RelativeLayout progressbar;
    DatabaseReference registrationRef= FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_registration);

        userName = findViewById(R.id.registration_full_name_edtTxt);
        userEmail = findViewById(R.id.registration_email_address_edtTxt);
        userId = findViewById(R.id.registration_phone_number_edtTxt);
        userOrg = findViewById(R.id.registration_address_edtTxt);
        userRegisterBtn = findViewById(R.id.registration_register_btn);
        registrationCheckBox = findViewById(R.id.registration_terms_and_condition_cb);
        registrationBackBtn = findViewById(R.id.registration_back_btn);

        userType = findViewById(R.id.registration_male_female_spinner);
        userDeptType = findViewById(R.id.registration_usertype_spinner);
        userPassword = findViewById(R.id.registration_password_edtTxt);
        progressbar = findViewById(R.id.user_reg_progressBarRL);

        ArrayAdapter<CharSequence> userTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.user_type_array, R.layout.spinner_item);

        ArrayAdapter<CharSequence> userDeptTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.user_dept_type_array, R.layout.spinner_item);

        userTypeAdapter.setDropDownViewResource(R.layout.spinner_dropdowm_item);
        userDeptTypeAdapter.setDropDownViewResource(R.layout.spinner_dropdowm_item);

        userType.setAdapter(userTypeAdapter);
        userDeptType.setAdapter(userDeptTypeAdapter);

        userType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(adapterView.getItemAtPosition(i).toString().equals("Department")){
                    userDeptType.setVisibility(View.VISIBLE);
                }
                else userDeptType.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        registrationBackBtn.setOnClickListener(view -> finish());

        userRegisterBtn.setOnClickListener(view -> sendData());
    }

    private boolean isValidEmailId(String email){

        return Pattern.compile("^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$").matcher(email).matches();
    }

    public void sendData(){
        if(userName.getText().toString().isEmpty()){
            userName.setError("Name can't be empty");
            userName.requestFocus();
        } else if (userType.getSelectedItem().toString().equals("Select User")){
            ((TextView)userType.getSelectedView()).setError("Select the user type");
            userType.requestFocus();
        } else if (userEmail.getText().toString().isEmpty()){
            userEmail.setError("Email can't be empty");
            userEmail.requestFocus();
        } else if(!isValidEmailId(userEmail.getText().toString().trim())){
            userEmail.setError("Please enter valid Email");
            userEmail.requestFocus();
        } else if (userId.getText().toString().isEmpty()){
            userId.setError("Id can't be empty");
            userId.requestFocus();
        } else if (userId.getText().toString().length() != 10){
            userId.setError("Please enter valid Id");
            userId.requestFocus();
        }  else if (userType.getSelectedItem().toString().equals("Department") && userDeptType.getSelectedItem().toString().equals("Select Department")){
            ((TextView)userDeptType.getSelectedView()).setError("Select your Department");
            userDeptType.requestFocus();
        } else if (userOrg.getText().toString().isEmpty()){
            userOrg.setError("Org can't be empty");
            userOrg.requestFocus();
        } else if (userPassword.getText().toString().isEmpty()){
            userPassword.setError("Password can't be empty");
            userPassword.requestFocus();
        } else if (userPassword.getText().toString().length()<6){
            userPassword.setError("Password should be of 6 Digits");
            userPassword.requestFocus();
        }else if (!registrationCheckBox.isChecked()){
            Toast.makeText(StudentRegistration.this, "Please agree to terms and condition", Toast.LENGTH_SHORT).show();
            registrationCheckBox.requestFocus();
        } else {
            progressbar.setVisibility(View.VISIBLE);
            HashMap<String,Object> newUserMap = new HashMap<>();
            newUserMap.put("name",userName.getText().toString());
            newUserMap.put("email",userEmail.getText().toString());
            newUserMap.put("org",userOrg.getText().toString());
            newUserMap.put("id",userId.getText().toString());
            newUserMap.put("purl","");

            String sp = userType.getSelectedItem().toString();
            if(sp.equals("Student")) newUserMap.put("type",0);
            else if(sp.equals("Department")) newUserMap.put("type",1);

            if(userType.getSelectedItem().toString().equals("Department")){
                sp = userDeptType.getSelectedItem().toString();
                if(sp.equals("Central Library")) newUserMap.put("dept",0);
                else if(sp.equals("Hostel")) newUserMap.put("dept",1);
                else if(sp.equals("Project Guide")) newUserMap.put("dept",2);
                else if(sp.equals("Project Co-ordinator")) newUserMap.put("dept",3);
                else if(sp.equals("Placement Cell")) newUserMap.put("dept",4);
                else if(sp.equals("Finance")) newUserMap.put("dept",5);
                else if(sp.equals("Admin")) newUserMap.put("dept",6);

                String deptTypeText = (userDeptType.getSelectedItem().toString().equals("Central Library"))?"library" :
                                            (userDeptType.getSelectedItem().toString().equals("Hostel"))  ? "hostel" :
                                            (userDeptType.getSelectedItem().toString().equals("Project Guide"))  ? "guide" :
                                            (userDeptType.getSelectedItem().toString().equals("Project Co-ordinator"))  ? "projectcod" :
                                            (userDeptType.getSelectedItem().toString().equals("Placement Cell"))  ? "placementcell" :
                                            (userDeptType.getSelectedItem().toString().equals("Admin"))  ? "admin" :
                                            (userDeptType.getSelectedItem().toString().equals("Finance"))  ? "finance" : "";

                registrationRef.child("Department").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.child(deptTypeText).exists()){
                            if(snapshot.child(deptTypeText).child(userId.getText().toString()).exists()){
                                String mail = Objects.requireNonNull(snapshot.child(deptTypeText).child(userId.getText().toString()).getValue()).toString();
                                if(mail.equals(userEmail.getText().toString())){
                                    addUser(newUserMap, progressbar);
                                }
                                else{
                                    Toast.makeText(StudentRegistration.this, "Enter the mail provided by Org", Toast.LENGTH_SHORT).show();
                                    progressbar.setVisibility(View.GONE);
                                }
                            }
                            else {
                                Toast.makeText(StudentRegistration.this, "You doesn't belong to this Department", Toast.LENGTH_SHORT).show();
                                progressbar.setVisibility(View.GONE);
                            }
                        }
                        else{
                            Toast.makeText(StudentRegistration.this, "Your Organisation Does not have this Department", Toast.LENGTH_SHORT).show();
                            progressbar.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
            else {
                registrationRef.child("student").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d("uiu", "onDataChange: "+userId.getText().toString());
                        if(snapshot.child(userId.getText().toString()).exists()){
                            String mail = Objects.requireNonNull(snapshot.child(userId.getText().toString()).getValue()).toString();
                            if(mail.equals(userEmail.getText().toString())){
                                addUser(newUserMap, progressbar);
                            }
                            else{
                                Toast.makeText(StudentRegistration.this, "Enter the mail provided by Org", Toast.LENGTH_SHORT).show();
                                progressbar.setVisibility(View.GONE);
                            }
                        }
                        else {
                            Toast.makeText(StudentRegistration.this, "You doesn't belong to this Org", Toast.LENGTH_SHORT).show();
                            progressbar.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

        }
    }
    public void addUser(HashMap<String, Object> newUserMap, RelativeLayout progressbar){
        registrationRef.child("user").child(userEmail.getText().toString().replaceAll("\\.", "%7"))
                .updateChildren(newUserMap)
                .addOnSuccessListener(s -> {
                    mAuth.createUserWithEmailAndPassword(userEmail.getText().toString(),userPassword.getText().toString())
                            .addOnCompleteListener(comp->{
                                if(comp.isSuccessful()){
                                    mAuth.signOut();
                                    Toast.makeText(StudentRegistration.this,"You are Registered",Toast.LENGTH_SHORT).show();
                                    progressbar.setVisibility(View.GONE);
                                    startActivity(new Intent(StudentRegistration.this,Login.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                } else {
                                    Toast.makeText(StudentRegistration.this,"Please try again",Toast.LENGTH_SHORT).show();
                                    progressbar.setVisibility(View.GONE);
                                }
                            }).addOnFailureListener( f->{
                                Toast.makeText(StudentRegistration.this, "Please try again", Toast.LENGTH_SHORT).show();
                                progressbar.setVisibility(View.GONE);
                            });
                })
                .addOnFailureListener(fail -> {
                    Toast.makeText(StudentRegistration.this, "Please try again", Toast.LENGTH_SHORT).show();
                    progressbar.setVisibility(View.GONE);
                });
    }

}