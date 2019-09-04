package com.tryfit.fittings;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.tryfit.R;

import java.util.List;

/**
 * Created by alexeyreznik on 28/06/2017.
 */

public class ImageViewPagerAdapter extends PagerAdapter {

    private static final String TAG = ImageViewPagerAdapter.class.getSimpleName();
    private static final String IMAGE_URL_PREFIX = "https://demo.try.fit/resizer?w=";

    private final List<String> mImageUrls;
    private final int mImageWidth;
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private View.OnClickListener mListener;

    ImageViewPagerAdapter(Context context, List<String> imageUrls, int width, View.OnClickListener listener) {
        mContext = context;
        mImageUrls = imageUrls;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mListener = listener;
        mImageWidth = width;
    }

    @Override
    public int getCount() {
        return mImageUrls.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = mLayoutInflater.inflate(R.layout.fittings_recycler_view_item_viewpager_item, container, false);

        final ImageView imageView = itemView.findViewById(R.id.cart_recycler_view_item_image);
        final ProgressBar loading = itemView.findViewById(R.id.loading);
        final ImageView error = itemView.findViewById(R.id.error);
        String imageUrl = IMAGE_URL_PREFIX + mImageWidth + "&s=" + mImageUrls.get(position);
        Picasso.with(mContext)
                .load(imageUrl)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        loading.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {
                        loading.setVisibility(View.GONE);
                        error.setVisibility(View.VISIBLE);
                        Log.e(TAG, "Error");
                    }
                });

        imageView.setOnClickListener(mListener);
        error.setOnClickListener(mListener);
        container.addView(itemView);
        return itemView;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((FrameLayout) object);
    }
}
