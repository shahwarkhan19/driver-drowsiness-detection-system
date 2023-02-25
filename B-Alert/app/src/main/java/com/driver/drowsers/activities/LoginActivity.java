package com.driver.drowsers.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.driver.drowsers.R;
import com.driver.drowsers.common.Common;
import com.driver.drowsers.databinding.ActivityLoginBinding;
import com.driver.drowsers.helper.Helper;
import com.driver.drowsers.model.RegistrationModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    FirebaseAuth mAuth;

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.light_black));

        mAuth=FirebaseAuth.getInstance();

        binding.forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            startActivity(new Intent(LoginActivity.this,ForgetPassword.class));
            }
        });

        binding.signUp.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));


        binding.login.setOnClickListener(view -> checkInput());

    }

    private void checkInput() {
        if (!Helper.isEmailValid(binding.email.getText().toString().trim())) {
            binding.email.setError("incorrect");
            binding.email.requestFocus();

        } else if (TextUtils.isEmpty(binding.password.getText().toString().trim())) {
            binding.password.setError("incorrect");
            binding.password.requestFocus();

        } else {
            binding.progressBar.setVisibility(View.VISIBLE);
            ContinueLogin(binding.email.getText().toString().trim(), binding.password.getText().toString().trim());
        }
    }

    private void ContinueLogin(String email, String password) {

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        fetchUserInfo(task.getResult().getUser().getUid());
                    } else {
                        binding.progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    binding.progressBar.setVisibility(View.INVISIBLE);
                });
    }

    private void fetchUserInfo(String uid) {
        FirebaseDatabase.getInstance().getReference("Users")
                .child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        RegistrationModel currentUser = snapshot.getValue(RegistrationModel.class);
                        if (currentUser != null) {
                            Common.CurrentUser = currentUser;
                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                        } else {
                            binding.progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(LoginActivity.this, "no data found please register again", Toast.LENGTH_SHORT).show();

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(LoginActivity.this, "Fallied login", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}