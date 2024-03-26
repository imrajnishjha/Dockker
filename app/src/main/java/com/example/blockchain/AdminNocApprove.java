package com.example.blockchain;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class AdminNocApprove extends AppCompatActivity {

    AppCompatButton backBtn;
    RecyclerView adminNocRv;
    FirebaseRecyclerOptions<AdminNocModel> options;
    AdminNocAdapter nocAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_noc_approve);

        backBtn = findViewById(R.id.noc_back_btn_admin);
        adminNocRv = findViewById(R.id.noc_Rv_admin);

        WrapContentLinearLayoutManager nocAdminLayout = new WrapContentLinearLayoutManager(getApplicationContext(),LinearLayoutManager.VERTICAL,false);
        adminNocRv.setLayoutManager(nocAdminLayout);
        Query query = FirebaseDatabase.getInstance().getReference().child("noc").orderByChild("Status").equalTo(0);

        options = new FirebaseRecyclerOptions.Builder<AdminNocModel>()
                .setQuery(query, AdminNocModel.class)
                .build();

        nocAdapter = new AdminNocAdapter(options);
        adminNocRv.setAdapter(nocAdapter);
        nocAdapter.startListening();

        backBtn.setOnClickListener( v -> finish());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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