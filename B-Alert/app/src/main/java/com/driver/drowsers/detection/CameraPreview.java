package com.driver.drowsers.detection;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.driver.drowsers.detection.GraphicOverlay;
import com.google.android.gms.common.images.Size;
import com.google.android.gms.vision.CameraSource;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

public class CameraPreview extends ViewGroup {
    private static final String TAG = "CameraPreview";

    private Context mContext;
    private SurfaceView mSurfaceView;
    private boolean mStartRequested;
    private boolean mSurfaceAvailable;
    private CameraSource mCameraSource;

    private GraphicOverlay mOverlay;

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mStartRequested = false;
        mSurfaceAvailable = false;

        mSurfaceView = new SurfaceView(context);
        mSurfaceView.getHolder().addCallback(new SurfaceCallback());
        addView(mSurfaceView);


    }


    public void start(CameraSource cameraSource) throws IOException {
        if (cameraSource == null) {
            stop();
        }

        mCameraSource = cameraSource;

        if (mCameraSource != null) {
            mStartRequested = true;
            startIfReady();
        }
    }

    public void start(CameraSource cameraSource, GraphicOverlay overlay) throws IOException {
        mOverlay = overlay;
        start(cameraSource);
    }

    public void stop() {
        if (mCameraSource != null) {
            mCameraSource.stop();
        }
    }

    public void release() {
        if (mCameraSource != null) {
            mCameraSource.release();
            mCameraSource = null;
        }
    }

    @SuppressLint("MissingPermission")
    private void startIfReady() throws IOException {
        if (mStartRequested && mSurfaceAvailable) {
            mCameraSource.start(mSurfaceView.getHolder());
            if (mOverlay != null) {
                Size size = mCameraSource.getPreviewSize();
                int min = Math.min(size.getWidth(), size.getHeight());
                int max = Math.max(size.getWidth(), size.getHeight());
                if (isPortraitMode()) {
                    // Swap width and height sizes when in portrait, since it will be rotated by
                    // 90 degrees
                    mOverlay.setCameraInfo(min, max, mCameraSource.getCameraFacing());
                } else {
                    mOverlay.setCameraInfo(max, min, mCameraSource.getCameraFacing());
                }
                mOverlay.clear();
            }
            mStartRequested = false;

        }

        setParameters(mCameraSource);
    }

    private void setParameters(CameraSource cameraSource) {

        if (cameraSource != null) {
            Field[] declaredFields = CameraSource.class.getDeclaredFields();
//
            for (Field field : declaredFields) {
                if (field.getType() == Camera.class) {
                    Log.i(TAG, "setParameters: into if");

                    field.setAccessible(true);
                    try {
                        Camera camera = (Camera) field.get(cameraSource);
                        if (camera != null) {
                            Log.i(TAG, "setParameters: into second if");

                            Camera.Parameters params = camera.getParameters();
                            final int[] previewFpsRange = new int[2];
                            params.getPreviewFpsRange(previewFpsRange);
                            if (previewFpsRange[0] == previewFpsRange[1]) {
                                final List<int[]> supportedFpsRanges = params.getSupportedPreviewFpsRange();
                                for (int[] range : supportedFpsRanges) {
                                    if (range[0] != range[1]) {
                                        params.setPreviewFpsRange(range[0], range[1]);
                                        break;
                                    }
                                }
                            }
                            camera.setParameters(params);


                        } else
                            Log.i(TAG, "setParameters: into else");


                    } catch (IllegalAccessException e) {
                        Log.i(TAG, "setParameters: " + e.getMessage());
                        e.printStackTrace();
                    }

                    break;
                }
            }
        }

    }

    //    private static boolean cameraFocus(@NonNull CameraSource cameraSource, int brightness) {
//        Field[] declaredFields = CameraSource.class.getDeclaredFields();
//
//        for (Field field : declaredFields) {
//            if (field.getType() == Camera.class) {
//                field.setAccessible(true);
//                try {
//                    Camera camera = (Camera) field.get(cameraSource);
//                    if (camera != null) {
//                        Camera.Parameters params = camera.getParameters();
//                        params.setExposureCompensation(brightness);
//                        camera.setParameters(params);
//                        return true;
//                    }
//
//                    return false;
//                } catch (IllegalAccessException e) {
//                    e.printStackTrace();
//                }
//
//                break;
//            }
//        }
//
//        return false;
//    }

    private class SurfaceCallback implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder surface) {
            mSurfaceAvailable = true;
            try {
                startIfReady();
            } catch (IOException e) {
                Log.e(TAG, "Could not start camera source.", e);
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surface) {
            mSurfaceAvailable = false;
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int width = 320;
        int height = 240;
        if (mCameraSource != null) {
            Size size = mCameraSource.getPreviewSize();
            if (size != null) {
                width = size.getWidth();
                height = size.getHeight();
            }
        }

        // Swap width and height sizes when in portrait, since it will be rotated 90 degrees
        if (isPortraitMode()) {
            int tmp = width;
            width = height;
            height = tmp;
        }

        final int layoutWidth = right - left;
        final int layoutHeight = bottom - top;

        // Computes height and width for potentially doing fit width.
        int childWidth = layoutWidth;
        int childHeight = (int) (((float) layoutWidth / (float) width) * height);

        // If height is too tall using fit width, does fit height instead.
        if (childHeight > layoutHeight) {
            childHeight = layoutHeight;
            childWidth = (int) (((float) layoutHeight / (float) height) * width);
        }

        for (int i = 0; i < getChildCount(); ++i) {
            getChildAt(i).layout(0, 0, childWidth, childHeight);
        }

        try {
            startIfReady();
        } catch (IOException e) {
            Log.e(TAG, "Could not start camera source.", e);
        }
    }

    private boolean isPortraitMode() {
        int orientation = mContext.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return false;
        }
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            return true;
        }

        Log.d(TAG, "isPortraitMode returning false by default");
        return false;
    }
}
