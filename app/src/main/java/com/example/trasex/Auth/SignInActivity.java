package com.example.trasex.Auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.trasex.MapsActivity;
import com.example.trasex.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignInActivity extends AppCompatActivity {
    static final int REGISTER_REQUEST = 1;
    private FirebaseAuth mAuth;

    private EditText mEmail;
    private EditText mPassword;
    private Button mSignInButton;
    private Button mSignUpButton;

    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        FirebaseApp.initializeApp(this);

        mAuth = FirebaseAuth.getInstance();

        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);

        mSignInButton = findViewById(R.id.signInButton);
        mSignUpButton = findViewById(R.id.signUpButton);

        mProgressBar = findViewById(R.id.loadingProgressBar);

        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isEmpty()) return;
                inProgress(true);
                mAuth.signInWithEmailAndPassword(mEmail.getText().toString(), mPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {

                                Toast.makeText(SignInActivity.this, "Zalogowano!", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(SignInActivity.this, MapsActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                                return;
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        inProgress(false);
                        Toast.makeText(SignInActivity.this, "Logowanie się nie powiodło!" + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
            }
        });
    }

    private void inProgress(boolean x) {
        if (x) {
            mProgressBar.setVisibility(View.VISIBLE);
            mSignInButton.setEnabled(false);
            mSignUpButton.setEnabled(false);
        } else {
            mProgressBar.setVisibility(View.GONE);
            mSignInButton.setEnabled(true);
            mSignUpButton.setEnabled(true);
        }
    }

    private boolean isEmpty() {
        if (TextUtils.isEmpty(mEmail.getText().toString())) {
            mEmail.setError("Pole jest wymagane!");
            return true;
        }
        if (TextUtils.isEmpty(mPassword.getText().toString())) {
            mPassword.setError("Pole jest wymagane!");
            return true;
        }
        return false;
    }

}
