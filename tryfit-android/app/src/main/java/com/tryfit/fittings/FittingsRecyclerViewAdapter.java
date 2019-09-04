package com.tryfit.fittings;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tryfit.R;
import com.tryfit.common.db.models.FittingItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexeyreznik on 26/06/2017.
 */

public class FittingsRecyclerViewAdapter extends RecyclerView.Adapter<FittingsRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = FittingsRecyclerViewAdapter.class.getSimpleName();
    private final static int IMAGE_WIDTH_LIST = 600;
    private final static int IMAGE_WIDTH_GRID = 300;

    private final Context mContext;
    private final List<FittingItem> mItems;
    private final OnRecyclerItemClickListener mListener;
    private final int mLayoutResId;


    public FittingsRecyclerViewAdapter(Context mContext, int layoutResId, OnRecyclerItemClickListener listener) {
        this.mContext = mContext;
        this.mListener = listener;
        this.mLayoutResId = layoutResId;
        this.mItems = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(mLayoutResId, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        FittingItem item = mItems.get(position);
        holder.model.setText(item.getProduct().getName());
        holder.code.setText(item.getProduct().getCode());
        holder.fitrate.setText(String.valueOf(item.getSize().getFitrateAbs()));
        holder.size.setText(String.valueOf(item.getSize().getValue()));
        int imageWidth;
        if (mLayoutResId == R.layout.fittings_recycler_view_item_layout_list) {
            imageWidth = IMAGE_WIDTH_LIST;
        } else {
            imageWidth = IMAGE_WIDTH_GRID;
        }
        if (item.getProduct().getPictures() != null && item.getProduct().getPictures().size() > 0) {
            holder.imagePager.setAdapter(new ImageViewPagerAdapter(mContext, item.getProduct().getPictures(), imageWidth, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onRecyclerItemClicked(holder.getAdapterPosition());
                }
            }));
        }
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onRecyclerItemClicked(holder.getAdapterPosition());
            }
        });
        holder.tabDots.setupWithViewPager(holder.imagePager);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void setItems(List<FittingItem> items) {
        mItems.clear();
        mItems.addAll(items);
        notifyDataSetChanged();
    }

    public void addItems(List<FittingItem> items) {
        int initialSize = mItems.size();
        mItems.clear();
        mItems.addAll(items);
        int updatedSize = mItems.size();
        notifyItemRangeInserted(initialSize, updatedSize);
    }

    public List<FittingItem> getItems() {
        return mItems;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CardView card;
        public TextView model;
        public TextView code;
        public TextView fitrate;
        public TextView size;
        public ViewPager imagePager;
        public TabLayout tabDots;

        public ViewHolder(View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cart_recycler_view_item_cardview);
            model = itemView.findViewById(R.id.cart_recycler_view_item_model);
            code = itemView.findViewById(R.id.cart_recycler_view_item_code);
            fitrate = itemView.findViewById(R.id.cart_recycler_view_item_fitrate);
            size = itemView.findViewById(R.id.cart_recycler_view_item_size);
            imagePager = itemView.findViewById(R.id.cart_recycler_view_item_viewpager);
            tabDots = itemView.findViewById(R.id.cart_recycler_view_item_tabs);
        }
    }
}
