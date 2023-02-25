package com.driver.drowsers.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.driver.drowsers.R;
import com.driver.drowsers.common.Common;
import com.driver.drowsers.databinding.ActivityRegisterBinding;
import com.driver.drowsers.helper.Helper;
import com.driver.drowsers.model.RegistrationModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;


public class RegisterActivity extends AppCompatActivity {
    FirebaseAuth mFirebaseAuth;
    FirebaseDatabase database;
    DatabaseReference myRef;
    FirebaseUser user;
    FirebaseAuth mAuth;


    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.light_black));

        database = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        myRef = database.getReference();


        mAuth = FirebaseAuth.getInstance();

        binding.backLogin.setOnClickListener(view -> startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));
        binding.createAccount.setOnClickListener(view -> checkInput());


    }


    private void checkInput() {
        if (TextUtils.isEmpty(binding.fullName.getText().toString().trim())) {

            binding.fullName.setError("Please enter your Full name");
            binding.fullName.requestFocus();
        } else if (!Helper.isValidName(binding.fullName.getText().toString().trim())) {
            binding.fullName.setError("Please enter valid name");
            binding.fullName.requestFocus();
        } else if (!Helper.isEmailValid(binding.email.getText().toString().trim())) {
            binding.email.setError("Please enter email");
            binding.email.requestFocus();
        } else if (TextUtils.isEmpty(binding.phoneNumber.getText().toString().trim())) {
            binding.phoneNumber.setError("Please enter MobileNo");
            binding.phoneNumber.requestFocus();
        } else if (binding.phoneNumber.getText().toString().length() < 10 || binding.phoneNumber.getText().toString().length() > 11) {
            binding.phoneNumber.setError("Please enter valid mobile number");
            binding.phoneNumber.requestFocus();
        } else if (TextUtils.isEmpty(binding.password.getText().toString().trim())) {
            binding.password.setError("Please enter your password");
            binding.password.requestFocus();
        } else if (TextUtils.isEmpty(binding.rePassword.getText().toString().trim())) {
            binding.rePassword.setError("This filed can't be empty");
            binding.rePassword.requestFocus();
        } else if (!binding.password.getText().toString().trim().equals(binding.rePassword.getText().toString().trim())) {
            binding.rePassword.setError("Please match your password");
            binding.rePassword.requestFocus();
        } else {
            ContinueRegistration(binding.email.getText().toString().trim(), binding.password.getText().toString().trim());
            binding.progressBar.setVisibility(View.VISIBLE);
        }
    }

    private void ContinueRegistration(String email, String password) {
        Log.i("MyData", "ContinueRegistration: Email : " + email + " Password : " + password);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.i("MyData", "ContinueRegistration:Complete");

                        if (task.isSuccessful()) {
                            user = task.getResult().getUser();
                            uploadtofirebase(user);
                        } else {
                            binding.progressBar.setVisibility(View.GONE);
                            Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("MyData", "ContinueRegistration:Failure" + e.getMessage());

                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadtofirebase(FirebaseUser user) {
        RegistrationModel userModel = new RegistrationModel();
        userModel.setUid(user.getUid());
        userModel.setName(binding.fullName.getText().toString().trim());
        userModel.setEmail(binding.email.getText().toString().trim());
        userModel.setPassword(binding.password.getText().toString().trim());
        userModel.setMobileNo(binding.phoneNumber.getText().toString().trim());
        userModel.setRePassword(binding.rePassword.getText().toString().trim());


        FirebaseDatabase.getInstance().getReference("Users")
                .child(user.getUid())
                .setValue(userModel)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Common.CurrentUser = userModel;
                            Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                            startActivity(intent);
                        } else {
                            binding.progressBar.setVisibility(View.GONE);
                            Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        binding.progressBar.setVisibility(View.GONE);

                    }
                });

    }
}


