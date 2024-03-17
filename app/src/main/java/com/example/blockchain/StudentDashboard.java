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

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class StudentDashboard extends AppCompatActivity {

    CircleImageView userImage;
    Uri itemPurl;
    FirebaseAuth mAuth;
    String userEmail,convertedEmail;
    Dialog addItemDialog;
    CardView nocCard,statusCard;
    DatabaseReference registrationRef= FirebaseDatabase.getInstance().getReference("user/");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        userImage = findViewById(R.id.user_img);
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()!=null){
            userEmail = Objects.requireNonNull(mAuth.getCurrentUser()).getEmail();
        }

        nocCard = findViewById(R.id.core_mem);
        statusCard = findViewById(R.id.explore);

        assert userEmail != null;
        convertedEmail = userEmail.replaceAll("\\.", "%7");

        addItemDialog = new Dialog(this);

        registrationRef.child(convertedEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("purl").exists()){
                    Glide.with(getApplicationContext())
                            .load(Objects.requireNonNull(snapshot.child("purl").getValue().toString()))
                            .error(R.drawable.student)
                            .placeholder(R.drawable.student)
                            .into(userImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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

        nocCard.setOnClickListener( v -> startActivity(new Intent(this, NocStudentApply.class)));
        statusCard.setOnClickListener( v -> startActivity(new Intent(this, StudentStatus.class)));
    }

    public void uploadImage() {

        Log.d("uiu", "onCreate:j ");
        HashMap<String, Object> newMap = new HashMap<>();

        StorageReference storageRef = FirebaseStorage.getInstance().getReference("User Picture/" + convertedEmail );
        storageRef.putFile(itemPurl).addOnCompleteListener( task -> {
            if(task.isSuccessful()){
                storageRef.getDownloadUrl().addOnSuccessListener( uri ->{
                    newMap.put("purl",uri.toString());

                    registrationRef.child(convertedEmail).updateChildren(newMap)
                            .addOnSuccessListener(s -> {
                                Toast.makeText(getApplicationContext(), "Item Added", Toast.LENGTH_SHORT).show();
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