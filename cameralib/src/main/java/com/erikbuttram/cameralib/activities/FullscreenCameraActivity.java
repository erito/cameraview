package com.erikbuttram.cameralib.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.erikbuttram.cameralib.Constants;
import com.erikbuttram.cameralib.R;
import com.erikbuttram.cameralib.components.CameraView;
import com.erikbuttram.cameralib.enums.CameraPosition;

public class FullscreenCameraActivity extends Activity {

    public static final String TAG = FullscreenCameraActivity.class.getPackage() + " " +
            FullscreenCameraActivity.class.getSimpleName();

    private enum MediaMode {
        Picture,
        Video
    }

    private int mHideFlags;

    private CameraView mCameraView;
    private LinearLayout mSwitchModeView;
    private ImageButton mActionView;
    private ImageButton mSwitchCameraView;

    private MediaMode mCurrentMode;
    private boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        mCameraView = (CameraView)findViewById(R.id.preview_surface);
        mSwitchCameraView = (ImageButton)findViewById(R.id.switch_cameras);
        mActionView = (ImageButton)findViewById(R.id.primary_camera);
        mSwitchModeView = (LinearLayout)findViewById(R.id.switch_mode_other);

        mHideFlags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        String modeKey = getIntent().getStringExtra(Constants.OPTION_MEDIA_TYPE);

        if (modeKey == null || modeKey.equals(Constants.KEY_IMAGE_CAPTURE)) {
            mCurrentMode = MediaMode.Picture;
        } else {
            mCurrentMode = MediaMode.Video;
        }

        mSwitchCameraView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mCameraView.toggleCameraPosition();
            }
        });

        getWindow().getDecorView().setSystemUiVisibility(mHideFlags);

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(mHideFlags);
        }
    }
}
