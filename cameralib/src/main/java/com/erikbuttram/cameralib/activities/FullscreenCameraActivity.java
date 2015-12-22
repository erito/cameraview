package com.erikbuttram.cameralib.activities;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.erikbuttram.cameralib.Constants;
import com.erikbuttram.cameralib.R;
import com.erikbuttram.cameralib.components.ActionView;
import com.erikbuttram.cameralib.components.CameraView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FullscreenCameraActivity extends Activity implements ActionView.OnActionViewExecutedListener, Camera.PictureCallback {

    public static final String MEDIA_OUT_KEY = "com.erikbuttram.output_media_key";
    public static final String VIDEO_MAX_LENGTH_KEY = "com.erikbuttram.output_length_key";

    public static final int RESULT_MEDIA_ACTION_FAILED = 0x100;
    public static final int RESULT_MEDIA_ACTION_SUCCESS = 0x010;
    public static final int LENGTH_NONE = -1;

    private static final String TAG = FullscreenCameraActivity.class.getPackage() + " " +
            FullscreenCameraActivity.class.getSimpleName();

    private enum MediaMode {
        Picture,
        Video
    }

    private int mHideFlags;

    private int mMaxLength;
    private CameraView mCameraView;
    private LinearLayout mSwitchModeView;
    private ActionView mActionView;
    private ImageView mOtherMode;
    private ImageButton mSwitchCameraView;
    private MediaRecorder mRecorder;

    private MediaMode mCurrentMode;
    private boolean mIsRecording = false;
    private File outputFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        mCameraView = (CameraView)findViewById(R.id.preview_surface);
        mSwitchCameraView = (ImageButton)findViewById(R.id.switch_cameras);
        mActionView = (ActionView)findViewById(R.id.primary_camera);
        mSwitchModeView = (LinearLayout)findViewById(R.id.switch_mode_other);
        mOtherMode = (ImageView)findViewById(R.id.other_mode);
        mRecorder = null;

        mHideFlags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        String modeKey = getIntent().getStringExtra(Constants.OPTION_MEDIA_TYPE);
        if (getIntent().getParcelableExtra(MEDIA_OUT_KEY) == null) {
            outputFile = null;
        } else {
            Uri locUri = getIntent().getParcelableExtra(MEDIA_OUT_KEY);
            outputFile = new File(locUri.getPath());
        }


        if (modeKey == null || modeKey.equals(Constants.KEY_IMAGE_CAPTURE)) {
            mCurrentMode = MediaMode.Picture;
            //mActionView.setImageBitmap(setActionDrawable(R.drawable.camera_small, this, false));
            mOtherMode.setImageResource(R.drawable.video_small);
            mActionView.setDrawableFrom(R.drawable.camera_small);
        } else {
            mCurrentMode = MediaMode.Video;
            //mActionView.setImageBitmap(setActionDrawable(R.drawable.video_small, this, false));
            mOtherMode.setImageResource(R.drawable.camera_small);
            mActionView.setDrawableFrom(R.drawable.video_small);

        }
        if (getIntent().hasExtra(VIDEO_MAX_LENGTH_KEY)) {
            mMaxLength = getIntent().getIntExtra(VIDEO_MAX_LENGTH_KEY, LENGTH_NONE);
        } else {
            mMaxLength = LENGTH_NONE;

        }

        mSwitchCameraView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mCameraView.toggleCamera();
            }
        });

        mSwitchModeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentMode == MediaMode.Picture) {
                    mCurrentMode = MediaMode.Video;
                    mActionView.setDrawableFrom(R.drawable.video_small);
                } else {
                    mCurrentMode = MediaMode.Picture;
                    mActionView.setDrawableFrom(R.drawable.camera_small);
                }
                toggleIcons();
            }
        });

        mActionView.setOnActionListener(this);

        getWindow().getDecorView().setSystemUiVisibility(mHideFlags);
    }

    @Override
    public void onStop() {

        if (mIsRecording) {
            mActionView.stopAnimation();
            mActionView.setDrawableFrom(R.drawable.video_small);
            mIsRecording = false;
        }
        super.onStop();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(mHideFlags);
        }
    }

    @Override
    public void onActionViewInvoked() {
        if (mCurrentMode == MediaMode.Picture) {
            mCameraView.takePicture(this);
        } else {
            if (!mIsRecording) {
                mSwitchCameraView.setEnabled(false);
                mSwitchModeView.setEnabled(false);
                mIsRecording = true;
                mRecorder = mCameraView.startRecording();
                if (mRecorder != null) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
                    mActionView.setDrawableFrom(R.drawable.stop);
                }
            } else {
                mSwitchCameraView.setEnabled(true);
                mSwitchModeView.setEnabled(true);
                mIsRecording = false;
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                mActionView.setDrawableFrom(R.drawable.video_small);
            }
        }
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        if (outputFile == null || data == null) {
            return;
        }

        try {
            FileOutputStream stream = new FileOutputStream(outputFile);
            stream.write(data);
            stream.close();
            setResult(RESULT_MEDIA_ACTION_SUCCESS);
            finish();
        } catch (FileNotFoundException fileEx) {
            Log.d(TAG, "Unable to capture image: file not found");
        } catch (IOException ioEx) {
            Log.d(TAG, String.format("IOException occurred while capturing image: %s", ioEx.getMessage()));
        }
    }

    private void toggleIcons() {
        int lonAnimTime = getResources().getInteger(android.R.integer.config_longAnimTime);

        ObjectAnimator animVanish = ObjectAnimator.ofFloat(mOtherMode, "alpha", 1f, 0f);
        ObjectAnimator animReappear = ObjectAnimator.ofFloat(mOtherMode, "alpha", 0f, 1f);
        animVanish.setDuration(lonAnimTime);
        animReappear.setDuration(lonAnimTime);
        animVanish.start();

        if (mCurrentMode == MediaMode.Picture) {
            mOtherMode.setImageResource(R.drawable.video_small);
        } else {
            mOtherMode.setImageResource(R.drawable.camera_small);
        }

        animReappear.start();
    }

    private class RecorderProgressRunnable implements Runnable {

        @Override
        public void run() {
            //TODO:  Finish.

        }
    }

}
