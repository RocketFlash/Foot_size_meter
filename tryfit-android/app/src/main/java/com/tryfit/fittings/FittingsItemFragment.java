package com.tryfit.fittings;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tryfit.MainActivity;
import com.tryfit.R;
import com.tryfit.common.OnFragmentInteractionListener;
import com.tryfit.common.db.models.FittingItem;
import com.tryfit.common.db.models.Size;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FittingsItemFragment extends Fragment {
    private static final String ARG_FITTINGS_ITEM_POSITION = "position";

    @BindView(R.id.image_viewpager)
    ViewPager mImageViewPager;
    @BindView(R.id.tabs)
    TabLayout mTabs;
    @BindView(R.id.model)
    TextView mModel;
    @BindView(R.id.code)
    TextView mCode;
    @BindView(R.id.price)
    TextView mPrice;
    @BindView(R.id.availability)
    TextView mAvailability;
    @BindView(R.id.fitrate_view)
    FitrateView mFitrateView;
    @BindView(R.id.sizes_scroll_view)
    SizesScrollView mSizesView;

    private static final int IMAGE_WIDTH = 1000;

    private FittingItem mItem;

    private OnFragmentInteractionListener mListener;
    private FittingItemsRepository mFittingItemsRepository;

    public FittingsItemFragment() {
    }

    public static FittingsItemFragment newInstance(int itemPosition) {
        FittingsItemFragment fragment = new FittingsItemFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_FITTINGS_ITEM_POSITION, itemPosition);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFittingItemsRepository = FittingItemsRepository.getInstance();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_fittings_item, container, false);
        ButterKnife.bind(this, rootView);

        int productPosition = getArguments().getInt(ARG_FITTINGS_ITEM_POSITION);
        mItem = mFittingItemsRepository.getItems().get(productPosition);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mItem != null) {
            mImageViewPager.setAdapter(new ImageViewPagerAdapter(getActivity(), mItem.getProduct().getPictures(), IMAGE_WIDTH, new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            }));
            mTabs.setupWithViewPager(mImageViewPager);

            mModel.setText(mItem.getProduct().getName());
            mCode.setText(mItem.getProduct().getCode());
            mPrice.setText(String.format(Locale.getDefault(), "%.1f", mItem.getProduct().getPrice()));

            displaySizes();
        }
    }

    private void displaySizes() {
        if (mItem.getProduct().getSizes() != null && mItem.getProduct().getSizes().size() > 0) {
            mSizesView.populateSizes(selectAvailableSizes(mItem.getProduct().getSizes()), mItem.getSize().getValue());
            mSizesView.setOnSizeSelectedListener(new SizesScrollView.OnSizeSelected() {
                @Override
                public void onSizeSelected(Size size) {
                    mFitrateView.setFitrateAnimated(size.getFitrate());
                }
            });
        }
    }

    private List<Size> selectAvailableSizes(List<Size> sizes) {
        List<Size> availableSizes = new ArrayList<>();
        for (Size size : sizes) {
//            if (size.getAvailable()) availableSizes.add(size);
            availableSizes.add(size);
        }
        return availableSizes;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            if (mItem != null) {
                actionBar.setTitle(mItem.getProduct().getName());
            } else {
                actionBar.setTitle(R.string.fittings);
            }
        }

        ((MainActivity) getActivity()).selectTab(MainActivity.TABS.None);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
