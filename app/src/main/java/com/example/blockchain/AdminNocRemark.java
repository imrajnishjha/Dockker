package com.example.blockchain;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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

import java.util.HashMap;
import java.util.Objects;

public class AdminNocRemark extends AppCompatActivity {

    EditText remarkEdt;
    ImageView uploadBtn, previewIv;
    TextView picNameText;
    AppCompatButton adminRemarkBackBtn;
    String studentKey, type, isDelete = "0";
    Uri itemPurl;
    AppCompatButton approveBtn;
    String adminType,adminName,adminMail,adminMailConverted = "";
    int Status;
    String userEmail,convertedEmail;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference userTypeRef = FirebaseDatabase.getInstance().getReference();
    RelativeLayout progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noc_admin_remark);

        remarkEdt = findViewById(R.id.noc_company_name_edtTxt_admin);
        uploadBtn = findViewById(R.id.noc_remark_admin_upload);
        previewIv = findViewById(R.id.remark_admin_picture_preview);
        picNameText = findViewById(R.id.remark_admin_picture_name);
        adminRemarkBackBtn = findViewById(R.id.noc_remark_back_btn_admin);
        approveBtn = findViewById(R.id.noc_approve_remark_btn_admin);
        progressBar = findViewById(R.id.user_admin_noc_remark_progressBarRL);

        adminRemarkBackBtn.setOnClickListener( v -> finish());

        studentKey = getIntent().getStringExtra("key");
        type = getIntent().getStringExtra("type");
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()!=null){
            userEmail = Objects.requireNonNull(mAuth.getCurrentUser()).getEmail();
        }
        assert userEmail != null;
        convertedEmail = userEmail.replaceAll("\\.", "%7");

        if(type.equals("1")){
            approveBtn.setText("Reject");
            approveBtn.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.light_red_btn));
        }

        userTypeRef.child("user").child(convertedEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    int deptType = Integer.parseInt(snapshot.child("dept").getValue().toString());
                    adminName = snapshot.child("name").getValue().toString();
                    adminMail = snapshot.getKey().toString();
                    adminMailConverted = adminMail.replaceAll("%7","\\.");
                    if(deptType == 2){
                        adminType = "guide";
                        Status = 1;
                    } else if(deptType == 3){
                        adminType = "projectcod";
                        Status = 2;
                    } else if(deptType == 4){
                        adminType = "placement";
                        Status = 3;
                    } else if(deptType == 5){
                        adminType = "finance";
                        Status = 4;
                    } else if(deptType == 6){
                        adminType = "admin";
                        Status = 5;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    // Callback is invoked after the user selects a media item or closes the
                    // photo picker.
                    if (uri != null) {
                        itemPurl = uri;
                        String name = getFileNameFromUri(itemPurl);

                        if(name.length()>10) picNameText.setText(name.substring(0,10)+"... Uploaded");
                        else picNameText.setText(name+" Uploaded");
                        Glide.with(getApplicationContext())
                                .load(R.drawable.cancel)
                                .error(R.drawable.check)
                                .placeholder(R.drawable.check)
                                .into(uploadBtn);
                        Glide.with(getApplicationContext())
                                .load(itemPurl)
                                .error(R.drawable.check)
                                .placeholder(R.drawable.check)
                                .into(previewIv);
                        isDelete = "1";

                    } else {
                        Log.d("PhotoPicker", "No media selected");
                    }
                });

        uploadBtn.setOnClickListener(v ->{
            if(isDelete.equals("0")){
                pickMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            }
            else {
                isDelete = "0";
                itemPurl = null;
                picNameText.setText("Upload File");
                Glide.with(getApplicationContext())
                        .load(R.drawable.arrow)
                        .error(R.drawable.check)
                        .placeholder(R.drawable.check)
                        .into(uploadBtn);
                Glide.with(getApplicationContext())
                        .load(R.drawable.cv)
                        .error(R.drawable.check)
                        .placeholder(R.drawable.check)
                        .into(previewIv);

            }

        });

        //1 to reject 0 to approve
        approveBtn.setOnClickListener( v ->{
            if(type.equals("1")){
                uploadRemark(1,adminType,itemPurl,remarkEdt.getText().toString(),adminName,progressBar,adminMailConverted);
            } else if(type.equals("0")){
                uploadRemark(0,adminType,itemPurl,remarkEdt.getText().toString(),adminName,progressBar,adminMailConverted);
            }
        });

    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            cursor.close();
        }
        return fileName;
    }
    private void uploadRemark(int appRej,String type, Uri imgUri, String remark, String name, RelativeLayout progressBar,String Mail){
        progressBar.setVisibility(View.VISIBLE);
        DatabaseReference nocRef = FirebaseDatabase.getInstance().getReference("noc").child(studentKey);
        HashMap<String,Object> remarkMap = new HashMap<>();
        HashMap<String,Object> nocStatusMap = new HashMap<>();
        if(appRej == 0){
            nocStatusMap.put(type,2);
            nocStatusMap.put("Status",Status);
            nocStatusMap.put("Reject",0);
        }
        else {
            nocStatusMap.put(type,0);
            nocStatusMap.put("Reject",1);
        }

        if(imgUri == null){
            remarkMap.put("name",name);
            remarkMap.put("remark",remark);
            remarkMap.put("purl","");
            remarkMap.put("email",Mail);
            nocRef.updateChildren(nocStatusMap).addOnSuccessListener(s ->{
                nocRef.child("Remark").child(type).updateChildren(remarkMap);
                Toast.makeText(this, appRej == 0 ?"NOC Approved":"NOC Rejected", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                startActivity(new Intent(this,AdminDashboard.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
            }).addOnFailureListener( f->{
                Toast.makeText(this, "Please Try Again", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            });
        }
        else {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference("NocRemark/" + studentKey );
            storageRef.putFile(imgUri).addOnCompleteListener(task -> {
               if(task.isSuccessful()){
                   storageRef.getDownloadUrl().addOnSuccessListener(uri ->{
                       remarkMap.put("name",name);
                       remarkMap.put("remark",remark);
                       remarkMap.put("purl",uri.toString());
                       remarkMap.put("email",Mail);
                       nocRef.updateChildren(nocStatusMap).addOnSuccessListener( s->{
                           nocRef.child("Remark").child(type).updateChildren(remarkMap);
                           Toast.makeText(this, appRej == 0 ?"NOC Approved":"NOC Rejected", Toast.LENGTH_SHORT).show();
                           progressBar.setVisibility(View.GONE);
                           startActivity(new Intent(this,AdminDashboard.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
                       }).addOnFailureListener(f->{
                           Toast.makeText(this, "Please Try Again", Toast.LENGTH_SHORT).show();
                           progressBar.setVisibility(View.GONE);
                       });
                   }).addOnFailureListener(f->{
                       Toast.makeText(this, "Please Try Again", Toast.LENGTH_SHORT).show();
                       progressBar.setVisibility(View.GONE);
                   });
               }
               else {
                   Toast.makeText(this, "Please Try Again", Toast.LENGTH_SHORT).show();
                   progressBar.setVisibility(View.GONE);
               }
            }).addOnFailureListener(f->{
                Toast.makeText(this, "Please Try Again", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            });
        }
    }
}