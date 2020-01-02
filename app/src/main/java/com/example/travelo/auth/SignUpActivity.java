package com.example.travelo.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.travelo.R;
import com.example.travelo.database.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mUsers;

    private EditText mName;
    private EditText mSurname;
    private EditText mCity;
    private EditText mEmail;
    private EditText mPassword;
    private EditText mPassword2;

    private Button mRegisterButton;

    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mUsers = mDatabase.getReference("users");

        mName = findViewById(R.id.name);
        mSurname = findViewById(R.id.surname);
        mCity = findViewById(R.id.city);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mPassword2 = findViewById(R.id.password2);

        mProgressBar = findViewById(R.id.progressBar2);

        mRegisterButton = findViewById(R.id.SignUpButton);

        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isEmpty()) return;
                if (differentPassword()) return;
                if (isEmailCorrect()) return;

                inProgress(true);
                mAuth.createUserWithEmailAndPassword(mEmail.getText().toString(), mPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                User user = new User();

                                String uid = mAuth.getUid();
                                user.setAdmin(false);
                                user.setName(mName.getText().toString());
                                user.setSurname(mSurname.getText().toString());
                                user.setEmail(mEmail.getText().toString());

                                mUsers.child(uid).setValue(user);

                                Toast.makeText(SignUpActivity.this, "Zarejestrowano!", Toast.LENGTH_LONG).show();
                                inProgress(false);
                                finish();
                                return;
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        inProgress(false);
                        Toast.makeText(SignUpActivity.this, "Istnieje już konto przypisane do podanego emaila!", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void inProgress(boolean x) {
        if (x) {
            mProgressBar.setVisibility(View.VISIBLE);
            mRegisterButton.setEnabled(false);
        } else {
            mProgressBar.setVisibility(View.GONE);
            mRegisterButton.setEnabled(true);

        }
    }

    private boolean isEmpty() {
        if (TextUtils.isEmpty(mName.getText().toString())) {
            mName.setError("Pole jest wymagane!");
            return true;
        }
        if (TextUtils.isEmpty(mSurname.getText().toString())) {
            mSurname.setError("Pole jest wymagane!");
            return true;
        }
        if (TextUtils.isEmpty(mCity.getText().toString())) {
            mCity.setError("Pole jest wymagane!");
            return true;
        }
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

    private boolean differentPassword() {
        if (mPassword.getText().toString().equals(mPassword2.getText().toString())) {
            if (mPassword.getText().toString().length() >= 6) {
                return false;
            } else {
                Toast.makeText(SignUpActivity.this, "Hasło jest za krótkie!", Toast.LENGTH_LONG).show();
                return true;
            }
        } else {
            Toast.makeText(SignUpActivity.this, "Hasła są różne!", Toast.LENGTH_LONG).show();
            return true;
        }
    }

    private boolean isEmailCorrect() {
        if (mEmail.getText().toString().trim().matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")) {
            return false;
        } else {
            mEmail.setError("Niepoprawny Email!");
            return true;
        }
    }

}
