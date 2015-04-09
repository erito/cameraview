package com.erikbuttram.cameralib.activities;

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
import android.util.TypedValue;
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
    private ImageView mActionView;
    private ImageButton mSwitchCameraView;

    private MediaMode mCurrentMode;
    private boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        mCameraView = (CameraView)findViewById(R.id.preview_surface);
        mSwitchCameraView = (ImageButton)findViewById(R.id.switch_cameras);
        mActionView = (ImageView)findViewById(R.id.primary_camera);
        mSwitchModeView = (LinearLayout)findViewById(R.id.switch_mode_other);

        mHideFlags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        String modeKey = getIntent().getStringExtra(Constants.OPTION_MEDIA_TYPE);

        if (modeKey == null || modeKey.equals(Constants.KEY_IMAGE_CAPTURE)) {
            mCurrentMode = MediaMode.Picture;
            mActionView.setImageBitmap(getActionDrawable(R.drawable.camera_small, this));
        } else {
            mCurrentMode = MediaMode.Video;
            mActionView.setImageBitmap(getActionDrawable(R.drawable.video_small, this));
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


    private Bitmap getActionDrawable(int actionDrawable, Context context) {
        float dimen = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 76,
                context.getResources().getDisplayMetrics());
        float innerDimen = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 70,
                context.getResources().getDisplayMetrics());

        Bitmap output = Bitmap.createBitmap((int)dimen, (int)dimen, Bitmap.Config.ARGB_8888);
        Bitmap input = BitmapFactory.decodeResource(context.getResources(), actionDrawable);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, (int)dimen, (int)dimen);

        float halfX = input.getWidth() / 2;
        float halfY = input.getHeight() / 2;

        int left = (int) (rect.centerX() - halfX);
        int top = (int) (rect.centerY() - halfY);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.argb(255, 255, 255, 255));
        canvas.drawCircle(rect.width() / 2 + 0.7f, rect.height() / 2 + 0.7f, dimen / 2 + 0.1f, paint);
        paint.setColor(Color.argb(255, 0, 0, 0));
        canvas.drawCircle(rect.width() / 2 + 0.7f, rect.height() / 2 + 0.7f, innerDimen / 2 + 0.1f, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        canvas.drawBitmap(input, left, top, paint);

        return output;
    }
}
