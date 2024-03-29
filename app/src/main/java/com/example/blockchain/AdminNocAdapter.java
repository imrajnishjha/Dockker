package com.example.blockchain;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class AdminNocAdapter extends FirebaseRecyclerAdapter<AdminNocModel, AdminNocAdapter.NovViewModel>{

    public AdminNocAdapter(@NonNull FirebaseRecyclerOptions<AdminNocModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull NovViewModel holder, int position, @NonNull AdminNocModel model) {

        if(model.getReject() == 0 ){
            holder.studentId.setText(String.valueOf(model.getStatus()));
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("user");
            userRef.child(getRef(position).getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        Glide.with(holder.studentImg.getContext())
                                .load(snapshot.child("purl").getValue().toString())
                                .error(R.drawable.student)
                                .into(holder.studentImg);
                        holder.studentName.setText(snapshot.child("name").getValue().toString());
                        holder.studentId.setText(snapshot.child("id").getValue().toString());
                        holder.mainView.setOnClickListener(v ->{
                            v.getContext().startActivity(new Intent(v.getContext(),AdminNocDetail.class).putExtra("key",getRef(holder.getAbsoluteAdapterPosition()).getKey()));
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        else {
            holder.mainView.setVisibility(View.GONE);
        }

    }

    @NonNull
    @Override
    public NovViewModel onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_noc_item,parent,false);
        return new NovViewModel(view);
    }

    public static class NovViewModel extends RecyclerView.ViewHolder{
        TextView studentName, studentId;
        ImageView studentImg;
        View mainView;
        public NovViewModel(@NonNull View itemView) {
            super(itemView);
            studentId = (TextView) itemView.findViewById(R.id.student_id);
            studentName = (TextView) itemView.findViewById(R.id.studentName);
            studentImg = (ImageView)  itemView.findViewById(R.id.student_img);
            mainView = itemView;
        }
    }
}
