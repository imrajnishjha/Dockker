package com.example.blockchain;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class NocStatusDescription extends AppCompatActivity {

    TextView verifiedBy, remark;
    ImageView imgDescription;
    AppCompatButton reApplyBtn,nocDescBackBtn;
    private DatabaseReference nocRef = FirebaseDatabase.getInstance().getReference("noc");
    private FirebaseAuth mAuth;
    String userEmail, convertedEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noc_status_description);

        verifiedBy = findViewById(R.id.noc_verified_by);
        remark = findViewById(R.id.noc_remark_id);
        imgDescription = findViewById(R.id.noc_image);
        reApplyBtn = findViewById(R.id.noc_reapply_btn);
        nocDescBackBtn = findViewById(R.id.noc_remark_back_btn);

        int type = getIntent().getIntExtra("type",-1);
        int statusDesc = getIntent().getIntExtra("status",2);

        if(statusDesc == 2 || statusDesc == 1){
            reApplyBtn.setVisibility(View.GONE);
        }
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()!=null){
            userEmail = Objects.requireNonNull(mAuth.getCurrentUser()).getEmail();
        }
        assert userEmail != null;
        convertedEmail = userEmail.replaceAll("\\.", "%7");

        nocRef.child(convertedEmail).child("Remark").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(type == 0) infoSetter("guide",verifiedBy,remark,imgDescription,snapshot);
                    if(type == 1) infoSetter("projectcod",verifiedBy,remark,imgDescription,snapshot);
                    if(type == 2) infoSetter("placement",verifiedBy,remark,imgDescription,snapshot);
                    if(type == 3) infoSetter("finance",verifiedBy,remark,imgDescription,snapshot);
                    if(type == 4) infoSetter("admin",verifiedBy,remark,imgDescription,snapshot);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        nocDescBackBtn.setOnClickListener(v -> finish());
        reApplyBtn.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), NocStudentReapply.class)
                    .putExtra("adminType", type == 0?"guide":type == 1?"projectcod":type == 2?"placement":type == 3?"finance":type == 4?"admin":""));
        });
    }

    private void infoSetter(String type,TextView nameTv, TextView remarkTv, ImageView imgDescIv, DataSnapshot snapshot){
        if(snapshot.child(type).exists()){
            nameTv.setText(snapshot.child(type).child("name").getValue().toString()+" ("+snapshot.child(type).child("email").getValue().toString()+")");
            remarkTv.setText(snapshot.child(type).child("remark").getValue().toString());
            Glide.with(getApplicationContext())
                    .load(snapshot.child(type).child("purl").getValue().toString())
                    .error(R.drawable.cancel)
                    .placeholder(R.drawable.cancel)
                    .into(imgDescIv);

        }
    }
}