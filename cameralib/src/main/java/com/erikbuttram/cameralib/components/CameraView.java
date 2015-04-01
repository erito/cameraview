package com.erikbuttram.cameralib.components;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.WindowManager;

import java.io.IOException;
import java.util.List;

/**
 * Created by erikb on 3/31/15.
 */
public class CameraView extends TextureView implements TextureView.SurfaceTextureListener{

    public static final String TAG = CameraView.class.getPackage() + " " +
            CameraView.class.getSimpleName();

    /**
     * {@link CameraView#mCurrentCamera}
     * @return
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
     * The currently used camera being used by the TextureView;
     */
    private Camera mCurrentCamera;
    /**
     *  Returns the Camera Info of the current camera
     */
    private Camera.CameraInfo mCameraInfo;
    private boolean mIsCameraOpen;
    private int mCurrentCameraId;

    public CameraView(Context context) {
        super(context);
        setSurfaceTextureListener(this);
    }

    public CameraView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        setSurfaceTextureListener(this);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        //first we'll open the camera
        mCurrentCameraId = 0;
        mCameraInfo = new Camera.CameraInfo();
        try {
            mCurrentCamera = Camera.open(mCurrentCameraId);
            mIsCameraOpen = mCurrentCamera != null;
            if (mIsCameraOpen) {
                Camera.getCameraInfo(mCurrentCameraId, mCameraInfo);
                mCurrentCamera.setPreviewTexture(getSurfaceTexture());
                List<Camera.Size> previewSizes = mCurrentCamera.getParameters().getSupportedPreviewSizes();
                adjustRotation();
                mCurrentCamera.startPreview();
            }
        } catch (IOException ioEx) {
            Log.e(TAG, String.format("Unable to initialize camera preview: %s", ioEx.getMessage()));
            mCurrentCamera = null;
            mIsCameraOpen = false;
        } catch (Exception e) {
            Log.e(TAG, String.format("Unable to get camera: %s", e.getMessage()));
            e.printStackTrace();
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
        }
    }

    /**
     * TODO:  Animate the toggle, as parameter?
     *
     * @param facing can be either {@link android.hardware.Camera.CameraInfo#CAMERA_FACING_FRONT} or
     *               {@link android.hardware.Camera.CameraInfo#CAMERA_FACING_BACK}
     * @return true if the view successfully swapped out the camera, false if no front facing camera
     * was available or the parameter passed was invalid
     */
    public boolean toggleFrontFacingCamera(int facing) {
        if (facing != Camera.CameraInfo.CAMERA_FACING_FRONT &&
                facing != Camera.CameraInfo.CAMERA_FACING_BACK) {
            return false;
        }
        Camera.CameraInfo inspect = new Camera.CameraInfo();
        for (int idx = 0; idx < Camera.getNumberOfCameras(); idx++) {
            Camera.getCameraInfo(idx, inspect);
            if (inspect.facing == facing) {
                mCurrentCameraId = idx;
                mCurrentCamera.stopPreview();
                mCurrentCamera.release();
                try {
                    mCurrentCamera = Camera.open(idx);
                } catch (Exception ex) {
                    //presumably this one works
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {

        if (mIsCameraOpen) {
            mCurrentCamera.stopPreview();
            mCurrentCamera.release();
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
}
