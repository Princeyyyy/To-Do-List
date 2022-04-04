package com.example.todolist;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.loginEmail)
    EditText mloginEmail;
    @BindView(R.id.loginPassword)
    EditText mloginPassword;
    @BindView(R.id.loginButton)
    Button mloginButton;
    @BindView(R.id.signupScreen)
    Button msignupScreen;

    private FirebaseAuth mAuth;
    private ProgressDialog loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        loader = new ProgressDialog(this);

        if (mAuth.getCurrentUser() != null) {
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
        }

        msignupScreen.setOnClickListener(this::onRegistrationClick);

        mloginButton.setOnClickListener(v -> {
            String email = mloginEmail.getText().toString().trim();
            String password = mloginPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                mloginEmail.setError("Email Required");
            }
            if (TextUtils.isEmpty(password)) {
                mloginPassword.setError("Password Required");
            } else {
                loader.setMessage("LogIn in Progress");
                loader.setCanceledOnTouchOutside(false);
                loader.show();

                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                        loader.dismiss();
                    } else {
                        String error = task.getException().toString();
                        Toast.makeText(LoginActivity.this, "Login Failed " + error, Toast.LENGTH_SHORT).show();
                        loader.dismiss();
                    }
                });
            }
        });
    }

    public void onRegistrationClick(View View) {
        Intent intent = new Intent(this, RegistrationActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
    }
}