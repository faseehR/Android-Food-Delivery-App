package com.example.foodx;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class SignUpActivity extends AppCompatActivity {

    EditText nameEditText, emailEditTextSignUp, passwordEditTextSignUp, confirmPasswordEditText;
    Button signUpButton;
    TextView signInText;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        // Find Views
        nameEditText = findViewById(R.id.nameEditText);
        emailEditTextSignUp = findViewById(R.id.emailEditTextSignUp);
        passwordEditTextSignUp = findViewById(R.id.passwordEditTextSignUp);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        signUpButton = findViewById(R.id.SButton);
        signInText = findViewById(R.id.signInText);

        // Sign Up Button Click
        signUpButton.setOnClickListener(view -> registerUser());

        // Sign In Text Click
        signInText.setOnClickListener(view -> {
            Intent intent = new Intent(SignUpActivity.this, UserSignInActivity.class);
            startActivity(intent);
        });
    }

    private void registerUser() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditTextSignUp.getText().toString().trim();
        String password = passwordEditTextSignUp.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase sign up
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(SignUpActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignUpActivity.this, UserSignInActivity.class));
                        finish();
                    } else {
                        Exception e = task.getException();
                        Log.e("FIREBASE_SIGNUP", "Error: ", e);

                        if (e instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(SignUpActivity.this, "This email is already registered.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SignUpActivity.this, "Sign up failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
