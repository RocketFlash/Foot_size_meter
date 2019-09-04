package com.tryfit.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by alexeyreznik on 11/04/2017.
 */

public class CanvasView extends View {

    private static final String TAG = "CanvasView";

    public static final double HELPER_RECT_SIZE = 0.5;
    public static final double HELPER_RECT_RATIO = 0.724;

    public static final float STROKE_WIDTH = 5f;
    public static final String GRAY_TRANSPARENT_COLOR = "#55000000";


    Context context;
    private Paint mPaintGray;
    private Paint mPaintBlack;
    private int mScreenWidth;
    private int mScreenHeight;
    private double mHelperRectWidth;
    private double mHelperRectHeight;
    private float mCornersWidth;
    private float mCornersHeight;

    public CanvasView(Context c, AttributeSet attrs) {
        super(c, attrs);
        context = c;

        // and we set a new Paint with the desired attributes
        mPaintBlack = new Paint();
        mPaintBlack.setAntiAlias(true);
        mPaintBlack.setColor(Color.BLACK);
        mPaintBlack.setStyle(Paint.Style.FILL);
        mPaintBlack.setStrokeJoin(Paint.Join.ROUND);

        mPaintGray = new Paint();
        mPaintGray.setAntiAlias(true);
        mPaintGray.setColor(Color.parseColor(GRAY_TRANSPARENT_COLOR));
    }

    // override onSizeChanged
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mScreenWidth = w;
        mScreenHeight = h;

        mHelperRectHeight = h * HELPER_RECT_SIZE;
        mHelperRectWidth = mHelperRectHeight * HELPER_RECT_RATIO;

        mCornersWidth = (float) (mHelperRectWidth / 50);
        mCornersHeight = (float) (mHelperRectHeight / 10);

        Log.d(TAG, "Screen: " + mScreenWidth + "/" + mScreenHeight + " Helper rect: " + mHelperRectWidth + "/" + mHelperRectHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float left = (float) ((mScreenWidth - mHelperRectWidth) / 2);
        float right = (float) (left + mHelperRectWidth);
        float top = (float) ((mScreenHeight - mHelperRectHeight) / 2);
        float bottom = (float) (top + mHelperRectHeight);

        canvas.drawRect(0, 0, left, mScreenHeight, mPaintGray);
        canvas.drawRect(right, 0, mScreenWidth, mScreenHeight, mPaintGray);
        canvas.drawRect(left, 0, right, top, mPaintGray);
        canvas.drawRect(left, bottom, right, mScreenHeight, mPaintGray);

        canvas.drawRect(left - mCornersWidth, top, left, top + mCornersHeight, mPaintBlack);
        canvas.drawRect(left - mCornersWidth, top - mCornersWidth, left + mCornersWidth + mCornersHeight, top, mPaintBlack);

        canvas.drawRect(right - mCornersHeight - mCornersWidth, top - mCornersWidth, right + mCornersWidth, top, mPaintBlack);
        canvas.drawRect(right + mCornersWidth, top, right, top + mCornersHeight, mPaintBlack);

        canvas.drawRect(left - mCornersWidth, bottom, left, bottom - mCornersHeight, mPaintBlack);
        canvas.drawRect(left - mCornersWidth, bottom + mCornersWidth, left + mCornersWidth + mCornersHeight, bottom, mPaintBlack);

        canvas.drawRect(right - mCornersHeight - mCornersWidth, bottom + mCornersWidth, right + mCornersWidth, bottom, mPaintBlack);
        canvas.drawRect(right + mCornersWidth, bottom, right, bottom - mCornersHeight, mPaintBlack);
    }
}
