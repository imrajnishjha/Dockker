package com.example.blockchain;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class StudentProfile extends AppCompatActivity {

    ImageView userImg, logOutImg;
    EditText userName, userDate, userMail, userUniversity, userId, userContact;
    TextView logOutText;
    FirebaseAuth mAuth;
    String userEmail,convertedEmail;
    Uri itemPurl;
    AppCompatButton saveBtn,profileBackBtn;
    DatabaseReference registrationRef= FirebaseDatabase.getInstance().getReference();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_profile);

        mAuth = FirebaseAuth.getInstance();
        userImg = findViewById(R.id.user_profile_image);
        userName = findViewById(R.id.user_profile_contactNumber_edtTxt);
        userDate = findViewById(R.id.user_profile_dateOfBirth_edtTxt);
        userMail = findViewById(R.id.user_profile_email_edtTxt);
        userUniversity = findViewById(R.id.user_profile_company_name_edtTxt);
        userId = findViewById(R.id.user_profile_Id_edtTxt);
        userContact = findViewById(R.id.user_profile_address_edtTxt);
        saveBtn = findViewById(R.id.user_profile_save_button);
        profileBackBtn = findViewById(R.id.userProfile_back_button);
        logOutImg = findViewById(R.id.logout_img);
        logOutText = findViewById(R.id.logout_text);

        userDate.setFocusable(false);
        userDate.setClickable(true);
        userDate.setOnClickListener( v -> showDatePickerDialog());
        profileBackBtn.setOnClickListener(v -> finish());
        logOutText.setOnClickListener(v -> logOut());
        logOutImg.setOnClickListener(v -> logOut());

        if(mAuth.getCurrentUser()!=null){
            userEmail = Objects.requireNonNull(mAuth.getCurrentUser()).getEmail();
        }
        assert userEmail != null;
        convertedEmail = userEmail.replaceAll("\\.", "%7");

        registrationRef.child("user").child(convertedEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Glide.with(getApplicationContext())
                            .load(snapshot.child("purl").getValue().toString())
                            .error(R.drawable.student)
                            .placeholder(R.drawable.student)
                            .into(userImg);
                    userName.setText(snapshot.child("name").getValue().toString());
                    userUniversity.setText(snapshot.child("org").getValue().toString());
                    userMail.setText(snapshot.child("email").getValue().toString());
                    userMail.setEnabled(false);
                    userId.setText(snapshot.child("id").getValue().toString());
                    if(snapshot.child("birth").exists()){
                        userDate.setText(snapshot.child("birth").getValue().toString());
                        userDate.setEnabled(false);
                        userDate.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.grey));
                    }
                    if(snapshot.child("phone").exists()){
                        userContact.setText(snapshot.child("phone").getValue().toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    if (uri != null) {
                        itemPurl = uri;
                        userImg.setImageURI(itemPurl);
                        uploadImage();

                    } else {
                        Log.d("PhotoPicker", "No media selected");
                    }
                });

        userImg.setOnClickListener(v ->{
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        saveBtn.setOnClickListener( v -> uploadProfile());
    }
    private void uploadImage() {
        HashMap<String, Object> newMap = new HashMap<>();

        StorageReference storageRef = FirebaseStorage.getInstance().getReference("User Picture/" + convertedEmail );
        storageRef.putFile(itemPurl).addOnCompleteListener( task -> {
            if(task.isSuccessful()){
                storageRef.getDownloadUrl().addOnSuccessListener( uri ->{
                    newMap.put("purl",uri.toString());

                    registrationRef.child("user").child(convertedEmail).updateChildren(newMap)
                            .addOnSuccessListener(s -> {
                                Toast.makeText(getApplicationContext(), "Profile Picture Updated", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(fail -> {
                                Toast.makeText(getApplicationContext(), "Some Error Occured", Toast.LENGTH_SHORT).show();
                            });

                }).addOnFailureListener( fail ->{
                    Toast.makeText(getApplicationContext(), "Some Error Occured", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void uploadProfile() {
        HashMap<String, Object> newMap = new HashMap<>();

        newMap.put("name",userName.getText().toString());
        newMap.put("birth",userDate.getText().toString());
        newMap.put("org",userUniversity.getText().toString());
        newMap.put("id",userId.getText().toString());
        newMap.put("phone",userContact.getText().toString());

        registrationRef.child("user").child(convertedEmail).updateChildren(newMap).addOnSuccessListener(success ->{
            Toast.makeText(getApplicationContext(), "Profile Data Updated", Toast.LENGTH_SHORT).show();
            userDate.setEnabled(false);
            userDate.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.grey));
        });

    }
    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (DatePicker datePicker, int selectedYear, int selectedMonth, int selectedDayOfMonth) -> {

                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    String formattedDate = sdf.format(selectedDate.getTime());

                    userDate.setText(formattedDate);
                }, year, month, dayOfMonth);

        // Show DatePickerDialog
        datePickerDialog.show();
    }
    private void logOut(){
        mAuth.signOut();
        startActivity(new Intent(getApplicationContext(), Login.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }
}