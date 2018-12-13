package com.example.android.photoblog;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class AccountSetup extends AppCompatActivity {

    private CircleImageView profileImage;
    private Uri profileImageUri = null;
    EditText setupUserName;
    Button setUpSave;
    private String user_id;
    private boolean isChanged = false;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private ProgressBar setUpProgressBar;
    private FirebaseFirestore firebaseFirestore;
    private Bitmap compressedImageFile;
    private static final int PICK_IMAGE_REQUEST = 111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setup);

        Toolbar setUpToolbar = findViewById(R.id.accountSettingsToolbar);
//        setSupportActionBar(setUpToolbar);
        getSupportActionBar().setTitle("Account Setup");

        profileImage = (CircleImageView)findViewById(R.id.profileimage);
        setupUserName = (EditText)findViewById(R.id.accoutnSetupUserName);
        setUpSave = (Button)findViewById(R.id.accountSetupsave);
        setUpProgressBar = findViewById(R.id.setUp_progressBar);
        mAuth = FirebaseAuth.getInstance();
        user_id = mAuth.getCurrentUser().getUid();


        storageReference = FirebaseStorage.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference().child("profile_pic");
        firebaseFirestore = FirebaseFirestore.getInstance();




        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful()){

                    if(task.getResult().exists()){

                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");
                        setupUserName.setText(name);

                        profileImageUri = Uri.parse(image);
                        RequestOptions placeHolderRequest = new RequestOptions();
                      //  placeHolderRequest.placeholder(R.drawable.com_facebook_profile_picture_blank_portrait);
                        Glide.with(AccountSetup.this).setDefaultRequestOptions(placeHolderRequest).load(image).into(profileImage);
                    }
                }else{

                    String Error = task.getException().getMessage();
                    Toast.makeText(AccountSetup.this,"Error: "+ Error, Toast.LENGTH_LONG).show();
                }
            }
        });


        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if (ContextCompat.checkSelfPermission(AccountSetup.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(AccountSetup.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                        return;
                    }else{

                        cropImage();

                    }
                }else{

                    cropImage();
                }
            }
        });

        setUpSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String userName = setupUserName.getText().toString();
                if (!TextUtils.isEmpty(userName) && profileImageUri != null) {
                    setUpProgressBar.setVisibility(View.VISIBLE);

                     if(isChanged) {

                        user_id = mAuth.getCurrentUser().getUid();
                         File newImageFile = new File(profileImageUri.getPath());
                         try {

                             compressedImageFile = new Compressor(AccountSetup.this)
                                     .setMaxHeight(125)
                                     .setMaxWidth(125)
                                     .setQuality(50)
                                     .compressToBitmap(newImageFile);

                         } catch (IOException e) {
                             e.printStackTrace();
                         }

                         ByteArrayOutputStream baos = new ByteArrayOutputStream();
                         compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                         byte[] thumbData = baos.toByteArray();


                        UploadTask imagePath = storageReference.child("profile_images").child(user_id + ".jpg").putBytes(thumbData);

                        imagePath.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                if (task.isSuccessful()) {

                                    startFireStote(task, userName);
                                } else {
                                    String error = task.getException().getMessage();
                                    Toast.makeText(AccountSetup.this, "Error: " + error, Toast.LENGTH_LONG).show();
                                    setUpProgressBar.setVisibility(View.GONE);
                                }


                            }
                        });
                     }else{
                         startFireStote(null, userName);
                     }
                }

            }
        });
    }

    private void startFireStote(@NonNull final Task<UploadTask.TaskSnapshot> task, String userName) {
        Uri downloadUri;

       Task<Uri> s =  storageReference.child("Users/profile_images.jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                String url = task.getResult().toString();
           }
        });
        Map<String, String > userMap = new HashMap<>();
        userMap.put("name",userName);
        userMap.put("image",s.toString());

        firebaseFirestore.collection("Users").document(user_id).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){

                    Intent intent = new Intent(AccountSetup.this, MainActivity.class);
                    startActivity(intent);
                    finish();

                }else{
                    String Error = task.getException().getMessage();
                    Toast.makeText(AccountSetup.this,"Error: "+ Error, Toast.LENGTH_LONG).show();
                }

                setUpProgressBar.setVisibility(View.GONE);
            }
        });
    }

    private void cropImage() {
     /*   CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(AccountSetup.this);*/

        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        //intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK){

                Uri uri = data.getData();

                try {
                    compressedImageFile = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);


                    profileImageUri = data.getData();
                    profileImage.setImageBitmap(compressedImageFile);
                    profileImage.setImageURI(profileImageUri);
                    isChanged = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }



            }else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Exception error = result.getError();
            }
        }
    }
}
