package com.example.dev20_2;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.example.dev20_2.model.Notification;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SendLocationActivity extends AppCompatActivity {
    private Spinner accidentList;
    private Button btnSubmit;
    private ImageButton imgBtn;
    private int type;
    private String description;
    private long lat, lng;
    private DatabaseReference mDatabase;


    private StorageReference mStorageRef;
    private static final int TAKE_PICTURE = 1;
    private Uri imageUri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_location);
        String id = createID();
        Context context = this;

        accidentList = findViewById(R.id.accidents_list);
        String[] accidentListTxt = {"Traffic Jam", "Accident", "Blockage"};
        int[] accidenListValue = {1,2,3};
        SpinnerAdapter spinnerAdapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, accidentListTxt);
        accidentList.setAdapter(spinnerAdapter);
        accidentList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                type = accidenListValue[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        description = findViewById(R.id.editText_description).toString();
        lat = getIntent().getExtras().getLong("lat");
        lng = getIntent().getExtras().getLong("lng");

        imgBtn = (ImageButton) findViewById(R.id.imgBtn);
        btnSubmit = (Button) findViewById(R.id.btn_submit);
        imgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto(id, context);
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StorageReference imgRef = mStorageRef.child("images/" + imageUri.getLastPathSegment());
                Notification notification = new Notification();
                notification.setLat(lat);
                notification.setLng(lng);
                notification.setDescription(description);
                notification.setType(type);
                notification.setImage(uploadFile(imageUri, imgRef).toString());
                notification.setId(id);

                mDatabase = FirebaseDatabase.getInstance().getReference().child("notis");
                mDatabase.child(id).push().setValue(notification);
            }
        });
    }

    public void takePhoto(String id, Context context) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo = new File(Environment.getExternalStorageDirectory(), "Dev20Pic-" + id + " .jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                FileProvider.getUriForFile(context, context.getPackageName() + ".provider", photo));
        imageUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", photo);
        startActivityForResult(intent, TAKE_PICTURE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PICTURE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri selectedImage = imageUri;
                    getContentResolver().notifyChange(selectedImage, null);
                    ImageView imageView = (ImageView) findViewById(R.id.imgReview);
                    ContentResolver cr = getContentResolver();
                    Bitmap bitmap;
                    try {
                        bitmap = android.provider.MediaStore.Images.Media
                                .getBitmap(cr, selectedImage);
                        imageView.setImageBitmap(bitmap);
                        Toast.makeText(this, selectedImage.toString(),
                                Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT)
                                .show();
                        Log.e("Camera", e.toString());
                    }
                }
        }
    }

    public Uri uploadFile(Uri file, StorageReference imgRef){
        UploadTask uploadTask = imgRef.putFile(file);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(exception -> {
            // Handle unsuccessful uploads
        }).addOnSuccessListener(taskSnapshot -> {
            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
            // ...
        });

//        get download url
        Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }

            // Continue with the task to get the download URL
            return imgRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri imageUri = (Uri) task.getResult();
            } else {
                // Handle failures
                // ...
            }
        });
        return imageUri;
    }

    public void downloadFile(StorageReference imgRef) throws IOException {

        File localFile = File.createTempFile("images", "jpg");
        imgRef.getFile(localFile)
                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        // Successfully downloaded data to local file
                        // ...
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle failed download
                // ...
            }
        });
    }
    public String createID(){
        Date now = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        return dateFormat.format(now);
    }
}
