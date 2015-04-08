package com.erikbuttram.cameralib;

/**
 */
public class Constants {
    public static final String ACTION_IMAGE_CAPTURE = "com.erikbuttram.cameralib.image_capture";

    public static final String KEY_VIDEO_CAPTURE = "video_type";
    public static final String KEY_IMAGE_CAPTURE = "image_type";

    /**
     * <p>
     * Sets the {@link com.erikbuttram.cameralib.activities.FullscreenCameraActivity} to either
     * default to either be {@link #KEY_IMAGE_CAPTURE} or {@link #KEY_VIDEO_CAPTURE};
     * </p>
     *
     * <p>The default is {@link #KEY_IMAGE_CAPTURE}</p>
     *
     */
    public static final String OPTION_MEDIA_TYPE = "media_type_key";

}
