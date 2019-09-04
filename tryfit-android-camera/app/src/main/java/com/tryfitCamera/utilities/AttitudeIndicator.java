package com.tryfitCamera.utilities;

/**
 * Created by Rauf Yagfarov on 21/09/2017.
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class AttitudeIndicator extends View {

    private static final boolean LOG_FPS = false;

    private static final int MIN_PLANE_COLOR = Color.parseColor("#E8D4BB");
    private static final float TOTAL_VISIBLE_PITCH_DEGREES = 45 * 2; // � 45�

    private final PorterDuffXfermode mXfermode;
    private final Paint mBitmapPaint;
    private final Paint mBallPaint;
    private final Paint mInternalPoint;
    private final Paint mInternalCircle;
    private final Paint circlePaint;


    // These are created once and reused in subsequent onDraw calls.
    private Bitmap mSrcBitmap;
    private Canvas mSrcCanvas;
    private Bitmap mDstBitmap;

    private int mWidth;
    private int mHeight;

    private boolean isGood;

    private float mPitch = 0; // Degrees
    private float mRoll = 0; // Degrees, left roll is positive

    public AttitudeIndicator(Context context) {
        this(context, null);
    }

    public AttitudeIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);

        isGood = false;

        mXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
        mBitmapPaint = new Paint();
        mBitmapPaint.setFilterBitmap(false);

        mBallPaint = new Paint();
        mBallPaint.setAntiAlias(true);
        mBallPaint.setColor(Color.RED);

        circlePaint = new Paint();
        circlePaint.setTextSize(18f);
        circlePaint.setTextAlign(Paint.Align.CENTER);
        circlePaint.setColor(Color.WHITE);


        mInternalCircle = new Paint();
        mInternalCircle.setAntiAlias(true);
        mInternalCircle.setStyle(Paint.Style.STROKE);
        mInternalCircle.setColor(Color.BLACK);
        mInternalCircle.setStrokeWidth(3);


        mInternalPoint = new Paint();
        mInternalPoint.setAntiAlias(true);
        mInternalPoint.setColor(MIN_PLANE_COLOR);
        mInternalPoint.setStrokeWidth(5);
        mInternalPoint.setStyle(Paint.Style.STROKE);
    }

    public float getPitch() {
        return mPitch;
    }

    public float getRoll() {
        return mRoll;
    }

    public void setAttitude(float pitch, float roll) {
        mPitch = pitch;
        mRoll = roll;
        invalidate();
    }

    public synchronized boolean getIsGood() {
        return isGood;
    }



    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    }

    private Bitmap getSrc() {
        if (mSrcBitmap == null) {
            mSrcBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
            mSrcCanvas = new Canvas(mSrcBitmap);
        }
        Canvas canvas = mSrcCanvas;

        float centerX = mWidth / 2;
        float centerY = mHeight / 2;

        // Background
        canvas.drawColor(Color.WHITE);

        // Save the state without any rotation/translation so
        // we can revert back to it to draw the fixed components.
        canvas.save();
        // Orient the earth to reflect the pitch and roll angles
        canvas.rotate(mRoll, centerX, centerY);
        canvas.translate(0, (mPitch / TOTAL_VISIBLE_PITCH_DEGREES) * mHeight);

        float centerXX = 0;
        float centerYY = -(mPitch / TOTAL_VISIBLE_PITCH_DEGREES) * mHeight;
        float point2x = centerX;
        float point2y = centerY;
        float newX = (float) (centerX + (point2x - centerXX) * Math.cos(Math.toRadians(mRoll)) - (point2y - centerYY) * Math.sin(Math.toRadians(mRoll)));
        float newY = (float) (centerY + (point2x - centerXX) * Math.sin(Math.toRadians(mRoll)) + (point2y - centerYY) * Math.cos(Math.toRadians(mRoll)));

        float prevValX = centerX;
        float prevValY = centerY + mWidth;
        canvas.drawCircle(prevValX, prevValY, 40, mBallPaint);
        // Return to normal to draw the miniature plane


        // Draw the nose dot
        double distance = mHeight - Math.sqrt((newX - centerX) * (newX - centerX) + (newY - centerY) * (newY - centerY));
//        String text = String.valueOf((int) distance);
//        Rect bounds = new Rect();
//        circlePaint.getTextBounds(text, 0, text.length(), bounds);
        if (distance > 80) {
            isGood = false;
            mBallPaint.setColor(Color.RED);
        } else {
            isGood = true;
            mBallPaint.setColor(Color.GREEN);
        }

//        canvas.drawText(text, prevValX, prevValY, circlePaint);
        canvas.restore();

        canvas.drawCircle(centerX, centerY, 60, mInternalCircle);
        canvas.drawPoint(centerX, centerY, mInternalPoint);

        return mSrcBitmap;
    }

    private Bitmap getDst() {
        if (mDstBitmap == null) {
            mDstBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(mDstBitmap);
            c.drawColor(Color.TRANSPARENT);

            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            p.setColor(Color.RED);
            c.drawOval(new RectF(0, 0, mWidth, mHeight), p);
        }
        return mDstBitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        Bitmap src = getSrc();
        Bitmap dst = getDst();

        @SuppressLint("WrongConstant") int sc = canvas.saveLayer(0, 0, mWidth, mHeight, null, Canvas.MATRIX_SAVE_FLAG
                | Canvas.CLIP_SAVE_FLAG | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG
                | Canvas.FULL_COLOR_LAYER_SAVE_FLAG | Canvas.CLIP_TO_LAYER_SAVE_FLAG);

        canvas.drawBitmap(dst, 0, 0, mBitmapPaint);
        mBitmapPaint.setXfermode(mXfermode);
        canvas.drawBitmap(src, 0, 0, mBitmapPaint);
        mBitmapPaint.setXfermode(null);

        canvas.restoreToCount(sc);
    }

}