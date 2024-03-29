package com.example.blockchain;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.HashMap;
import java.util.Objects;

public class NocStudentApply extends AppCompatActivity {

    CardView offerCard, idProofCard, declarationCard;
    TextView offerText, idText, declarationText;
    ImageView offerCheck, idCheck, declarationCheck;
    Uri pdfOfferUri, pdfIdUri, pdfdeclUri;
    AppCompatButton nocBackBtn, applyBtn;
    EditText companyName, companyAddress, companyPhone;
    Spinner nocTypeSpinner;
    private StorageReference storageReference;
    FirebaseAuth mAuth;
    String userEmail, convertedEmail;
    RelativeLayout progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noc_student_apply);

        offerCard = findViewById(R.id.offerLetterCard);
        idProofCard = findViewById(R.id.idProofCard);
        declarationCard = findViewById(R.id.declarationFormCard);
        offerText = findViewById(R.id.offerText);
        idText = findViewById(R.id.idProofText);
        declarationText = findViewById(R.id.declarationText);
        offerCheck = findViewById(R.id.offer_check);
        idCheck = findViewById(R.id.id_check);
        declarationCheck = findViewById(R.id.declaration_check);
        nocTypeSpinner = findViewById(R.id.noc_type_spinner);
        nocBackBtn = findViewById(R.id.noc_back_btn);
        companyName = findViewById(R.id.noc_company_name_edtTxt);
        companyAddress = findViewById(R.id.noc_company_address_edtTxt);
        companyPhone = findViewById(R.id.company_phone_number_edtTxt);
        applyBtn = findViewById(R.id.noc_register_btn);
        progressBar = findViewById(R.id.user_noc_apply_progressBarRL);

        storageReference = FirebaseStorage.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()!=null){
            userEmail = Objects.requireNonNull(mAuth.getCurrentUser()).getEmail();
        }
        assert userEmail != null;
        convertedEmail = userEmail.replaceAll("\\.", "%7");

        ArrayAdapter<CharSequence> nocTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.noc_type_array, R.layout.spinner_item);
        nocTypeAdapter.setDropDownViewResource(R.layout.spinner_dropdowm_item);
        nocTypeSpinner.setAdapter(nocTypeAdapter);


        ActivityResultLauncher<String> pickOfferPdfLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
            result -> {
                if (result != null) {
                    pdfOfferUri = result;
                    String name = getFileNameFromUri(pdfOfferUri);

                    if(name.length()>10) offerText.setText(name.substring(0,10)+"... Uploaded");
                    else offerText.setText(name+" Uploaded");
                    Glide.with(getApplicationContext())
                            .load(R.drawable.check)
                            .error(R.drawable.check)
                            .placeholder(R.drawable.check)
                            .into(offerCheck);
                }
        });
        ActivityResultLauncher<String> pickIdPdfLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                result -> {
                    if (result != null) {
                        pdfIdUri = result;
                        String name = getFileNameFromUri(pdfIdUri);

                        if(name.length()>10) idText.setText(name.substring(0,10)+"... Uploaded");
                        else offerText.setText(name+" Uploaded");
                        Glide.with(getApplicationContext())
                                .load(R.drawable.check)
                                .error(R.drawable.check)
                                .placeholder(R.drawable.check)
                                .into(idCheck);
            }
        });
        ActivityResultLauncher<String> pickDeclPdfLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                result -> {
                    if (result != null) {
                        pdfdeclUri = result;
                        String name = getFileNameFromUri(pdfdeclUri);

                        if(name.length()>10) declarationText.setText(name.substring(0,10)+"... Uploaded");
                        else offerText.setText(name+" Uploaded");
                        Glide.with(getApplicationContext())
                                .load(R.drawable.check)
                                .error(R.drawable.check)
                                .placeholder(R.drawable.check)
                                .into(declarationCheck);
                    }
                });
        offerCard.setOnClickListener(view -> pickOfferPdfLauncher.launch("application/pdf"));
        idProofCard.setOnClickListener(view -> pickIdPdfLauncher.launch("application/pdf"));
        declarationCard.setOnClickListener(view -> pickDeclPdfLauncher.launch("application/pdf"));

        nocBackBtn.setOnClickListener(v -> finish());
        applyBtn.setOnClickListener(v -> ApplyNoc(nocTypeSpinner,companyName,companyAddress,companyPhone,pdfOfferUri,pdfIdUri,pdfdeclUri,convertedEmail,progressBar));
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

    private void ApplyNoc(Spinner type, EditText cName, EditText cAddress, EditText cPhone, Uri offer, Uri iD, Uri declaration, String email,RelativeLayout progressBar){
        HashMap<String, Object> Detail = new HashMap<>();
        HashMap<String, Object> Status = new HashMap<>();
        Status.put("guide",1);
        Status.put("projectcod",1);
        Status.put("placement",1);
        Status.put("finance",1);
        Status.put("admin",1);
        Status.put("Status",0);

        if (type.getSelectedItem().toString().equals("Select Offer type")){
            ((TextView)type.getSelectedView()).setError("Select Offer Type");
            type.requestFocus();
        } else if(cName.getText().toString().isEmpty()){
            cName.setError("Name can't be empty");
            cName.requestFocus();
        } else if(cAddress.getText().toString().isEmpty()){
            cName.setError("Address can't be empty");
            cName.requestFocus();
        } else if (cPhone.getText().toString().isEmpty()){
            cPhone.setError("PhoneNo. can't be empty");
            cPhone.requestFocus();
        } else if (offer == null){
            Toast.makeText(getApplicationContext(), "Upload Offer Letter", Toast.LENGTH_SHORT).show();
        } else if (iD == null){
            Toast.makeText(getApplicationContext(), "Upload Id Proof", Toast.LENGTH_SHORT).show();
        } else if (declaration == null){
            Toast.makeText(getApplicationContext(), "Upload Self Declaration", Toast.LENGTH_SHORT).show();
        } else {
            progressBar.setVisibility(View.VISIBLE);
            StorageReference fileReference = storageReference.child("noc").child(email);
            fileReference.child("offer").putFile(offer).addOnSuccessListener(success ->{
                fileReference.child("offer").getDownloadUrl().addOnSuccessListener(uri -> {
                    Detail.put("offerLetter",uri.toString());
                });
                fileReference.child("Id").putFile(iD).addOnSuccessListener( success2 ->{
                    fileReference.child("Id").getDownloadUrl().addOnSuccessListener(uri2 -> {
                        Detail.put("idProof",uri2.toString());
                    });
                    fileReference.child("Declaration").putFile(declaration).addOnSuccessListener(s ->{
                        fileReference.child("Declaration").getDownloadUrl().addOnSuccessListener(uri3 -> {
                            Log.d("oio", uri3.toString());
                            Detail.put("OfferType",type.getSelectedItem().toString());
                            Detail.put("CompanyName",cName.getText().toString());
                            Detail.put("CompanyAddress",cAddress.getText().toString());
                            Detail.put("CompanyPhone",cPhone.getText().toString());
                            Detail.put("selfDeclaration",uri3.toString());
                            Status.put("Detail",Detail);

                            DatabaseReference nocRef= FirebaseDatabase.getInstance().getReference("noc/");
                            nocRef.child(email).updateChildren(Status).addOnSuccessListener( save ->{
                                Toast.makeText(getApplicationContext(), "NOC Applied", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(),StudentDashboard.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                progressBar.setVisibility(View.GONE);
                            }).addOnFailureListener( f->{
                                Toast.makeText(getApplicationContext(), "Some Error Occurred", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                            });

                        }).addOnFailureListener( f->{
                            Toast.makeText(getApplicationContext(), "Some Error Occurred", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        });

                    }).addOnFailureListener( f->{
                        Toast.makeText(getApplicationContext(), "Some Error Occurred", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    });
                }).addOnFailureListener( f->{
                    Toast.makeText(getApplicationContext(), "Some Error Occurred", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
            }).addOnFailureListener( f->{
                Toast.makeText(getApplicationContext(), "Some Error Occurred", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            });
        }
    }
}