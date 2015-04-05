package com.erikbuttram.cameralib.components;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.TextureView;
import android.view.WindowManager;

import com.erikbuttram.cameralib.R;

import java.io.IOException;
import java.util.List;

/**
 * Created by erikb on 3/31/15.
 * TODO:  Need to figure out shutter callback, two other callbacks for this
 */
public class CameraView extends TextureView implements TextureView.SurfaceTextureListener, Camera.AutoFocusCallback, Camera.OnZoomChangeListener {

    public static final String TAG = CameraView.class.getPackage() + " " +
            CameraView.class.getSimpleName();

    public static final int ID_NONE = -99;

    /**
     * {@link CameraView#mCurrentCamera}
     * @return the current camera that is in use.
     */
    public Camera getCurrentCamera() {
        return mCurrentCamera;
    }

    /**
     * {@link CameraView#mCameraInfo}
     * @return
     */
    public Camera.CameraInfo getCameraInfo() {
        return mCameraInfo;
    }

    /**
     * Set to enable the zoom feature on the camera, this will use the cameras built in max and
     * min zoom levels to intelligently set the zoom.  If zooming isn't supported,
     * this property is ignored
     * @param isEnabled
     */
    public void enableZoom(boolean isEnabled) {
        mZoomEnabled = isEnabled;
    }

    /**
     * Sets or unsets the auto focus, if set to false, there will be no auto focus
     * @param autoFocusEnabled
     */
    public void setAutoFocus(boolean autoFocusEnabled) {
        this.mAutoFocusEnabled = autoFocusEnabled;
    }

    /**
     * Used to set or unset the "shutter" sounds whenever a picture is taken with the camera.
     * This value is ignored in Jelly Bean (api 16) and below.
     * @param shutterEnabled
     */
    public void setShutterEnabled(boolean shutterEnabled) {
        this.mEnableShutter = shutterEnabled;
    }

    /**
     *  This is the {@link android.hardware.Camera.PictureCallback} that is used
     *  whenever {@link com.erikbuttram.cameralib.components.CameraView#takePicture()}
     *  is invoked, otherwise, when a preview is hit, nothing happens.
     */
    public void setPictureCallback(Camera.PictureCallback newCallback) {
        this.pictureCallback = newCallback;
    }

    /**
     * The currently used camera being used by the TextureView;
     */
    private Camera mCurrentCamera;

    /**
     *  Returns the Camera Info of the current camera
     */
    private Camera.CameraInfo mCameraInfo;
    private boolean mIsCameraOpen;
    private int mCurrentCameraId;
    private boolean mZoomEnabled = true;
    private boolean mAutoFocusEnabled = true;
    private boolean mEnableShutter = true;
    private Camera.PictureCallback pictureCallback;

    private ScaleGestureDetector mScaleGestureDetector;
    private CameraZoomListener mZoomListener;

    private void setAttributes(AttributeSet attributeSet) {
        TypedArray array = getContext().getTheme().obtainStyledAttributes(
                attributeSet,
                R.styleable.CameraView,
                0,
                0);

        mZoomEnabled = array.getBoolean(R.styleable.CameraView_zoomEnabled, true);
        boolean cycleToFront = array.getInteger(R.styleable.CameraView_cameraPosition, 0) != 0;
        if (cycleToFront) {
            setCameraPosition(Camera.CameraInfo.CAMERA_FACING_FRONT);
        } else {
            setCameraPosition(Camera.CameraInfo.CAMERA_FACING_BACK);
        }
        mAutoFocusEnabled = array.getBoolean(R.styleable.CameraView_autoFocus, true);
        mEnableShutter = array.getBoolean(R.styleable.CameraView_enableShutter, true);
    }

    //gets called before anything else
    private void init() {
        mCurrentCamera = null;
        mCurrentCameraId = ID_NONE;
        mCameraInfo = new Camera.CameraInfo();
        setSurfaceTextureListener(this);
        mZoomListener = new CameraZoomListener();
        mScaleGestureDetector = new ScaleGestureDetector(getContext(), mZoomListener);
        pictureCallback = null;
    }

    public CameraView(Context context) {
        super(context);
        init();
    }

    public CameraView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
        setAttributes(attributeSet);
    }

    public void releaseCamera() {
        mCurrentCamera.setZoomChangeListener(null);
        mCurrentCamera.stopPreview();
        mCurrentCamera.release();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mZoomEnabled) {
            mScaleGestureDetector.onTouchEvent(event);
        }
        return true;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

        if (mCurrentCamera == null) {
            //Default to the back facing position if it hasn't been set
            setCameraPosition(Camera.CameraInfo.CAMERA_FACING_BACK);
        }
        try {
            if (mIsCameraOpen) {
                Camera.getCameraInfo(mCurrentCameraId, mCameraInfo);
                mCurrentCamera.setPreviewTexture(getSurfaceTexture());
                List<Camera.Size> previewSizes = mCurrentCamera.getParameters().getSupportedPreviewSizes();
                //TODO:  Need to set the preview sizes based off of deltas to the size of Surface Texture
                adjustRotation();
                mCurrentCamera.startPreview();
            }
        } catch (IOException ioEx) {
            Log.e(TAG, String.format("Unable to initialize camera preview: %s", ioEx.getMessage()));
            mCurrentCamera = null;
            mIsCameraOpen = false;
        }
    }

    private void adjustRotation() {
        //reconcile the current screen orientation with the camera view
        WindowManager manager = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
        if (manager != null) {
            //set the orientation.
            int rotation = manager.getDefaultDisplay().getRotation();
            int degrees = 0;
            switch (rotation) {
                case Surface.ROTATION_0: degrees = 0; break;
                case Surface.ROTATION_90: degrees = 90; break;
                case Surface.ROTATION_180: degrees = 180; break;
                case Surface.ROTATION_270: degrees = 270; break;
            }
            int finalAngle;
            if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                finalAngle = (mCameraInfo.orientation + degrees) % 360;
                //invert the angle
                finalAngle = (360 - finalAngle) % 360;
            } else {
                finalAngle = (mCameraInfo.orientation - degrees + 360) % 360;
            }
            mCurrentCamera.setDisplayOrientation(finalAngle);
            mCurrentCamera.getParameters().setRotation(finalAngle);
        }
    }

    //NOTE:  These are some sane defaults that seem to work ok across different platforms
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void setInternalParams() {
        if (mCurrentCamera == null) {
            return;
        }
        Camera.Parameters params = mCurrentCamera.getParameters();
        //let the driver do the focusing for us
        params.setFocusAreas(null);
        params.setMeteringAreas(null);
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        if (mCurrentCamera.getParameters().isVideoStabilizationSupported()) {
            params.setVideoStabilization(true);
        }

        mCurrentCamera.setParameters(params);
        if (mEnableShutter && Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mCurrentCamera.enableShutterSound(true);
        }
    }

    /**
     * @param facing can be either {@link android.hardware.Camera.CameraInfo#CAMERA_FACING_FRONT} or
     *               {@link android.hardware.Camera.CameraInfo#CAMERA_FACING_BACK}
     * @return true if the view successfully swapped out the camera, false if no front facing camera
     * was available or the parameter passed was invalid
     */
    public boolean setCameraPosition(int facing) {
        if (facing != Camera.CameraInfo.CAMERA_FACING_FRONT &&
                facing != Camera.CameraInfo.CAMERA_FACING_BACK) {
            return false;
        }
        Camera.CameraInfo inspect = new Camera.CameraInfo();
        for (int idx = 0; idx < Camera.getNumberOfCameras(); idx++) {
            Camera.getCameraInfo(idx, inspect);
            if (inspect.facing == facing) {
                if (mCurrentCameraId != ID_NONE) {
                    releaseCamera();
                }
                try {
                    mCurrentCamera = Camera.open(idx);
                    mCurrentCameraId = idx;
                    mIsCameraOpen = true;
                    mCurrentCamera.startPreview();
                    mCameraInfo = inspect;
                    if (mCurrentCamera.getParameters().isZoomSupported() && mZoomEnabled) {
                        mZoomListener.setMaxZoom(mCurrentCamera.getParameters().getMaxZoom());
                        mCurrentCamera.setZoomChangeListener(this);
                    }
                    setInternalParams();
                    mCurrentCamera.autoFocus(this);
                } catch (Exception ex) {
                    //presumably this one works
                    mIsCameraOpen = false;
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Invokes the {@link Camera#takePicture(android.hardware.Camera.ShutterCallback, android.hardware.Camera.PictureCallback, android.hardware.Camera.PictureCallback)}
     * @param callback
     */
    public void takePicture(Camera.PictureCallback callback) {

    }

    public void takePicture() {

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {

        if (mIsCameraOpen) {
            releaseCamera();
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        //TODO:  Maybe apply some cool filter effects here

    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {

    }

    @Override
    public void onZoomChange(int zoomValue, boolean stopped, Camera camera) {

        //TODO: This seems to crash once in a while.
        if (camera != null && stopped) {
            camera.cancelAutoFocus();
            camera.autoFocus(this);
        }

    }

    private class CameraZoomListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        public CameraZoomListener() {
            this.mMaxZoom = 1f;
            this.mCurrentZoom = 0;
            this.mTolerance = .009f;
            this.mPreviousFactor = 0f;
        }

        public void setMaxZoom(float maxZoom) {
            this.mMaxZoom = maxZoom;
        }

        private int mCurrentZoom;
        private float mMaxZoom;
        private float mPreviousFactor;
        private float mTolerance;
        //only used for cameras that don't have smooth scrolling
        private Camera.Parameters useParams;

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {

            useParams = mCurrentCamera.getParameters();
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (!mCurrentCamera.getParameters().isZoomSupported()) {
                return false;
            }

            if (Math.abs(detector.getScaleFactor() - mPreviousFactor) < mTolerance) {
                return false;
            }

            boolean incr = detector.getScaleFactor() > 1;

            if (incr && mCurrentZoom < mMaxZoom) {
                mCurrentZoom++;
            } else if (!incr && mCurrentZoom > 0) {
                mCurrentZoom--;
            } else {
                return false;
            }

            if (mCurrentCamera.getParameters().isSmoothZoomSupported()) {
                mCurrentCamera.startSmoothZoom(mCurrentZoom);
            } else {
                if (!incr) {
                    mCurrentZoom = Math.max(mCurrentZoom-2, 0);
                } else {
                    mCurrentZoom = (int)Math.min(mCurrentZoom + 2, mMaxZoom);
                }
                useParams.setZoom(mCurrentZoom);
                mCurrentCamera.setParameters(useParams);
            }

            mPreviousFactor = detector.getScaleFactor();
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {

        }
    }
}
