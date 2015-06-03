package com.erikbuttram.cameralib.components;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
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
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;

import com.erikbuttram.cameralib.R;

/**
 * Camera Action view that fires property animations when the touch event is lifted
 *
 */
public class ActionView extends ImageButton {


    private AnimationRunnable mAnimationRunnable;

    public static interface OnActionViewExecutedListener {
        public void onActionViewInvoked();
    }

    public static final String TAG = ActionView.class.getPackage() + " " +
            ActionView.class.getSimpleName();

    public void setOnActionListener(OnActionViewExecutedListener mListener) {
        this.mListener = mListener;
    }

    private OnActionViewExecutedListener mListener;
    private int mDrawableId;
    private Handler mHandler; //for the recording animation loop
    private boolean mIsRecording;

    private void init() {
        mListener = null;
        mHandler = new Handler(Looper.getMainLooper());
        int longCount = getContext().getResources().getInteger(android.R.integer.config_longAnimTime);
        int sleepCount = 1050;
        mAnimationRunnable = new AnimationRunnable(sleepCount, getContext());
        mIsRecording = false;
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

        //only care about the first'n
        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            //trigger event and animation
            if (mDrawableId == R.drawable.camera_small) {
                activeAnimation(R.animator.anim_camera_btn_inactive);
                setImageBitmap(setActionDrawable(false));
                if (mListener != null) {
                    mListener.onActionViewInvoked();
                }
            }
        } else if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            if (mDrawableId == R.drawable.camera_small) {
                setImageBitmap(setActionDrawable(true));
                activeAnimation(R.animator.anim_camera_btn_active);
            } else if (mListener != null) {
                mListener.onActionViewInvoked();
            }
        }
        return super.onTouchEvent(event);
    }

    public void stopAnimation() {
        mHandler.removeCallbacks(mAnimationRunnable);
    }

    public void setDrawableFrom(int drawable) {
        mDrawableId = drawable;
        if (drawable == R.drawable.stop) {
            mIsRecording = true;
            setImageResource(drawable);
            mHandler.post(mAnimationRunnable);
        } else {
            if (mIsRecording) {
                mIsRecording = false;
                mHandler.removeCallbacks(mAnimationRunnable);
            }
            setImageBitmap(setActionDrawable(false));
        }
    }

    private Bitmap setRecordingDrawable(Integer color) {
        float dimen = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 72,
                getContext().getResources().getDisplayMetrics());
        float innerDimen = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 68,
                getContext().getResources().getDisplayMetrics());

        Bitmap output = Bitmap.createBitmap((int)dimen, (int)dimen, Bitmap.Config.ARGB_8888);
        Bitmap input = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.stop);
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
        paint.setColor(color);
        canvas.drawCircle(rect.width() / 2 + 0.7f, rect.height() / 2 + 0.7f, innerDimen / 2 + 0.1f, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        canvas.drawBitmap(input, left, top, paint);

        return output;
    }

    private Bitmap setActionDrawable(boolean active) {
        float dimen = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 72,
                getContext().getResources().getDisplayMetrics());
        float innerDimen = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 68,
                getContext().getResources().getDisplayMetrics());

        Bitmap output = Bitmap.createBitmap((int)dimen, (int)dimen, Bitmap.Config.ARGB_8888);
        Bitmap input = BitmapFactory.decodeResource(getContext().getResources(), mDrawableId);
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

    private void activeAnimation(int animatorId) {
        Animator animator = AnimatorInflater.loadAnimator(getContext(),
                animatorId);
        animator.setTarget(this);
        animator.start();
    }

    private static class AnimationRunnable implements Runnable {

        private int mSleep;
        private boolean mToRed;


        public AnimationRunnable(int sleepInterval, Context context) {
            mToRed = true;
            mSleep = sleepInterval;
        }

        @Override
        public void run() {
            ValueAnimator animator;
            Integer black = getContext().getResources().getColor(R.color.black);
            Integer red = getContext().getResources().getColor(R.color.red);
            if (mToRed) {
                mToRed = false;
                animator = ValueAnimator.ofObject(new ArgbEvaluator(), black,
                    red);
                animator.setInterpolator(new AccelerateInterpolator());
            } else {
                mToRed = true;
                animator = ValueAnimator.ofObject(new ArgbEvaluator(), red,
                        black);
                animator.setInterpolator(new DecelerateInterpolator());
            }
            animator.setDuration(1000);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (mIsRecording) {
                        setImageBitmap(setRecordingDrawable((Integer) animation.getAnimatedValue()));
                    }
                }
            });
            animator.start();
            mHandler.postDelayed(this, mSleep);
        }
    }
}
