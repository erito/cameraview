package com.erikbuttram.cameralib.components;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.TextureView;
import android.view.WindowManager;

import com.erikbuttram.cameralib.R;
import com.erikbuttram.cameralib.enums.CameraPosition;
import com.erikbuttram.cameralib.utils.CameraInterface;

import java.io.IOException;
import java.util.List;

/**
 * Created by erikb on 3/31/15.
 * TODO:  This is a big one, we're going to NEED the Camera2 apis to get this library to work on
 * TODO:  Lollipop, it crashes when we attempt to take a picture, We'll do this by picking strategies
 * based on API Level
 */
/**
 * Created by erikb on 3/31/15.
 * NOTE:  this is some code I threw together a while back....its pretty stable with respect to config changes,
 * and playing nicely with resource management, however it still needs some work, good todo would be to incorporate
 * L21 Camera Apis in as a different loader strategy
 */
public class CameraView extends TextureView implements TextureView.SurfaceTextureListener {

    public static final String TAG = CameraView.class.getPackage() + " " +
            CameraView.class.getSimpleName();
    public static final int ID_NONE = -99;
    /**
     * The currently used camera being used by the TextureView;
     */
    private Camera mCurrentCamera;
    /**
     * Returns the Camera Info of the current camera
     */
    private Camera.CameraInfo mCameraInfo;
    private boolean mIsCameraOpen;
    private int mCurrentCameraId;
    private boolean mZoomEnabled = true;
    private String mFocusSetting;
    private boolean mAutoFocusEnabled = true;
    private boolean mEnableShutter = true;
    private ScaleGestureDetector mScaleGestureDetector;
    private CameraZoomListener mZoomListener;
    public CameraView(Context context) {
        super(context);
        init();
    }
    public CameraView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    /**
     * {@link CameraView#mCurrentCamera}
     *
     * @return the current camera that is in use.
     */
    public Camera getCurrentCamera() {
        return mCurrentCamera;
    }

    /**
     * {@link CameraView#mCameraInfo}
     *
     * @return
     */
    public Camera.CameraInfo getCameraInfo() {
        return mCameraInfo;
    }

    /**
     * Set to enable the zoom feature on the camera, this will use the cameras built in max and
     * min zoom levels to intelligently set the zoom.  If zooming isn't supported,
     * this property is ignored
     *
     * @param isEnabled
     */
    public void enableZoom(boolean isEnabled) {
        mZoomEnabled = isEnabled;
    }

    /**
     * Sets or unsets the auto focus, if set to false, there will be no auto focus
     *
     * @param autoFocusEnabled
     */
    public void setAutoFocus(boolean autoFocusEnabled) {
        this.mAutoFocusEnabled = autoFocusEnabled;
    }

    /**
     * Used to set or unset the "shutter" sounds whenever a picture is taken with the camera.
     * This value is ignored in Jelly Bean (api 16) and below.
     *
     * @param shutterEnabled
     */
    public void setShutterEnabled(boolean shutterEnabled) {
        this.mEnableShutter = shutterEnabled;
    }

    /**
     * Used to set the focus mode for the camera view.  The default is
     * {@link android.hardware.Camera.Parameters#FOCUS_MODE_AUTO}
     *
     * @param mode
     */
    public void setFocusMode(String mode) {
        this.mFocusSetting = mode;
    }

    //TODO:  This needs to provide a cleaner api so we can at least fall back gracefully
    public void requestCameraView() {
        attemptResume();
    }

    //gets called before anything else
    private void init() {
        mCurrentCamera = null;
        mCurrentCameraId = ID_NONE;
        mCameraInfo = new Camera.CameraInfo();
        setSurfaceTextureListener(this);
        mZoomListener = new CameraZoomListener();
        mScaleGestureDetector = new ScaleGestureDetector(getContext(), mZoomListener);
        mFocusSetting = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;
        mZoomEnabled = true;
        mAutoFocusEnabled = true;
        mEnableShutter = true;
    }

    public void releaseCamera() {
        mCurrentCamera.stopPreview();
        mCurrentCamera.release();
    }

    /**
     * Toggles the camera position (I.E from front to back and vice versa)
     */
    public void toggleCamera() {
        if (mCameraInfo == null) {
            setCameraPosition(CameraPosition.BACK);
            return;
        }

        if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            setCameraPosition(CameraPosition.BACK);
        } else {
            setCameraPosition(CameraPosition.FRONT);
        }
    }

    /**
     * Basically an internal api that returns the focus mode to use, if any.
     *
     * @param supported
     * @return the supported {@link android.hardware.Camera.Parameters#getSupportedFocusModes()} that was specified,
     * the only focus mode available, AUTO if available, or an empty string (not to set focus mode)
     */
    private String setFocusMode(List<String> supported) {

        if (supported.size() == 1) {
            return supported.get(0);
        }

        if (supported.size() == 0) {
            return "";
        }

        if (supported.contains(mFocusSetting)) {
            return mFocusSetting;
        } else if (supported.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            return Camera.Parameters.FOCUS_MODE_AUTO;
        }

        return supported.get(0);
    }

    /**
     * Part of the initialization process
     */
    private void attemptResume() {
        if (mCurrentCamera == null) {
            setCameraPosition(CameraPosition.BACK);
        } else {
            try {
                //we're resuming, just start the preview again
                mCurrentCamera = Camera.open(mCurrentCameraId);
                setInternalParams();
                mIsCameraOpen = true;
            } catch (RuntimeException sadDays) {
                mIsCameraOpen = false;
            }
        }
        try {
            if (mIsCameraOpen) {
                Camera.getCameraInfo(mCurrentCameraId, mCameraInfo);
                adjustRotation();
                mCurrentCamera.setPreviewTexture(getSurfaceTexture());

                mCurrentCamera.startPreview();
            }
        } catch (IOException ioEx) {
            Log.e(TAG, String.format("Unable to initialize camera preview: %s", ioEx.getMessage()));
            mCurrentCamera = null;
            mIsCameraOpen = false;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = widthMeasureSpec;
        final int height = heightMeasureSpec;

        float ratio = (float) width / (float) height;

        setMeasuredDimension(width, ratio > 1 ? (int) (width * ratio) : (int) (height * ratio));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mZoomEnabled) {
            mScaleGestureDetector.onTouchEvent(event);
        }
        return true;
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mCurrentCamera != null) {
            mCurrentCamera.release();
            mIsCameraOpen = false;
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        attemptResume();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        if (mIsCameraOpen) {
            Camera.Parameters params = mCurrentCamera.getParameters();
            Camera.Size newSize = getPreviewSize();
            params.setPreviewSize(newSize.width, newSize.height);
            Camera.Size pictureSize = getPictureSize(newSize);
            params.setPictureSize(pictureSize.width, pictureSize.height);
            mCurrentCamera.setParameters(params);
            mCurrentCamera.startPreview();
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {

        if (mIsCameraOpen) {
            releaseCamera();
            mIsCameraOpen = false;
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    /**
     * returns the preview size to use
     *
     * @return 2 index int array with int[0] = width int[1] = height
     */
    private Camera.Size getPreviewSize() {

        List<Camera.Size> sizes = mCurrentCamera.getParameters().getSupportedPreviewSizes();
        final double ASPECT_TOLERANCE = 0.2;

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        double targetRatio = (double) height / width;
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int area = width * height;
        for (Camera.Size size : sizes) {
            double ratio = (double) size.height / size.width;

            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
                continue;
            }

            double areaDiff = Math.abs(area - (size.width * size.height));
            if (areaDiff < minDiff) {
                optimalSize = size;
                minDiff = areaDiff;
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - height) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - height);
                }
            }
        }

        return optimalSize;
    }

    private Camera.Size getPictureSize(Camera.Size previewSize) {

        List<Camera.Size> sizes = mCurrentCamera.getParameters().getSupportedPictureSizes();

        Camera.Size optimalSize = null;

        float previewRatio = (float) previewSize.width / (float) previewSize.height;
        float captureRatio;
        float optimalCaptureRatio;

        for (Camera.Size size : sizes) {
            if (size.width >= 1080 && size.height >= 1080) {
                if (optimalSize != null) {
                    captureRatio = (float) size.width / (float) size.height;
                    optimalCaptureRatio = (float) optimalSize.width / (float) optimalSize.height;

                    if (Math.abs(previewRatio - captureRatio) <= Math.abs(previewRatio - optimalCaptureRatio)) {
                        optimalSize = size;
                    }
                } else {
                    optimalSize = size;
                }
            }
        }
        return optimalSize;
    }

    private int getDisplayRotation() {
        if (getContext() == null) {
            return NO_ID;
        }
        WindowManager manager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        if (manager != null) {
            //set the orientation.
            return manager.getDefaultDisplay().getRotation();
        }
        return NO_ID;
    }

    private void adjustRotation() {
        //reconcile the current screen orientation with the camera view
        int rotation = getDisplayRotation();
        if (rotation != NO_ID) {
            //set the orientation.
            int degrees = 0;
            switch (rotation) {
                case Surface.ROTATION_0:
                    degrees = 0;
                    break;
                case Surface.ROTATION_90:
                    degrees = 90;
                    break;
                case Surface.ROTATION_180:
                    degrees = 180;
                    break;
                case Surface.ROTATION_270:
                    degrees = 270;
                    break;
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
        //TODO:  Pretty limited so far.  maybe more parameters can be set here?
        if (mCurrentCamera == null) {
            return;
        }
        Camera.Parameters params = mCurrentCamera.getParameters();
        Camera.Size size = getPreviewSize();
        params.setPreviewSize(size.width, size.height);

        Camera.Size pictureSize = getPictureSize(size);
        params.setPictureSize(pictureSize.width, pictureSize.height);

        //let the driver do the focusing for us
        String focus = setFocusMode(params.getSupportedFocusModes());
        if (!TextUtils.isEmpty(focus)) {
            params.setFocusMode(focus);
        }
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
    public boolean setCameraPosition(CameraPosition facing) {
        //TODO:  This method is fairly heavyweight, might be good to move off the main thread
        int facingInt = facing.ordinal();
        if (facingInt != Camera.CameraInfo.CAMERA_FACING_FRONT &&
                facingInt != Camera.CameraInfo.CAMERA_FACING_BACK) {
            return false;
        }
        Camera.CameraInfo inspect = new Camera.CameraInfo();
        for (int idx = 0; idx < Camera.getNumberOfCameras(); idx++) {
            Camera.getCameraInfo(idx, inspect);
            if (inspect.facing == facingInt) {
                try {
                    if (mCurrentCameraId != ID_NONE) {
                        //no longer needs a preview texture listener
                        releaseCamera();
                    }
                    mCurrentCamera = Camera.open(idx);
                    mCurrentCameraId = idx;
                    mIsCameraOpen = true;
                    mCameraInfo = inspect;
                    setInternalParams();
                    adjustRotation();
                    mCurrentCamera.setPreviewTexture(getSurfaceTexture());
                    mCurrentCamera.startPreview();
                    if (mCurrentCamera.getParameters().isZoomSupported() && mZoomEnabled) {
                        mZoomListener.setMaxZoom(mCurrentCamera.getParameters().getMaxZoom());
                    }
                    //mCurrentCamera.autoFocus(this);
                    return true;
                } catch (Exception ex) {
                    //presumably this one works
                    Log.e(TAG, String.format("Error initializing camera: %s", ex.getMessage()));
                    mIsCameraOpen = false;
                    return false;
                }
            }
        }
        return true;
    }

    public MediaRecorder startRecording() {

        if (mCurrentCamera != null) {
            MediaRecorder recorder = new MediaRecorder();
            recorder.setCamera(mCurrentCamera);
            recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            try {
                recorder.prepare();
                recorder.start();
                return recorder;
            } catch (IOException ioEx) {
                return null;
            }
        }
        return null;
    }

    /**
     * Invokes the {@link Camera#takePicture(android.hardware.Camera.ShutterCallback, android.hardware.Camera.PictureCallback, android.hardware.Camera.PictureCallback)}
     *
     * @param callback
     */
    public void takePicture(final Camera.PictureCallback callback) {
        if (mIsCameraOpen) {
            mCurrentCamera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    if (mIsCameraOpen) {
                        mCurrentCamera.startPreview();
                    }
                    callback.onPictureTaken(data, camera);
                }
            });
        }
    }

    private enum CameraPosition {
        BACK,
        FRONT
    }

    private class CameraZoomListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        private int mCurrentZoom;
        private float mMaxZoom;
        private float mPreviousFactor;
        private float mTolerance;
        //only used for cameras that don't have smooth scrolling
        private Camera.Parameters useParams;
        public CameraZoomListener() {
            this.mMaxZoom = 1f;
            this.mCurrentZoom = 0;
            this.mTolerance = .009f;
            this.mPreviousFactor = 0f;
        }

        public void setMaxZoom(float maxZoom) {
            this.mMaxZoom = maxZoom;
        }

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
                    mCurrentZoom = Math.max(mCurrentZoom - 2, 0);
                } else {
                    mCurrentZoom = (int) Math.min(mCurrentZoom + 2, mMaxZoom);
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
