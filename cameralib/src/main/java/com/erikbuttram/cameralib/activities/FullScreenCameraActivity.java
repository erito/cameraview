package com.erikbuttram.cameralib.activities;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.TextureView;

import com.erikbuttram.cameralib.R;

import java.io.IOException;

public class FullScreenCameraActivity extends Activity implements TextureView.SurfaceTextureListener {

    public static final String TAG = FullScreenCameraActivity.class.getPackage() + " " +
            FullScreenCameraActivity.class.getSimpleName();

    private Camera currentCamera;
    private boolean isCameraOpen;
    private TextureView textureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_camera);

        textureView = (TextureView)findViewById(R.id.preview_surface);
        //first we'll open the camera
        try {
            currentCamera = Camera.open();
            isCameraOpen = currentCamera != null;
            if (isCameraOpen) {
                currentCamera.setPreviewTexture(textureView.getSurfaceTexture());
                currentCamera.startPreview();
                currentCamera.startSmoothZoom(1);
            }
        } catch (IOException ioEx) {
            Log.e(TAG, String.format("Unable to initialize camera preview: %s", ioEx.getMessage()));
        } catch (Exception e) {
            Log.e(TAG, String.format("Unable to get camera: %s", e.getMessage()));
            currentCamera = null;
            isCameraOpen = false;
        }

    }

    @Override
    public void onStop() {
        releaseResources();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_full_screen_camera, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void releaseResources() {
        currentCamera.release();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (isCameraOpen) {
            currentCamera.stopPreview();
            currentCamera.release();
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
}
