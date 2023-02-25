package com.driver.drowsers.fragments.facealert;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.driver.drowsers.detection.FaceGraphic;
import com.driver.drowsers.detection.GraphicFaceTracker;
import com.driver.drowsers.databinding.FragmentFaceAlertBinding;
import com.driver.drowsers.interfaces.IAlarmListener;
import com.driver.drowsers.service.FloatingCamera;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.IOException;

public class FaceAlert extends Fragment implements IAlarmListener {


    private CameraSource mCameraSource = null;

    private static final String TAG = "VideoFaceDetection";
    private static final int RC_HANDLE_GMS = 2;

    private FragmentFaceAlertBinding binding;

    public static IAlarmListener alarmListener;

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), result -> {
                if (result) {
                    createCameraSource();
                } else {
                    Toast.makeText(requireActivity(), "You must allow this permission to continue", Toast.LENGTH_SHORT).show();
                }
            }
    );

    public static Boolean isDetectionStarted = false;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentFaceAlertBinding.inflate(getLayoutInflater(), container, false);

        alarmListener = this;

        binding.btnStart.setOnClickListener(view -> {
            startDetectionView();
        });

        binding.endBtn.setOnClickListener(view -> {
            stopDetectionView();
        });

        binding.stopAlarm.setOnClickListener(view -> {
            if (FaceGraphic.mp.isPlaying()) {
                FaceGraphic.mp.stop();
                FaceGraphic.mp.reset();
            }

            binding.stopAlarm.setVisibility(View.INVISIBLE);

        });

        binding.imgFloating.setOnClickListener(view -> {
            if (!Settings.canDrawOverlays(requireActivity())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + requireActivity().getPackageName()));
                startActivityForResult(intent, 12);
            } else {
                startDetectionService();
            }
        });

        return binding.getRoot();
    }

    private void stopDetectionView() {
        isDetectionStarted = false;

        binding.btnStart.setVisibility(View.VISIBLE);
        binding.logo.setVisibility(View.VISIBLE);
        binding.stopAlarm.setVisibility(View.INVISIBLE);
        binding.endBtn.setVisibility(View.GONE);
        binding.preview.setVisibility(View.GONE);
        binding.imgFloating.setVisibility(View.INVISIBLE);
        stopDetection();
    }

    private void startDetectionView() {
        isDetectionStarted = true;
        binding.btnStart.setVisibility(View.GONE);
        binding.logo.setVisibility(View.GONE);
        binding.endBtn.setVisibility(View.VISIBLE);
        binding.preview.setVisibility(View.VISIBLE);
        binding.imgFloating.setVisibility(View.VISIBLE);
        startCameraSource();
    }

    private void startDetectionService() {
        ContextCompat.startForegroundService(requireActivity(), new Intent(requireActivity(), FloatingCamera.class));
        requireActivity().moveTaskToBack(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 12) {
            if (Settings.canDrawOverlays(requireActivity()))
                startDetectionService();

        }
    }

    private Boolean isCameraPermissionGranted() {
        return ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (isCameraPermissionGranted()) {
            createCameraSource();
        } else {
            // permission not granted, initiate request
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);

        }
    }

    private void createCameraSource() {
        FaceDetector detector = new FaceDetector.Builder(requireActivity())
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());

        mCameraSource = new CameraSource.Builder(requireActivity(), detector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setAutoFocusEnabled(true)
                .setRequestedFps(15.0f)
                .build();


    }

    /**
     * Restarts the camera.
     */
    @Override
    public void onResume() {
        super.onResume();
        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (isDetectionStarted) {
            startDetectionView();
        }
    }

    /**
     * Stops the camera.
     */

    @Override
    public void onPause() {
        super.onPause();
        binding.preview.stop();
    }

    private void stopDetection() {
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    private void startCameraSource() {
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(requireActivity().getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg = GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                binding.preview.start(mCameraSource, binding.faceOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    @Override
    public void onAlarmStarted() {
        binding.stopAlarm.setVisibility(View.VISIBLE);
    }

    @Override
    public void onAlarmFinished() {
        binding.stopAlarm.setVisibility(View.INVISIBLE);
    }

    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @NonNull
        @Override
        public Tracker<Face> create(@NonNull Face face) {
            return new GraphicFaceTracker(binding.faceOverlay);
        }
    }

}
