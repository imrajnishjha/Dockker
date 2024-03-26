package com.example.blockchain;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class AdminNocRemark extends AppCompatActivity {

    EditText remarkEdt;
    ImageView uploadBtn, previewIv;
    TextView picNameText;
    AppCompatButton adminRemarkBackBtn;
    String studentKey, type, isDelete = "0";
    Uri itemPurl;
    AppCompatButton approveBtn;

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

        adminRemarkBackBtn.setOnClickListener( v -> finish());

        studentKey = getIntent().getStringExtra("key");
        type = getIntent().getStringExtra("type");
        Log.d("oio", "onCreate: "+type+studentKey);

        if(type.equals("1")){
            approveBtn.setText("Reject");
            approveBtn.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.light_red_btn));
        }

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
    private void uploadRemark(String type, Uri imgUri, String remark){

    }
}