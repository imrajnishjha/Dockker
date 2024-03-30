package com.example.blockchain;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminNocRejectedDetail extends AppCompatActivity {
    ImageView offerCheck, idCheck, declarationCheck;
    String pdfOfferUri, pdfIdUri, pdfdeclUri;
    String studentKey;
    AppCompatButton nocBackBtn, rejectBtn;
    TextView companyName, companyAddress, companyPhone, nocTypeSpinner;
    private final DatabaseReference nocRef = FirebaseDatabase.getInstance().getReference("noc");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_noc_rejected_detail);

        offerCheck = findViewById(R.id.offer_reject_check_admin);
        idCheck = findViewById(R.id.id_reject_check_admin);
        declarationCheck = findViewById(R.id.declaration_reject_check_admin);
        nocTypeSpinner = findViewById(R.id.noc_type_reject_spinner_admin);
        nocBackBtn = findViewById(R.id.noc_reject_back_btn_admin);
        companyName = findViewById(R.id.noc_company_name_reject_edtTxt_admin);
        companyAddress = findViewById(R.id.noc_company_address_reject_edtTxt_admin);
        companyPhone = findViewById(R.id.company_phone_number_reject_edtTxt_admin);
        nocBackBtn = findViewById(R.id.noc_reject_back_btn_admin);
        rejectBtn = findViewById(R.id.noc_reject_remark_btn_admin);

        studentKey = getIntent().getStringExtra("key");

        nocRef.child(studentKey).child("Detail").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    companyPhone.setText(snapshot.child("CompanyPhone").getValue().toString());
                    companyAddress.setText(snapshot.child("CompanyAddress").getValue().toString());
                    companyName.setText(snapshot.child("CompanyName").getValue().toString());
                    nocTypeSpinner.setText(snapshot.child("OfferType").getValue().toString());
                    pdfIdUri = snapshot.child("idProof").getValue().toString();
                    pdfOfferUri = snapshot.child("offerLetter").getValue().toString();
                    pdfdeclUri = snapshot.child("selfDeclaration").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        offerCheck.setOnClickListener( v -> downloadPdf(pdfOfferUri));
        idCheck.setOnClickListener( v -> downloadPdf(pdfIdUri));
        declarationCheck.setOnClickListener( v -> downloadPdf(pdfdeclUri));
        nocBackBtn.setOnClickListener( v -> finish());

        rejectBtn.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), AdminRejectNocRemark.class).putExtra("key",studentKey).putExtra("type","1"));
        });
    }
    private void downloadPdf(String uri){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(uri), "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Intent newIntent = Intent.createChooser(intent, "Open File");
        try {
            startActivity(newIntent);
        } catch (ActivityNotFoundException e) {
        }
    }
}