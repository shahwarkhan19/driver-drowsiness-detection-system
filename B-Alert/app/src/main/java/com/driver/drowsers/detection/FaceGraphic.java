package com.driver.drowsers.detection;

import android.content.res.AssetFileDescriptor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;

import com.driver.drowsers.BAlert;
import com.driver.drowsers.fragments.facealert.FaceAlert;
import com.driver.drowsers.helper.AlertPref;
import com.google.android.gms.vision.face.Face;


import java.io.IOException;

public class FaceGraphic extends GraphicOverlay.Graphic {
    private static final float ID_TEXT_SIZE = 40.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;
    private Boolean tunePlaying = false;
    private static final int COLOR_CHOICES[] = {
            Color.BLUE,
            Color.CYAN,
            Color.GREEN,
            Color.MAGENTA,
            Color.RED,
            Color.WHITE,
            Color.YELLOW
    };
    private static int mCurrentColorIndex = 0;

    private Paint mFacePositionPaint;
    private Paint mIdPaint;
    private Paint mBoxPaint;

    private volatile Face mFace;
    private int mFaceId;

    public static MediaPlayer mp = new MediaPlayer();

    private boolean isEyeDetected = false;
    private int eyeDetectionCounter = 0;

    FaceGraphic(GraphicOverlay overlay) {
        super(overlay);

        mCurrentColorIndex = (mCurrentColorIndex + 1) % COLOR_CHOICES.length;
        final int selectedColor = COLOR_CHOICES[mCurrentColorIndex];

        mFacePositionPaint = new Paint();
        mFacePositionPaint.setColor(selectedColor);

        mIdPaint = new Paint();
        mIdPaint.setColor(selectedColor);
        mIdPaint.setTextSize(ID_TEXT_SIZE);

        mBoxPaint = new Paint();
        mBoxPaint.setColor(selectedColor);
        mBoxPaint.setStyle(Paint.Style.STROKE);
        mBoxPaint.setStrokeWidth(BOX_STROKE_WIDTH);
    }

    void setId(int id) {
        mFaceId = id;
    }


    /**
     * Updates the face instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    void updateFace(Face face) {
        mFace = face;
        postInvalidate();
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        Face face = mFace;
        if (face == null) {
            return;
        }
        Paint p = new Paint();
        // Draws a  square at the position of the detected face, with the face's track id below.
        float x = translateX(face.getPosition().x + face.getWidth() / 2);
        float y = translateY(face.getPosition().y + face.getHeight() / 2);

        canvas.drawText("Left Eye" + face.getIsLeftEyeOpenProbability(), 0, (float) (canvas.getHeight() / 4), mIdPaint);
        canvas.drawText("Right Eye" + face.getIsRightEyeOpenProbability(), 0, canvas.getHeight(), mIdPaint);


        if (isDrown(face)) {
            eyeDetectionCounter += 1;
            Log.i("MyEyeDetection", "draw: Outer Called");

            if (eyeDetectionCounter > 4 && !isEyeDetected) {
                isEyeDetected = true;


                new Handler().postDelayed(() -> {
                    eyeDetectionCounter = 0;

                    if (isDrown(face)) {
                        p.setColor(Color.RED);
                        p.setTextSize(280);
                        Log.e("leftright", "draw: " + face.getIsLeftEyeOpenProbability() + " " + face.getIsRightEyeOpenProbability());
                        canvas.drawText("Alert!!", 0, canvas.getHeight() / 2, p);

                        Log.i("MyEyeDetection", "draw: Inner Called");


                        if (!tunePlaying)
                            playAudio();
                    }

                    isEyeDetected = false;

                }, 200);
            }


        } else if (eyeDetectionCounter > 1) {
            eyeDetectionCounter = 1;
        }


        // Draws a bounding box around the face.
        float xOffset = scaleX(face.getWidth() / 2.0f);
        float yOffset = scaleY(face.getHeight() / 2.0f);
        float left = x - xOffset;
        float top = y - yOffset;
        float right = x + xOffset;
        float bottom = y + yOffset;
        canvas.drawRect(left, top, right, bottom, mBoxPaint);

        // Draws a circle for each face feature detected
//        for (Landmark landmark : face.getLandmarks()) {
//            // the preview display of front-facing cameras is flipped horizontally
//            float cx = canvas.getWidth() - scaleX(landmark.getPosition().x);
//            float cy = scaleY(landmark.getPosition().y);
//            canvas.drawCircle(cx, cy, 10, mIdPaint);
//        }
    }

    /**
     * if Eyes are currently closed this method will return true in that scenario
     *
     * @param face
     * @return
     */
    //previous value 0.4 new value 0.2
    private Boolean isDrown(Face face) {
        return ((face.getIsRightEyeOpenProbability() < 0.26) && (face.getIsRightEyeOpenProbability() > -1.0)) && ((face.getIsLeftEyeOpenProbability() < 0.26) && (face.getIsLeftEyeOpenProbability() > -1.0));
    }

    private void playAudio() {
        tunePlaying = true;
        Log.i("MyEyesLocation", "playAudio: called");
        if (mp != null && mp.isPlaying()) {
            mp.stop();
        }
        try {
            AssetFileDescriptor as = BAlert.getContext().getAssets().openFd(AlertPref.getValue(AlertPref.CURRENT_TUNE, "alarm.wav"));
            mp.setDataSource(as.getFileDescriptor(), as.getStartOffset(), as.getLength());
            as.close();
            mp.prepare();

        } catch (IOException e) {
            e.printStackTrace();
        }

        FaceAlert.alarmListener.onAlarmStarted();

        mp.start();
        mp.setOnCompletionListener(mediaPlayer -> {
            mp.reset();
            tunePlaying = false;
            FaceAlert.alarmListener.onAlarmFinished();
        });
    }
}
