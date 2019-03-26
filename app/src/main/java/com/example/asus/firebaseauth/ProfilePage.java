package com.example.asus.firebaseauth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

public class ProfilePage extends AppCompatActivity {

    private static final int CHOOSE_IMAGE = 101;
    String profileImageUrl;
    EditText displayName;
    TextView textVerified;
    Button save, logout;
    ImageView profilePic;
    Uri uriProfilePic;
    ProgressBar pgLoading;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);

        displayName = (EditText)findViewById(R.id.etDisName);
        save = (Button)findViewById(R.id.btnSave);
        logout = (Button)findViewById(R.id.btnLogout);
        profilePic = (ImageView)findViewById(R.id.imgProfile);
        pgLoading = (ProgressBar)findViewById(R.id.pgBar);
        textVerified = (TextView)findViewById(R.id.tvVerified);


        mAuth = FirebaseAuth.getInstance();

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageChooser();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInfo();
            }
        });

        loadUserInformation();

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userLogout();
            }
        });
    }

    private void userLogout() {

        FirebaseAuth.getInstance().signOut();
        finish();
        startActivity(new Intent(ProfilePage.this,Login.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null){
            finish();

            startActivity(new Intent(ProfilePage.this, Login.class));
        }
    }

    private void loadUserInformation() {
            final FirebaseUser user = mAuth.getCurrentUser();

           if (user!=null){
               if (user.getPhotoUrl() != null){
                   Glide.with(this)
                           .load(user.getPhotoUrl().toString())
                           .into(profilePic);
//                   Picasso.with(this).load(user.getPhotoUrl().toString()).
               }

               if (user.getDisplayName()!=null){
                   displayName.setText(user.getDisplayName());
               }

               if (user.isEmailVerified()){
                   textVerified.setText("Email Verified");
               }
               else
               {
                   textVerified.setText("Email Not Verified (Click to Verify)");
                   textVerified.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View v) {
                           final ProgressDialog pgDialog = new ProgressDialog(ProfilePage.this);
                           pgDialog.setTitle("Please Wait");
                           pgDialog.setMessage("Sending Verification Mail");
                           pgDialog.setCancelable(false);
                           pgDialog.show();


                           user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                               @Override
                               public void onComplete(@NonNull Task<Void> task) {
                                   pgDialog.dismiss();
                                   Toast.makeText(ProfilePage.this,"Verification Mail Sent", Toast.LENGTH_SHORT).show();
                               }
                           });
                       }
                   });
               }
           }

    }


    private void saveUserInfo() {
        String disName = displayName.getText().toString();

        if (disName.isEmpty()){
            displayName.setError("Name Required");
            displayName.requestFocus();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null && profileImageUrl != null){
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                    .setDisplayName(disName)
                    .setPhotoUri(Uri.parse(profileImageUrl))
                    .build();

            user.updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(ProfilePage.this,"Profile Updated",Toast.LENGTH_SHORT).show();

                        displayName.setText("");
                    }
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CHOOSE_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null){
            uriProfilePic = data.getData();

            try {

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uriProfilePic);
                profilePic.setImageBitmap(bitmap);

                uploadImage();

            } catch (IOException e) {
                Toast.makeText(this,"Error : " + e.getMessage(),Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private void uploadImage() {
        StorageReference profilePicReference =
                FirebaseStorage.getInstance().getReference("profilepics/" + System.currentTimeMillis() + ".jpg");
        if (profilePic != null){
            pgLoading.setVisibility(View.VISIBLE);
            profilePicReference.putFile(uriProfilePic).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    pgLoading.setVisibility(View.GONE);

                    profileImageUrl = taskSnapshot.getDownloadUrl().toString();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ProfilePage.this,"Error" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    pgLoading.setVisibility(View.GONE);
                }
            });
        }

    }

    private void imageChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), CHOOSE_IMAGE);
    }
}
