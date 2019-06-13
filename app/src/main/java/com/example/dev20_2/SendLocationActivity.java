package com.example.dev20_2;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class SendLocationActivity extends AppCompatActivity {
    private Spinner accidentList;
    private Button btnSubmit;
    private ImageButton imgBtn;
    private int type;
    private String description;
    private double lat, lng;
    private DatabaseReference mDatabase;


    private StorageReference mStorageRef;
    private static final int TAKE_PICTURE = 1;
    private String imageUri;

    private static final String IMAGE_DIRECTORY = "/YourDirectName";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_location);
        String id = createID();
        Context context = this;

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras != null) {
                lat = extras.getDouble("lat");
                lng = extras.getDouble("lng");
            }
        } else {
            lat = (Double) savedInstanceState.getSerializable("lat");
            lng = (Double) savedInstanceState.getSerializable("lng");
        }


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
        FirebaseStorage storage = FirebaseStorage.getInstance();
        mStorageRef = storage.getReference();

        description = findViewById(R.id.editText_description).toString();

        requestMultiplePermissions();
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

                StorageReference imgRef = mStorageRef.child("images/" + imageUri);
                Notification notification = new Notification();
                notification.setLat(lat);
                notification.setLng(lng);
                notification.setDescription(description);
                notification.setType(type);
                notification.setId(id);
                notification.setEmail("1");
                File imageFile = new File(imageUri);
                Uri image = Uri.fromFile(imageFile);
                UploadTask uploadTask = imgRef.putFile(image);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        Toast.makeText(getApplicationContext(),"Sending failed", Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(taskSnapshot -> {
                    Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                    firebaseUri.addOnSuccessListener(uri -> {
                        mDatabase = FirebaseDatabase.getInstance().getReference().child("notis");
                        notification.setImage(uri.toString());
                        mDatabase.push().setValue(notification);
                        Toast.makeText(context, "Sent report!", Toast.LENGTH_SHORT).show();
//                        Intent i = new Intent(SendLocationActivity.this, MainActivity.class);
//                        startActivity(i);
                    });
                });
            }
        });
    }

    private void requestMultiplePermissions() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {  // check if all permissions are granted
                            Toast.makeText(getApplicationContext(), "All permissions are granted by user!", Toast.LENGTH_SHORT).show();
                        }

                        if (report.isAnyPermissionPermanentlyDenied()) { // check for permanent denial of any permission
                            // show alert dialog navigating to Settings
                            //openSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(getApplicationContext(), "Some Error! ", Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();
    }

    public void takePhoto(String id, Context context) {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, TAKE_PICTURE);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PICTURE:
                Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                ImageView imageView = (ImageView) findViewById(R.id.imgReview);
                imageView.setImageBitmap(thumbnail);
                imageUri = saveImage(thumbnail);
                Toast.makeText(getApplicationContext(), "Image Saved!", Toast.LENGTH_SHORT).show();
        }
    }

    private String saveImage(Bitmap thumbnail) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File wallpaperDirectory = new File(Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
        if (!wallpaperDirectory.exists()) {  // have the object build the directory structure, if needed.
            wallpaperDirectory.mkdirs();
        }

        try {
            File f = new File(wallpaperDirectory, Calendar.getInstance().getTimeInMillis() + ".jpg");
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(this,
                    new String[]{f.getPath()},
                    new String[]{"image/jpeg"}, null);
            fo.close();
            Log.d("TAG", "File Saved::---&gt;" + f.getAbsolutePath());

            return f.getAbsolutePath();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";
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
        return UUID.randomUUID().toString();
    }
}
