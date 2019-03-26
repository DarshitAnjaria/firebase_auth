package com.example.asus.firebaseauth;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {

    private Button btnLogin,btnCreate;
    private EditText edEmail,edPassword;
    private FirebaseAuth dAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
//
//        Context context = getApplicationContext();
//        LayoutInflater inflater = getLayoutInflater();
//        View customToast = inflater.inflate(R.layout.custom_toast,null);
//        Toast myToast = new Toast(context);
//        myToast.setView(customToast);
//        myToast.setDuration(Toast.LENGTH_LONG);
//        myToast.show();

        btnLogin = (Button)findViewById(R.id.btnLogin);
        btnCreate = (Button)findViewById(R.id.btnCreate);
        edEmail = (EditText)findViewById(R.id.etEmail);
        edPassword = (EditText)findViewById(R.id.etPassword);
        dAuth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthenticateUser();
            }
        });

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this,Signup.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (dAuth.getCurrentUser() != null){
            finish();
            startActivity(new Intent(Login.this,ProfilePage.class));
        }
    }

    public void AuthenticateUser(){
        String email = edEmail.getText().toString();
        String password = edPassword.getText().toString();

        final ProgressDialog pgLogin = new ProgressDialog(this);
        pgLogin.setMessage("Logging in...");
        pgLogin.setCancelable(false);
        pgLogin.show();

        if (email.isEmpty()){
            pgLogin.dismiss();

            edEmail.setError("Email Required");
            edEmail.requestFocus();
//            Toast.makeText(this,R.string.email_req,Toast.LENGTH_SHORT).show();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

            pgLogin.dismiss();

            edEmail.setError("Invalid Email Address");
            edEmail.requestFocus();

        } else if (password.isEmpty()) {
            pgLogin.dismiss();

            edPassword.setError("Password Required!");
            edPassword.requestFocus();
//            Toast.makeText(this,R.string.password_req,Toast.LENGTH_SHORT).show();
        } else {
            dAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    pgLogin.dismiss();
                    if (task.isSuccessful()) {
                        finish();
                        Intent intProfile = new Intent(Login.this, ProfilePage.class);
                        intProfile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intProfile);
                    } else {
                        Toast.makeText(Login.this, "Invalid Credentials", Toast.LENGTH_LONG);
                        Toast.makeText(Login.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}
