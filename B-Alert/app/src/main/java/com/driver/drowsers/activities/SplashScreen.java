package com.driver.drowsers.activities;

import static com.driver.drowsers.helper.Helper.isInternetConnected;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.driver.drowsers.common.Common;
import com.driver.drowsers.databinding.ActivitySplashScreenBinding;
import com.driver.drowsers.model.RegistrationModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashScreen extends AppCompatActivity {
    FirebaseAuth mAuth;
    private ActivitySplashScreenBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();


        if (!isInternetConnected(SplashScreen.this)) {
            Toast.makeText(this, "You are not connected to internet", Toast.LENGTH_SHORT).show();
            Common.CurrentUser = null;
            navigateTo(0);
        }
        else if (mAuth.getCurrentUser() != null) {
            fetchUserInfo(mAuth.getCurrentUser().getUid());
        }
        else {
            navigateTo(-1);
        }

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
                            navigateTo(0);
                        } else {
                            navigateTo(-1);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        navigateTo(-1);
                    }
                });
    }

    private void navigateTo(int position) {
        if (position == 0) {
            startActivity(new Intent(SplashScreen.this, HomeActivity.class));
        } else
            startActivity(new Intent(SplashScreen.this, LoginActivity.class));

        finish();
    }

}