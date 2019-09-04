package com.tryfit.fittings;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.tryfit.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexeyreznik on 30/06/2017.
 */

public class FitrateCircleView extends View {
    float mFitrateAngle = -90;

    float mLineWidth;
    float mToggleLineWidth;
    float mPadding;
    float mToggleRadius;
    RectF mFitrateRect;
    Paint mCirclePaint;
    Paint mTogglePaint;
    Paint mGrayPaint;
    Paint mWhitePaint;

    final static int[] COLORS = {
            Color.parseColor("#00caff"), //Blue
            Color.parseColor("#0033ff"), //Dark Blue
            Color.parseColor("#e4103c"), //Red
            Color.parseColor("#f8c028"), //Yellow
            Color.parseColor("#19f041")}; //Green

    List<LinearGradient> gradients;

    public FitrateCircleView(@NonNull Context context) {
        super(context);
        initVariables();
    }

    public FitrateCircleView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initVariables();
    }

    public FitrateCircleView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initVariables();
    }

    public FitrateCircleView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initVariables();
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initVariables();
    }

    public void setFitrate(float fitratePositive) {
        this.mFitrateAngle = fitrateToAngle(fitratePositive);
        invalidate();
    }

    public int fitrateToAngle(float fitrate) {
        int angle = -90;
        double diff = 10. - Math.abs(fitrate);
        if (fitrate > 0) {
            angle += (int) (Math.sqrt(diff) * 160 / Math.sqrt(10));
        } else {
            angle -= (int) (Math.sqrt(diff) * 160 / Math.sqrt(10));
        }
        return angle;
    }

    void initVariables() {
        int w = getMeasuredWidth();
        int h = getMeasuredHeight();
        mPadding = w / 15;
        mLineWidth = w / 20;
        mToggleLineWidth = mLineWidth / 3;
        mToggleRadius = mLineWidth / 2 + mToggleLineWidth;
        mFitrateRect = new RectF(mPadding, mPadding, w - mPadding, h - mPadding);

        mCirclePaint = new Paint();
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setStrokeWidth(mLineWidth);
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setStrokeCap(Paint.Cap.ROUND);

        mTogglePaint = new Paint();
        mTogglePaint.setStyle(Paint.Style.STROKE);
        mTogglePaint.setStrokeWidth(mToggleLineWidth);
        mTogglePaint.setColor(Color.WHITE);
        mTogglePaint.setShadowLayer(mToggleLineWidth, 0, 0, getContext().getResources().getColor(R.color.textColorDarkGray));
        mTogglePaint.setAntiAlias(true);

        setLayerType(LAYER_TYPE_SOFTWARE, mTogglePaint);

        mGrayPaint = new Paint();
        mGrayPaint.setStyle(Paint.Style.STROKE);
        mGrayPaint.setStrokeWidth(mLineWidth / 10);
        mGrayPaint.setColor(Color.LTGRAY);
        mGrayPaint.setAntiAlias(true);
        mGrayPaint.setShadowLayer(mLineWidth / 4, 0, 0, getContext().getResources().getColor(R.color.textColorDarkGray));

        setLayerType(LAYER_TYPE_SOFTWARE, mGrayPaint);

        mWhitePaint = new Paint();
        mWhitePaint.setStyle(Paint.Style.FILL);
        mWhitePaint.setColor(getContext().getResources().getColor(R.color.colorWhite));
        mWhitePaint.setAntiAlias(true);

        gradients = new ArrayList<>();
        gradients.add(new LinearGradient(w - mPadding, h / 2, w / 2, h - mPadding, COLORS[0], COLORS[1], Shader.TileMode.CLAMP));
        gradients.add(new LinearGradient(w / 2, h - mPadding, mPadding, h / 2, COLORS[2], COLORS[3], Shader.TileMode.CLAMP));
        gradients.add(new LinearGradient(mPadding, h / 2, w / 2, mPadding, COLORS[3], COLORS[4], Shader.TileMode.CLAMP));
        gradients.add(new LinearGradient(w / 2, mPadding, w - mPadding, h / 2, COLORS[4], COLORS[0], Shader.TileMode.CLAMP));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getMeasuredWidth();
        int h = getMeasuredHeight();

        canvas.drawCircle(w / 2, h / 2, w / 2 - mPadding - mLineWidth / 2, mWhitePaint);
        canvas.drawCircle(w / 2, h / 2, w / 2 - mPadding - mLineWidth / 2, mGrayPaint);

        mCirclePaint.setShader(gradients.get(0));
        canvas.drawArc(mFitrateRect, 0, 70, false, mCirclePaint);

        mCirclePaint.setShader(gradients.get(1));
        canvas.drawArc(mFitrateRect, 110, 70, false, mCirclePaint);

        mCirclePaint.setShader(gradients.get(2));
        canvas.drawArc(mFitrateRect, 180, 90, false, mCirclePaint);

        mCirclePaint.setShader(gradients.get(3));
        canvas.drawArc(mFitrateRect, 270, 90, false, mCirclePaint);

        float toggleX = mPadding + (w - 2 * mPadding) / 2 + (w - 2 * mPadding) / 2 * (float) Math.cos(Math.toRadians(mFitrateAngle));
        float toggleY = mPadding + (h - 2 * mPadding) / 2 + (h - 2 * mPadding) / 2 * (float) Math.sin(Math.toRadians(mFitrateAngle));
        canvas.drawCircle(toggleX, toggleY, mToggleRadius, mTogglePaint);
    }
}
