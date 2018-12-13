package com.example.android.photoblog;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class signUp extends AppCompatActivity {

    private EditText signUpEmail, signUpPassword, signUpConfirmPassword;
    private Button createAccount, alreadyhaveAnAccount;
    private FirebaseAuth mAuth;
    ProgressBar loginProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        signUpEmail = (EditText)findViewById(R.id.signUp_email);
        signUpPassword = (EditText)findViewById(R.id.signUp_password);
        signUpConfirmPassword = (EditText)findViewById(R.id.signUp_confirm_password);
        createAccount =(Button)findViewById(R.id.createAccount_button);
        alreadyhaveAnAccount = (Button)findViewById(R.id.already_a_user);
        loginProgress = (ProgressBar)findViewById(R.id.loginprogress);

        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String signUpemail = signUpEmail.getText().toString().trim();
                String signUppassword = signUpPassword.getText().toString().trim();
                String ConfirmPassword = signUpConfirmPassword.getText().toString().trim();


                if(!TextUtils.isEmpty(signUpemail) && !TextUtils.isEmpty(signUppassword) && !TextUtils.isEmpty(ConfirmPassword)){


                    if(signUppassword.equals(ConfirmPassword)){

                        loginProgress.setVisibility(View.VISIBLE);

                        mAuth.createUserWithEmailAndPassword(signUpemail,signUppassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if(task.isSuccessful()){

                                    Intent loginIntent = new Intent(signUp.this, AccountSetup.class);
                                    startActivity(loginIntent);
                                    finish();

                                }else{
                                    String errorMessage = task.getException().getMessage();
                                    Toast.makeText(signUp.this,"Error:" + errorMessage, Toast.LENGTH_SHORT).show();
                                }

                                loginProgress.setVisibility(View.GONE);
                            }
                        });
                    }else{

                        Toast.makeText(signUp.this, "Mismatch in Passwords", Toast.LENGTH_SHORT).show();

                    }


                }


            }
        });

        alreadyhaveAnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            sendToMainActivity();
        }
    }

    private void sendToMainActivity() {
        Intent loginIntent = new Intent(signUp.this, MainActivity.class);
        startActivity(loginIntent);
        finish();
    }
}
