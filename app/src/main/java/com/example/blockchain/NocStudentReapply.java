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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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

public class NocStudentReapply extends AppCompatActivity {

    CardView offerCard, idProofCard, declarationCard;
    TextView offerText, idText, declarationText;
    ImageView offerCheck, idCheck, declarationCheck;
    Uri pdfOfferUri, pdfIdUri, pdfdeclUri;
    AppCompatButton nocBackBtn, applyBtn;
    EditText companyName, companyAddress, companyPhone;
    Spinner nocTypeSpinner;
    private StorageReference storageReference;
    FirebaseAuth mAuth;
    String userEmail, convertedEmail,adminType;
    String offerStr,idStr,decStr;
    DatabaseReference nocRef= FirebaseDatabase.getInstance().getReference("noc/");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noc_student_reapply);

        offerCard = findViewById(R.id.offerLetterCard_reapply);
        idProofCard = findViewById(R.id.idProofCard_reapply);
        declarationCard = findViewById(R.id.declarationFormCard_reapply);
        offerText = findViewById(R.id.offerText_reapply);
        idText = findViewById(R.id.idProofText_reapply);
        declarationText = findViewById(R.id.declarationText_reapply);
        offerCheck = findViewById(R.id.offer_check_reapply);
        idCheck = findViewById(R.id.id_check_reapply);
        declarationCheck = findViewById(R.id.declaration_check_reapply);
        nocTypeSpinner = findViewById(R.id.noc_type_spinner_reapply);
        nocBackBtn = findViewById(R.id.noc_back_btn_reapply);
        companyName = findViewById(R.id.noc_company_name_edtTxt_reapply);
        companyAddress = findViewById(R.id.noc_company_address_edtTxt_reapply);
        companyPhone = findViewById(R.id.company_phone_number_edtTxt_reapply);
        applyBtn = findViewById(R.id.noc_reapply_register_btn);
        adminType = getIntent().getStringExtra("adminType");

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


        nocRef.child(convertedEmail).child("Detail").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    companyName.setText(snapshot.child("CompanyName").getValue().toString());
                    companyAddress.setText(snapshot.child("CompanyAddress").getValue().toString());
                    companyPhone.setText(snapshot.child("CompanyPhone").getValue().toString());
                    String offer = snapshot.child("OfferType").getValue().toString();
                    int position = getPositionOfValue(nocTypeSpinner, offer);
                    nocTypeSpinner.setSelection(position==-1?0:position);
                    offerStr = snapshot.child("offerLetter").getValue().toString();
                    idStr = snapshot.child("idProof").getValue().toString();
                    decStr = snapshot.child("selfDeclaration").getValue().toString();
                    setFileName(offerText,offerCheck);
                    setFileName(idText,idCheck);
                    setFileName(declarationText,declarationCheck);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



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
        applyBtn.setOnClickListener(v -> ReApplyNoc(adminType,nocTypeSpinner,companyName,companyAddress,companyPhone,pdfOfferUri,pdfIdUri,pdfdeclUri,convertedEmail));

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
    private int getPositionOfValue(Spinner spinner, String value) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        return adapter.getPosition(value);
    }
    private void setFileName(TextView tv, ImageView iv ) {
        Glide.with(getApplicationContext())
                .load(R.drawable.check)
                .error(R.drawable.check)
                .placeholder(R.drawable.check)
                .into(iv);
        tv.setText("File Uploaded");
    }

    private void ReApplyNoc(String adminType,Spinner type, EditText cName, EditText cAddress, EditText cPhone, Uri offer, Uri iD, Uri declaration, String email){
        HashMap<String, Object> Detail = new HashMap<>();
        HashMap<String, Object> Status = new HashMap<>();
        Status.put(adminType,1);

        StorageReference fileReference = storageReference.child("noc").child(email);

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
        }
        if (offer != null){
            fileReference.child("offer").putFile(offer).addOnSuccessListener(success ->{
                fileReference.child("offer").getDownloadUrl().addOnSuccessListener(uri -> {
                    Detail.put("offerLetter",uri.toString());
                });
            }).addOnFailureListener( f->{
                Toast.makeText(getApplicationContext(), "Some Error Occurred", Toast.LENGTH_SHORT).show();
            });
        }
        else{
            Detail.put("offerLetter",offerStr);
        }
        if (iD != null){
            fileReference.child("Id").putFile(iD).addOnSuccessListener( success2 ->{
                fileReference.child("Id").getDownloadUrl().addOnSuccessListener(uri2 -> {
                    Detail.put("idProof",uri2.toString());
                });
            }).addOnFailureListener( f->{
                Toast.makeText(getApplicationContext(), "Some Error Occurred", Toast.LENGTH_SHORT).show();
            });
        }else{
            Detail.put("idProof",idStr);
        }
        if (declaration != null){
            fileReference.child("Declaration").putFile(declaration).addOnSuccessListener(s ->{
                fileReference.child("Declaration").getDownloadUrl().addOnSuccessListener(uri3 -> {
                    Detail.put("selfDeclaration",uri3.toString());
                });
            }).addOnFailureListener( f->{
                Toast.makeText(getApplicationContext(), "Some Error Occurred", Toast.LENGTH_SHORT).show();
            });
        }else {
            Detail.put("selfDeclaration",decStr);
        }
        Detail.put("OfferType",type.getSelectedItem().toString());
        Detail.put("CompanyName",cName.getText().toString());
        Detail.put("CompanyAddress",cAddress.getText().toString());
        Detail.put("CompanyPhone",cPhone.getText().toString());
        Status.put("Detail",Detail);
        DatabaseReference nocRef= FirebaseDatabase.getInstance().getReference("noc/");
        nocRef.child(email).updateChildren(Status).addOnSuccessListener( save ->{
            Toast.makeText(getApplicationContext(), "NOC Applied", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), StudentDashboard.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        }).addOnFailureListener( f->{
            Toast.makeText(getApplicationContext(), "Some Error Occurred", Toast.LENGTH_SHORT).show();
        });

    }

}