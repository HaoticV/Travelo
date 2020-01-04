package com.example.travelo.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.travelo.BaseActivity;
import com.example.travelo.QApp;
import com.example.travelo.R;
import com.example.travelo.map.MapsActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignInActivity extends BaseActivity {
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mUsers;

    private EditText mEmail;
    private EditText mPassword;
    private Button mSignInButton;
    private SignInButton mGoogleSignInButton;
    private TextView mSignUpButton;

    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        QApp.fAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    Intent intent = new Intent(SignInActivity.this, MapsActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

    setContentView(R.layout.activity_sign_in);
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mUsers = mDatabase.getReference("users");


        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);

        mSignInButton = findViewById(R.id.signInButton);
        mSignUpButton = findViewById(R.id.signUpButton);
        mGoogleSignInButton = findViewById(R.id.googleSignInButton);

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
                                onLogInSuccess();
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
        mGoogleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logIn();

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finishAffinity();
    }


}
