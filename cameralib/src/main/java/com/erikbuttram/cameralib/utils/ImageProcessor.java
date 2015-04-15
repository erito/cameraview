package com.erikbuttram.cameralib.utils;

import android.content.Context;
import android.graphics.Bitmap;

/**
 * Created by erikb on 4/13/15.
 * Utility that will reconcile images that have issues
 * with orientation of the camera sensor vs the rotation of the device, just not right this second
 */
public class ImageProcessor {

    private int orientation;
    private int context;
    private Bitmap bitmap;

    public ImageProcessor(RequestBuilder bulider) {
        this.orientation = orientation;
        this.bitmap = bitmap;
    }

    public static class RequestBuilder {

        private Context context;
        private int orientation;
        private String fileLocation;

    }
}
