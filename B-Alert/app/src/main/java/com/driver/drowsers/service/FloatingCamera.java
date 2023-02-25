package com.driver.drowsers.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.driver.drowsers.R;
import com.driver.drowsers.activities.HomeActivity;
import com.driver.drowsers.detection.CameraPreview;
import com.driver.drowsers.detection.GraphicFaceTracker;
import com.driver.drowsers.detection.GraphicOverlay;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.IOException;

public class FloatingCamera extends Service {

    private WindowManager mWindowManager;
    private View mFloatingWidget;
    private CameraPreview preview;
    private GraphicOverlay faceOverlay;

    private CameraSource mCameraSource = null;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private String createNotificationChannel(String channelId, String channelName) {
        NotificationChannel chan = new NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        return channelId;
    }

    @SuppressLint({"ClickableViewAccessibility", "InflateParams"})
    @Override
    public void onCreate() {
        super.onCreate();

        Notification.Builder builder = new Notification.Builder(this, createNotificationChannel("Camera service", "Camera"))
                .setContentTitle(getString(R.string.app_name))
                .setContentText("SmartDetection Running")
                .setAutoCancel(true);
        Notification notification = builder.build();
        startForeground(1, notification);


        mFloatingWidget = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null);
        preview = mFloatingWidget.findViewById(R.id.preview);
        faceOverlay = mFloatingWidget.findViewById(R.id.faceOverlay);


        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mFloatingWidget, params);

        mFloatingWidget.findViewById(R.id.img_close).setOnClickListener(view -> {
            stopForeground(true);
            stopSelf();
        });
        mFloatingWidget.findViewById(R.id.img_open_app).setOnClickListener(view -> {
            stopForeground(true);
            stopSelf();

            Intent intent = new Intent(this.getApplicationContext(), HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            this.getApplicationContext().startActivity(intent);
        });


        preview.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        int Xdiff = (int) (event.getRawX() - initialTouchX);
                        int Ydiff = (int) (event.getRawY() - initialTouchY);
                        if (Xdiff < 10 && Ydiff < 10) {
//                            if (isViewCollapsed()) {
////                                collapsedView.setVisibility(View.GONE);
////                                expandedView.setVisibility(View.VISIBLE);
//                            }
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        mWindowManager.updateViewLayout(mFloatingWidget, params);
                        return true;
                }
                return false;
            }
        });

        createCameraSource();
    }

    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @NonNull
        @Override
        public Tracker<Face> create(@NonNull Face face) {
            return new GraphicFaceTracker(faceOverlay);
        }
    }


    void startCameraSource() {
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this.getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
//            Dialog dlg = GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
//            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                preview.start(mCameraSource, faceOverlay);
            } catch (IOException e) {
                Log.e("Error", "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    private void createCameraSource() {
        FaceDetector detector = new FaceDetector.Builder(this)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());

        mCameraSource = new CameraSource.Builder(this, detector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setAutoFocusEnabled(true)
                .setRequestedFps(15.0f)
                .build();

        startCameraSource();


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
        if (mFloatingWidget != null) mWindowManager.removeView(mFloatingWidget);
    }
}
