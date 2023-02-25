package com.driver.drowsers.activities;


import androidx.appcompat.app.AppCompatActivity;

import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupMenu;


import com.driver.drowsers.R;
import com.driver.drowsers.databinding.ActivityHomeActiveBinding;
import com.driver.drowsers.fragments.facealert.FaceAlert;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeActiveBinding binding;
    private NavController controller;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeActiveBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.light_black));


        binding.map.setBackgroundColor(getResources().getColor(R.color.light_black));
        binding.faceAlert.setBackgroundColor(getResources().getColor(R.color.light_green));

        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        controller = navHostFragment.getNavController();

        binding.map.setOnClickListener(view -> {
            if (controller.getCurrentDestination().getId() != R.id.map2)
                binding.map.setBackgroundColor(getResources().getColor(R.color.light_green));
            binding.faceAlert.setBackgroundColor(getResources().getColor(R.color.light_black));
            controller.navigate(R.id.map2);
        });

        binding.faceAlert.setOnClickListener(view -> {
            if (controller.getCurrentDestination().getId() != R.id.faceAlert)
                binding.map.setBackgroundColor(getResources().getColor(R.color.light_black));
            binding.faceAlert.setBackgroundColor(getResources().getColor(R.color.light_green));
            controller.navigate(R.id.faceAlert);

        });
        binding.menuTop.setOnClickListener(view -> topMenu(view));


        initViews();
    }

    private void initViews() {
        controller.addOnDestinationChangedListener((navController, navDestination, bundle) -> {
            switch (navDestination.getId()) {
                case R.id.faceAlert: {
                    binding.mapTop.setVisibility(View.INVISIBLE);
                    binding.linearLayoutBottom.setVisibility(View.VISIBLE);
                    binding.menuTop.setVisibility(View.VISIBLE);
                    binding.backPress.setVisibility(View.INVISIBLE);
                    binding.profileTop.setVisibility(View.INVISIBLE);
                    binding.faceAlertTop.setVisibility(View.VISIBLE);
                    binding.toneTop.setVisibility(View.INVISIBLE);
                    binding.relativeLayout2Top.setVisibility(View.VISIBLE);
                    break;
                }

            }
        });
    }

    private void topMenu(View view) {
        PopupMenu popup = new PopupMenu(getApplicationContext(), view);
        popup.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.profile:
                    controller.navigate(R.id.profile2);
                    return true;
                case R.id.tone:
                    controller.navigate(R.id.tone2);
                    return true;
                case R.id.rate_us:
                    openDialog();

                default:
                    return false;

            }
        });
        popup.inflate(R.menu.menu);
        popup.show();
    }

    private void openDialog() {

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.rate_us);

        dialog.findViewById(R.id.ok).setOnClickListener(view -> dialog.dismiss());
        dialog.findViewById(R.id.cancel).setOnClickListener(view -> dialog.dismiss());


        dialog.show();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FaceAlert.isDetectionStarted = false;
    }
}