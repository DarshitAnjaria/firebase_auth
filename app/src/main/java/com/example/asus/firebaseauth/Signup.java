package com.example.asus.firebaseauth;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import java.util.regex.Pattern;

public class Signup extends AppCompatActivity {

    private Button login,signup;
    private EditText email,password;
    private FirebaseAuth dAuth;
    private static String TAG = "FireAuth";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        login = (Button)findViewById(R.id.btnLog);
        signup = (Button)findViewById(R.id.btnSignup);
        email = (EditText)findViewById(R.id.etAuthEmail);
        password = (EditText)findViewById(R.id.etAuthPassword);
        dAuth = FirebaseAuth.getInstance();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(Signup.this,Login.class));
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateUser();
            }
        });
    }

    private void CreateUser() {

        final String txtEmail = email.getText().toString().trim();
        String txtPassword = password.getText().toString().trim();

        final ProgressDialog pgCreating = new ProgressDialog(this);
        pgCreating.setMessage("Creating User...");
        pgCreating.setCancelable(false);
        pgCreating.show();

        if (txtEmail.isEmpty()){
            pgCreating.dismiss();
//            Toast.makeText(Signup.this,R.string.email_req,Toast.LENGTH_SHORT).show();
            email.setError("Please Enter Email!");
            email.requestFocus();
        }

       else if (!Patterns.EMAIL_ADDRESS.matcher(txtEmail).matches()){
            pgCreating.dismiss();
            email.setError("Enter Valid Email");
            email.requestFocus();
        }
        else if (txtPassword.isEmpty()){
           pgCreating.dismiss();
           password.setError("Please Enter Password!");
           password.requestFocus();
//            Toast.makeText(Signup.this,R.string.password_req,Toast.LENGTH_SHORT).show();

        }
        else if (txtPassword.length() < 8){
            pgCreating.dismiss();
            password.setError("Minimum Length is 8");
            password.requestFocus();
        }
        else
        {
            dAuth.createUserWithEmailAndPassword(txtEmail,txtPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    pgCreating.dismiss();
                    if (task.isSuccessful()){
                        finish();
                        Intent intNext = new Intent(Signup.this,ProfilePage.class);
                        intNext.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intNext);
                    }
                    else {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException){
                            Toast.makeText(Signup.this,"This E-Mail is Alreay Exists!",Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Log.d(TAG,"Error while Creating" + task.getException().getMessage());
                            Toast.makeText(Signup.this,"Error While Creating" + task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                        }

                    }
                }
            });
        }
    }
}
