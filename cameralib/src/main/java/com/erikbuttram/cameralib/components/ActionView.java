package com.erikbuttram.cameralib.components;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.erikbuttram.cameralib.R;

/**
 * Camera Action view that fires property animations when the touch event is lifted
 *
 */
public class ActionView extends ImageButton {

    public static interface OnActionViewExecutedListener {
        public void onActionViewExecuted();
    }

    public static final String TAG = ActionView.class.getPackage() + " " +
            ActionView.class.getSimpleName();

    private OnActionViewExecutedListener mListener;

    private void init() {

    }

    public ActionView(Context context) {
        super(context);
        init();
    }

    public ActionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ActionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ActionView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        Log.d(TAG, "Im getting something here");

        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            Log.d(TAG, "On Action up was pressed");
        } else if (event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) {
            Log.d(TAG, "On Action Down Down down");
        } else if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            Log.d(TAG, "On Action Down pressed");
        } else if (event.getActionMasked() == MotionEvent.ACTION_POINTER_UP) {
            Log.d(TAG, "On Action Pointer Up");
        }

        return super.onTouchEvent(event);
    }

    public void setDrawableFrom(int drawable) {
        setImageBitmap(setActionDrawable(drawable, false));
    }

    public void toggleAction(int drawable, boolean active) {
    }

    private Bitmap setActionDrawable(int actionDrawable, boolean active) {
        float dimen = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 72,
                getContext().getResources().getDisplayMetrics());
        float innerDimen = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 68,
                getContext().getResources().getDisplayMetrics());

        Bitmap output = Bitmap.createBitmap((int)dimen, (int)dimen, Bitmap.Config.ARGB_8888);
        Bitmap input = BitmapFactory.decodeResource(getContext().getResources(), actionDrawable);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, (int)dimen, (int)dimen);

        float halfX = input.getWidth() / 2;
        float halfY = input.getHeight() / 2;

        int left = (int) (rect.centerX() - halfX);
        int top = (int) (rect.centerY() - halfY);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        if (!active) {
            paint.setColor(Color.argb(255, 255, 255, 255));
        } else {
            paint.setColor(Color.argb(255, 255, 99, 71));
        }

        canvas.drawCircle(rect.width() / 2 + 0.7f, rect.height() / 2 + 0.7f, dimen / 2 + 0.1f, paint);
        paint.setColor(Color.argb(255, 0, 0, 0));
        canvas.drawCircle(rect.width() / 2 + 0.7f, rect.height() / 2 + 0.7f, innerDimen / 2 + 0.1f, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        canvas.drawBitmap(input, left, top, paint);

        return output;
    }

    private void activeAnimation() {
        Animator animator = AnimatorInflater.loadAnimator(getContext(),
                R.animator.anim_camera_action);
        animator.setTarget(this);
        animator.start();
    }


}
