package com.example.blockchain;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class AdminNocApprove extends AppCompatActivity {

    AppCompatButton backBtn;
    RecyclerView adminNocRv;
    FirebaseRecyclerOptions<AdminNocModel> options;
    AdminNocAdapter nocAdapter;
    String userEmail,convertedEmail;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference userTypeRef = FirebaseDatabase.getInstance().getReference();
    Query query;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_noc_approve);

        backBtn = findViewById(R.id.noc_back_btn_admin);
        adminNocRv = findViewById(R.id.noc_Rv_admin);

        WrapContentLinearLayoutManager nocAdminLayout = new WrapContentLinearLayoutManager(getApplicationContext(),LinearLayoutManager.VERTICAL,false);
        adminNocRv.setLayoutManager(nocAdminLayout);

        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()!=null){
            userEmail = Objects.requireNonNull(mAuth.getCurrentUser()).getEmail();
        }
        assert userEmail != null;
        convertedEmail = userEmail.replaceAll("\\.", "%7");

        userTypeRef.child("user").child(convertedEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    int deptType = Integer.parseInt(snapshot.child("dept").getValue().toString());
                    if(deptType == 2){
                        query = FirebaseDatabase.getInstance().getReference().child("noc").orderByChild("Status").equalTo(0);
                    } else if(deptType == 3){
                        query = FirebaseDatabase.getInstance().getReference().child("noc").orderByChild("Status").equalTo(1);
                    } else if(deptType == 4){
                        query = FirebaseDatabase.getInstance().getReference().child("noc").orderByChild("Status").equalTo(2);
                    } else if(deptType == 5){
                        query = FirebaseDatabase.getInstance().getReference().child("noc").orderByChild("Status").equalTo(3);
                    } else if(deptType == 6){
                        query = FirebaseDatabase.getInstance().getReference().child("noc").orderByChild("Status").equalTo(4);
                    }
                    options = new FirebaseRecyclerOptions.Builder<AdminNocModel>()
                            .setQuery(query, AdminNocModel.class)
                            .build();

                    nocAdapter = new AdminNocAdapter(options);
                    adminNocRv.setAdapter(nocAdapter);
                    nocAdapter.startListening();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        backBtn.setOnClickListener( v -> finish());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        nocAdapter.stopListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        nocAdapter.stopListening();
    }

    static class WrapContentLinearLayoutManager extends LinearLayoutManager {
        public WrapContentLinearLayoutManager(Context context) {
            super(context);
        }
        public WrapContentLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context,orientation,reverseLayout);
        }

        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                super.onLayoutChildren(recycler, state);
            } catch (IndexOutOfBoundsException e) {
                Log.e("TAG", "Recycler view error");
            }
        }
    }
}