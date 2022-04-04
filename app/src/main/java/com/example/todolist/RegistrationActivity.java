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
import butterknife.OnLongClick;

public class RegistrationActivity extends AppCompatActivity {

    @BindView(R.id.registrationEmail)
    EditText mregistrationEmail;
    @BindView(R.id.registrationPassword)
    EditText mregistrationPassword;
    @BindView(R.id.signupButton)
    Button msignupButton;
    @BindView(R.id.loginScreen)
    Button mloginScreen;

    private FirebaseAuth mAuth;
    private ProgressDialog loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        loader = new ProgressDialog(this);

        mloginScreen.setOnClickListener(this::onLoginClick);

        msignupButton.setOnClickListener(v -> {
            String email = mregistrationEmail.getText().toString().trim();
            String password = mregistrationPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                mregistrationEmail.setError("Email Required");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                mregistrationPassword.setError("Password Required");
            } else {
                loader.setMessage("Registration in Progress");
                loader.setCanceledOnTouchOutside(false);
                loader.show();
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        Intent intent = new Intent(RegistrationActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                        loader.dismiss();
                    } else {
                        String error = task.getException().toString();
                        Toast.makeText(RegistrationActivity.this, "Registration Failed " + error, Toast.LENGTH_SHORT).show();
                        loader.dismiss();
                    }
                });
            }
        });
    }

    public void onLoginClick(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.stay);

    }
}