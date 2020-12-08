package com.example.Secretgram;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StreamDownloadTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity {

    private Button UpdateAccountSetting;
    private EditText userName,userStatus;
    private CircleImageView userProfileImage;
    private String currentUserID;
    private DatabaseReference RootRef;

    private static final int GalleryPick=1;
    private StorageReference UserProfileImagesRef;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUserID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        UserProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        InitializeFiels();

        userName.setVisibility(View.INVISIBLE);

        UpdateAccountSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSetting();

            }
        });
        
        RetrieveUserInfo();

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GalleryPick);
            }
        });
    }




    private void InitializeFiels() {
        UpdateAccountSetting=(Button) findViewById(R.id.update_settings_button);
        userName = (EditText)findViewById(R.id.set_user_name);
        userStatus = (EditText)findViewById(R.id.set_profile_status);
        userProfileImage = (CircleImageView)findViewById(R.id.set_profile_image);
        loadingBar = new ProgressDialog(this);
        Toolbar settingsToolBar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(settingsToolBar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Settings");

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GalleryPick && resultCode==RESULT_OK && data!=null){
            //Uri ImageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode==RESULT_OK){
                assert result != null;
                final Uri resultUri = result.getUri();
                loadingBar.setTitle("Set Profile Image");
                loadingBar.setMessage("Please wait,your profile image is updating.");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                final StorageReference filePath = UserProfileImagesRef.child(currentUserID+ ".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(SettingActivity.this, "Profile Image uploaded Successfully.", Toast.LENGTH_SHORT).show();
                            //final String downloadUrl = task.getResult().getStorage().getDownloadUrl().toString();
                            final String downloadUrl1 = resultUri.toString();
                                    RootRef.child("Users").child(currentUserID).child("image")
                                    .setValue(downloadUrl1)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(SettingActivity.this, "Image save in Database Successfully.", Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }
                                            else{
                                                String message= Objects.requireNonNull(task.getException()).toString();
                                                Toast.makeText(SettingActivity.this, "Error: "+ message, Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }

                                        }
                                    });
                        }
                        else{
                            String message = Objects.requireNonNull(task.getException()).toString();
                            Toast.makeText(SettingActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void UpdateSetting() {
        String setUserName = userName.getText().toString();
        String setStatus = userStatus.getText().toString();

        if (TextUtils.isEmpty(setStatus))
        {
            userStatus.setText("hay, i am available now.");
            //Toast.makeText(this,"Please write your status.",Toast.LENGTH_SHORT).show();
        }

        if (TextUtils.isEmpty(setUserName))
        {
            Toast.makeText(this,"Please write your user name first.",Toast.LENGTH_SHORT).show();
        }
        else
        {
            HashMap<String,Object> profileMap = new HashMap<>();
            profileMap.put("uid",currentUserID);
            profileMap.put("name",setUserName);
            profileMap.put("status",setStatus);
            RootRef.child("Users").child(currentUserID).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                SendUserToMainActivity();
                                Toast.makeText(SettingActivity.this,"Profile Updated Successfulyy...",Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                String message = Objects.requireNonNull(task.getException()).toString();
                                Toast.makeText(SettingActivity.this,"Error : " + message,Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        }
    }
    private void RetrieveUserInfo() {
        RootRef.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")&& (dataSnapshot.hasChild("image")))){

                            String retrieveUserName = Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString();
                            String retrieveStatus = Objects.requireNonNull(dataSnapshot.child("status").getValue()).toString();
                            String retrieveProfileImage = Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString();
                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveStatus);
                            Picasso.get().load(retrieveProfileImage).into(userProfileImage);


                        }
                        else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")))
                        {
                            String retrieveUserName = Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString();
                            String retrieveStatus = Objects.requireNonNull(dataSnapshot.child("status").getValue()).toString();
                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveStatus);

                        }
                        else{
                            userName.setVisibility(View.VISIBLE);
                            Toast.makeText(SettingActivity.this,"Please set & update profile information..." ,Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
