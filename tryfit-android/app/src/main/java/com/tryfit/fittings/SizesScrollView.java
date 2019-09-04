package com.tryfit.fittings;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tryfit.R;
import com.tryfit.common.db.models.Size;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by alexeyreznik on 12/07/2017.
 */

public class SizesScrollView extends LinearLayout {
    private Context mContext;
    private HorizontalScrollView mSizesScroll;
    private LinearLayout mSizesBar;
    private View selectedSizeItem;

    private OnSizeSelected mListener;

    public SizesScrollView(Context context) {
        super(context);
        this.mContext = context;
        init();
    }

    public SizesScrollView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init();
    }

    public SizesScrollView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init();
    }

    public SizesScrollView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mContext = context;
        init();
    }

    public void setOnSizeSelectedListener(OnSizeSelected listener) {
        this.mListener = listener;
    }

    private void init() {
        LayoutInflater.from(mContext).inflate(R.layout.sizes_scroll_view_layout, this, true);
        mSizesScroll = (HorizontalScrollView) findViewById(R.id.size_scroll_container);
        mSizesScroll.setSmoothScrollingEnabled(true);
        mSizesBar = new LinearLayout(mContext);
        mSizesBar.setOrientation(LinearLayout.HORIZONTAL);
        mSizesBar.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mSizesBar.setPadding(getResources().getDisplayMetrics().widthPixels / 2, 0, getResources().getDisplayMetrics().widthPixels / 2, 0);
        mSizesBar.setGravity(Gravity.CENTER);
        mSizesScroll.addView(mSizesBar);
    }

    public void populateSizes(final List<Size> sizesArray, final float initialSize) {
        if (sizesArray != null && sizesArray.size() > 0) {
            int bestSizeIndex = 0;
            double bestSizeFitrate = Math.abs(sizesArray.get(0).getFitrate());
            for (int i = 0; i < sizesArray.size(); i++) {
                Size size = sizesArray.get(i);
                if (Math.abs(size.getFitrate()) > bestSizeFitrate) {
                    bestSizeFitrate = Math.abs(size.getFitrate());
                    bestSizeIndex = i;
                }
            }
            for (int i = 0; i < sizesArray.size(); i++) {
                View sizeItem = ((Activity) mContext).getLayoutInflater().inflate(R.layout.fittings_item_size_item, null);
                sizeItem.setTag(i);
                final Size size = sizesArray.get(i);
                ((TextView) sizeItem.findViewById(R.id.size_item_size)).setText(String.valueOf(size.getValue()));
                double fitrate = Math.abs(size.getFitrate());
                DecimalFormat df = new DecimalFormat("0.0");
                df.setRoundingMode(RoundingMode.DOWN);
                ((TextView) sizeItem.findViewById(R.id.size_item_fitrate)).setText(df.format(fitrate));
                ImageView fitrateRange = (ImageView) sizeItem.findViewById(R.id.size_item_indicator);
                if (i == bestSizeIndex) {
                    fitrateRange.setImageDrawable(getResources().getDrawable(R.drawable.ic_star_black_24dp));
                    fitrateRange.setColorFilter(getResources().getColor(R.color.colorFitRateBest));
                } else if (fitrate >= 8.5) {
                    fitrateRange.setColorFilter(getResources().getColor(R.color.colorFitRateBest));
                } else if (fitrate < 8.5 && fitrate >= 7.5) {
                    fitrateRange.setColorFilter(getResources().getColor(R.color.colorFitRateGood));
                } else if (fitrate < 7.5 && fitrate >= 6.5) {
                    if (size.getFitrate() < 0) {
                        fitrateRange.setColorFilter(getResources().getColor(R.color.colorFitRateAverage));
                    } else {
                        fitrateRange.setColorFilter(getResources().getColor(R.color.colorFitRateAverageLoose));
                    }
                } else {
                    if (size.getFitrate() < 0) {
                        fitrateRange.setColorFilter(getResources().getColor(R.color.colorFitRateBad));
                    } else {
                        fitrateRange.setColorFilter(getResources().getColor(R.color.colorFitRateBadLoose));
                    }
                }
                sizeItem.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (selectedSizeItem != null) {
                            selectedSizeItem.setBackgroundColor(getResources().getColor(R.color.colorBackground));
                        }
                        selectedSizeItem = view;
                        view.setBackground(getResources().getDrawable(R.drawable.size_item_selected_background));
                        int screenWidth = getResources().getDisplayMetrics().widthPixels;

                        int scrollX = (view.getLeft() - (screenWidth / 2))
                                + (view.getWidth() / 2);
                        ObjectAnimator animator = ObjectAnimator.ofInt(mSizesScroll, "scrollX", scrollX).setDuration(300);
                        animator.setInterpolator(new LinearInterpolator());
                        animator.start();
                        if (mListener != null) {
                            mListener.onSizeSelected(size);
                        }
                    }
                });
                mSizesBar.addView(sizeItem);
            }
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    int index = -1;
                    for (int i = 0; i < sizesArray.size(); i++) {
                        Size size = sizesArray.get(i);
                        if (size.getValue() == initialSize) {
                            index = i;
                        }
                    }
                    if (index >= 0) {
                        if (selectedSizeItem != null) {
                            selectedSizeItem.setBackgroundColor(getResources().getColor(R.color.colorBackground));
                        }
                        selectedSizeItem = mSizesBar.getChildAt(index);
                        mSizesBar.getChildAt(index).setBackground(getResources().getDrawable(R.drawable.size_item_selected_background));
                        int screenWidth = getResources().getDisplayMetrics().widthPixels;

                        int scrollX = (mSizesBar.getChildAt(index).getLeft() - (screenWidth / 2))
                                + (mSizesBar.getChildAt(index).getWidth() / 2);
                        ObjectAnimator animator = ObjectAnimator.ofInt(mSizesScroll, "scrollX", scrollX).setDuration(300);
                        animator.setInterpolator(new LinearInterpolator());
                        animator.start();
                        if (mListener != null) {
                            mListener.onSizeSelected(sizesArray.get(index));
                        }
                    }
                }
            }, 250);
        }
    }

    public interface OnSizeSelected {
        void onSizeSelected(Size size);
    }

}
