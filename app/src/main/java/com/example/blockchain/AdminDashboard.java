package com.example.blockchain;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdminDashboard extends AppCompatActivity {

    CircleImageView userImage;
    Uri itemPurl;
    FirebaseAuth mAuth;
    String userEmail,convertedEmail;
    Dialog addItemDialog;
    CardView nocCard,rejectCard,profileCard;
    TextView nocRejectedStatus, nocApprovedStatus;
    DatabaseReference registrationRef= FirebaseDatabase.getInstance().getReference();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        userImage = findViewById(R.id.user_img);
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()!=null){
            userEmail = Objects.requireNonNull(mAuth.getCurrentUser()).getEmail();
        }

        nocCard = findViewById(R.id.core_mem_admin);
        rejectCard = findViewById(R.id.explore_admin);
        profileCard = findViewById(R.id.member_directory_admin);
        nocRejectedStatus = findViewById(R.id.noc_admin_value);
        nocApprovedStatus = findViewById(R.id.issued_value_admin);

        assert userEmail != null;
        convertedEmail = userEmail.replaceAll("\\.", "%7");

        addItemDialog = new Dialog(this);

        registrationRef.child("user").child(convertedEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("purl").exists()){
                    Glide.with(getApplicationContext())
                            .load(Objects.requireNonNull(snapshot.child("purl").getValue().toString()))
                            .error(R.drawable.student)
                            .placeholder(R.drawable.student)
                            .into(userImage);
                    int deptType = Integer.parseInt(snapshot.child("dept").getValue().toString());
                    Query query = FirebaseDatabase.getInstance().getReference().child("noc");
                    if(deptType == 2){
                        query = query.orderByChild("Status").equalTo(0);
                    } else if(deptType == 3){
                        query = query.orderByChild("Status").equalTo(1);
                    } else if(deptType == 4){
                        query = query.orderByChild("Status").equalTo(2);
                    } else if(deptType == 5){
                        query = query.orderByChild("Status").equalTo(3);
                    } else if(deptType == 6){
                        query = query.orderByChild("Status").equalTo(4);
                    }
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            int pendingCount = 0, rejectedCount = 0;
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                                Object rejectValue = snapshot.child("Reject").getValue();
                                if(rejectValue == null || (rejectValue instanceof Long && (Long) rejectValue == 0)) pendingCount++;
                                else rejectedCount++;
                            }
                            nocApprovedStatus.setText(pendingCount+"");
                            nocRejectedStatus.setText(rejectedCount+"");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Counting of status

        //image picker
        ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    // Callback is invoked after the user selects a media item or closes the
                    // photo picker.
                    if (uri != null) {
                        itemPurl = uri;
                        userImage.setImageURI(itemPurl);
                        uploadImage();

                    } else {
                        Log.d("PhotoPicker", "No media selected");
                    }
                });

        userImage.setOnClickListener(v ->{
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });


        profileCard.setOnClickListener( v -> startActivity(new Intent(this, StudentProfile.class)));
        nocCard.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), AdminNocApprove.class)));
        rejectCard.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), AdminRejectStudent.class)));
        nocApprovedStatus.setOnClickListener(v-> startActivity(new Intent(getApplicationContext(),AdminNocApprove.class)));
        nocRejectedStatus.setOnClickListener(v-> startActivity(new Intent(getApplicationContext(), AdminRejectStudent.class)));
    }

    public void uploadImage() {

        HashMap<String, Object> newMap = new HashMap<>();

        StorageReference storageRef = FirebaseStorage.getInstance().getReference("User Picture/" + convertedEmail );
        storageRef.putFile(itemPurl).addOnCompleteListener( task -> {
            if(task.isSuccessful()){
                storageRef.getDownloadUrl().addOnSuccessListener( uri ->{
                    newMap.put("purl",uri.toString());

                    registrationRef.child("user").child(convertedEmail).updateChildren(newMap)
                            .addOnSuccessListener(s -> {
                                Toast.makeText(getApplicationContext(), "Profile Picture Updated", Toast.LENGTH_SHORT).show();
                                addItemDialog.dismiss();
                            })
                            .addOnFailureListener(fail -> {
                                Toast.makeText(getApplicationContext(), "Some Error Occured", Toast.LENGTH_SHORT).show();
                                addItemDialog.dismiss();
                            });

                }).addOnFailureListener( fail ->{
                    Toast.makeText(getApplicationContext(), "Some Error Occured", Toast.LENGTH_SHORT).show();
                    addItemDialog.dismiss();
                });
            }
        });
    }
}