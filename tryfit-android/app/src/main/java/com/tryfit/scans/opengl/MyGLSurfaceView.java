package com.tryfit.scans.opengl;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import java.util.Calendar;

/**
 * Created by alexeyreznik on 07/09/2017.
 */

public class MyGLSurfaceView extends GLSurfaceView {

    private static final String TAG = MyGLSurfaceView.class.getSimpleName();

    private static final int MAX_CLICK_DURATION = 200;
    private float mPreviousX;
    private float mPreviousY;
    private float mDensity;
    private long mStartClickTime;
    private MyGLRenderer mRenderer;

    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;

    public MyGLSurfaceView(Context context) {
        super(context);

        final DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mDensity = displayMetrics.density;

        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    public MyGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        final DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mDensity = displayMetrics.density;

        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    @Override
    public void setRenderer(Renderer renderer) {
        super.setRenderer(renderer);
        this.mRenderer = (MyGLRenderer) renderer;
    }


    @Override
    public boolean onTouchEvent(MotionEvent e) {
        mScaleDetector.onTouchEvent(e);

        if (e.getPointerCount() == 1) {
            float x = e.getX();
            float y = e.getY();

            switch (e.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    if (!mScaleDetector.isInProgress()) {
                        if (mRenderer != null) {
                            float deltaX = (x - mPreviousX) / mDensity / 2f;
                            float deltaY = (y - mPreviousY) / mDensity / 2f;

                            if (Math.abs(deltaX) < 10 && Math.abs(deltaX) < 10) {
                                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                                    mRenderer.startManualRotation();
                                    mRenderer.setDeltaX(mRenderer.getDeltaX() + deltaX);
                                } else {
                                    mRenderer.startManualRotation();
                                    mRenderer.setDeltaY(mRenderer.getDeltaY() + deltaY);
                                }
                            }
                        }
                    }
                    break;

                case MotionEvent.ACTION_DOWN: {
                    mStartClickTime = Calendar.getInstance().getTimeInMillis();
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    long clickDuration = Calendar.getInstance().getTimeInMillis() - mStartClickTime;
                    if (clickDuration < MAX_CLICK_DURATION) {
                        performClick();
                    }
                    break;
                }
            }

            mPreviousX = x;
            mPreviousY = y;
        }
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.5f, Math.min(mScaleFactor, 2.0f));

            if (mRenderer != null) {
                mRenderer.setScaleFactor(mScaleFactor);
            }

            return true;
        }
    }
}
