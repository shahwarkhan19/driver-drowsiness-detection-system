package com.driver.drowsers.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;


import com.driver.drowsers.databinding.ActivityForgetPasswordBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgetPassword extends AppCompatActivity {
    private ActivityForgetPasswordBinding binding;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityForgetPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth=FirebaseAuth.getInstance();

        binding.backPress.setOnClickListener(view -> onBackPressed());

        binding.resetPassword.setOnClickListener(view -> checkInput());

    }

    private void checkInput() {
        if (TextUtils.isEmpty(binding.forgetEmail.getText().toString().trim())){
            binding.forgetEmail.setText("requred");
        }else
        {
            forgetPassword(binding.forgetEmail.getText().toString());
        }
    }

    private void forgetPassword(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(ForgetPassword.this,"Check your email",Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ForgetPassword.this,LoginActivity.class));
                    }else
                        Toast.makeText(ForgetPassword.this,"error",Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                     Toast.makeText(ForgetPassword.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }
}