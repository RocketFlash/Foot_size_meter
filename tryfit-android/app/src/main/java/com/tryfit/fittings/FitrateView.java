package com.tryfit.fittings;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.tryfit.R;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Created by alexeyreznik on 30/06/2017.
 */

public class FitrateView extends FrameLayout {

    private static final String TAG = FitrateView.class.getSimpleName();
    TextView mFitrateText;
    TextView mFitrateRangeText;
    FitrateCircleView mFitrateCircle;

    float mFitrate = 10.0f;

    public FitrateView(@NonNull Context context) {
        super(context);
        inflate(context, R.layout.fitrate_view_layout, this);
        mFitrateText = (TextView) findViewById(R.id.fitrate);
        mFitrateRangeText = (TextView) findViewById(R.id.fitrate_range);
        mFitrateCircle = (FitrateCircleView) findViewById(R.id.fitrate_circle);
    }

    public FitrateView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.fitrate_view_layout, this);
        mFitrateText = (TextView) findViewById(R.id.fitrate);
        mFitrateRangeText = (TextView) findViewById(R.id.fitrate_range);
        mFitrateCircle = (FitrateCircleView) findViewById(R.id.fitrate_circle);
    }

    public FitrateView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.fitrate_view_layout, this);
        mFitrateText = (TextView) findViewById(R.id.fitrate);
        mFitrateRangeText = (TextView) findViewById(R.id.fitrate_range);
        mFitrateCircle = (FitrateCircleView) findViewById(R.id.fitrate_circle);
    }

    public FitrateView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        inflate(context, R.layout.fitrate_view_layout, this);
        mFitrateText = (TextView) findViewById(R.id.fitrate);
        mFitrateRangeText = (TextView) findViewById(R.id.fitrate_range);
        mFitrateCircle = (FitrateCircleView) findViewById(R.id.fitrate_circle);
    }

    public void setFitrate(float fitrate) {
        if (Math.abs(fitrate) >= 8.5) {
            mFitrateRangeText.setText(getContext().getString(R.string.fitrate_range_best_text));
        } else if (Math.abs(fitrate) < 8.5 && Math.abs(fitrate) >= 7.5) {
            mFitrateRangeText.setText(getContext().getString(R.string.fitrate_range_good_text));
        } else if (Math.abs(fitrate) < 7.5 && Math.abs(fitrate) >= 6.5) {
            mFitrateRangeText.setText(getContext().getString(R.string.fitrate_range_average_text));
        } else {
            mFitrateRangeText.setText(getContext().getString(R.string.fitrate_range_bad_text));
        }

        DecimalFormat df = new DecimalFormat("0.0");
        df.setRoundingMode(RoundingMode.DOWN);
        mFitrateText.setText(df.format(Math.abs(fitrate)));
        mFitrateCircle.setFitrate(fitrate);
        mFitrate = fitrate;
    }

    public void setFitrateAnimated(float finalFitrate) {
        if (Math.abs(finalFitrate) >= 8.5) {
            mFitrateRangeText.setText(getContext().getString(R.string.fitrate_range_best_text));
        } else if (Math.abs(finalFitrate) < 8.5 && Math.abs(finalFitrate) >= 7.5) {
            mFitrateRangeText.setText(getContext().getString(R.string.fitrate_range_good_text));
        } else if (Math.abs(finalFitrate) < 7.5 && Math.abs(finalFitrate) >= 6.5) {
            mFitrateRangeText.setText(getContext().getString(R.string.fitrate_range_average_text));
        } else {
            mFitrateRangeText.setText(getContext().getString(R.string.fitrate_range_bad_text));
        }

        int ANIMATION_DURATION = 300;
        int ANIMATION_CYCLES = 20;
        float initialFitrate = mFitrate;
        mFitrate = finalFitrate;
        for (int i = 1; i <= ANIMATION_CYCLES; i++) {
            final float fitrate;
            if (initialFitrate * finalFitrate > 0) {
                fitrate = initialFitrate + (finalFitrate - initialFitrate) * i / ANIMATION_CYCLES;
            } else {
                if (initialFitrate < 0) {
                    float diff = (10f + initialFitrate) + (10f - finalFitrate);
                    float tmp = initialFitrate - diff * i / ANIMATION_CYCLES;
                    if (tmp < -10.) {
                        fitrate = 20f + tmp;
                    } else {
                        if (tmp == -10f) {
                            tmp = -9.9f;
                        }
                        fitrate = tmp;
                    }
                } else {
                    float diff = (10f - initialFitrate) + (10f + finalFitrate);
                    float tmp = initialFitrate + diff * i / ANIMATION_CYCLES;
                    if (tmp > 10f) {
                        fitrate = -20f + tmp;
                    } else {
                        if (tmp == 10f) {
                            tmp = 9.9f;
                        }
                        fitrate = tmp;
                    }
                }
            }
            mFitrateCircle.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mFitrateCircle.setFitrate(fitrate);
                }
            }, ANIMATION_DURATION / ANIMATION_CYCLES * i);
            mFitrateText.postDelayed(new Runnable() {
                @Override
                public void run() {
                    DecimalFormat df = new DecimalFormat("0.0");
                    df.setRoundingMode(RoundingMode.DOWN);
                    mFitrateText.setText(df.format(Math.abs(fitrate)));
                }
            }, ANIMATION_DURATION / ANIMATION_CYCLES * i);
        }
    }

}
