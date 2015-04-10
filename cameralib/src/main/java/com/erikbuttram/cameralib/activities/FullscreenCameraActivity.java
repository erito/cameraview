package com.erikbuttram.cameralib.activities;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.erikbuttram.cameralib.Constants;
import com.erikbuttram.cameralib.R;
import com.erikbuttram.cameralib.components.ActionView;
import com.erikbuttram.cameralib.components.CameraView;

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
    private ActionView mActionView;
    private ImageView mOtherMode;
    private ImageButton mSwitchCameraView;

    private MediaMode mCurrentMode;
    private boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        mCameraView = (CameraView)findViewById(R.id.preview_surface);
        mSwitchCameraView = (ImageButton)findViewById(R.id.switch_cameras);
        mActionView = (ActionView)findViewById(R.id.primary_camera);
        mSwitchModeView = (LinearLayout)findViewById(R.id.switch_mode_other);
        mOtherMode = (ImageView)findViewById(R.id.other_mode);

        mHideFlags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        String modeKey = getIntent().getStringExtra(Constants.OPTION_MEDIA_TYPE);

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

        mSwitchCameraView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mCameraView.toggleCameraPosition();
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

        getWindow().getDecorView().setSystemUiVisibility(mHideFlags);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(mHideFlags);
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

}
