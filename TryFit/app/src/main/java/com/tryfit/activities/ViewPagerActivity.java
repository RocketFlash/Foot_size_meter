package com.tryfit.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.tryfit.tryfitlib.TryFitLibResult;
import com.tryfit.fragments.CameraFragment;
import com.tryfit.fragments.ImageAndTextFragment;
import com.tryfit.fragments.LogoAndTextFragment;
import com.tryfit.fragments.ResultFragment;
import com.tryfit.listeners.OnInteractionListener;
import com.tryfit.tryfit.R;

public class ViewPagerActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private TabLayout tabLayout;
    private Button nextButton;

    private CameraFragment cameraFragment;
    private ResultFragment resultFragment;

    private final OnInteractionListener mCameraFragmentOnInteractionListener = new OnInteractionListener() {
        @Override
        public void onInteraction(Object arg) {

            TryFitLibResult tryFitReturn = (TryFitLibResult) arg;
            resultFragment.setData(tryFitReturn);

            mViewPager.setCurrentItem(4);

        }
    };

    private final OnInteractionListener mResultFragmentInteractionListener = new OnInteractionListener() {
        @Override
        public void onInteraction(Object arg) {

            int viewId = (int) arg;

            if (viewId == R.id.container_inactive_right || viewId == R.id.rescan_right) {

                cameraFragment.setFoot("right");
            } else if (viewId == R.id.container_inactive_left || viewId == R.id.rescan_left ){

                cameraFragment.setFoot("left");
            }

            mViewPager.setCurrentItem(3);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_view_pager);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        tabLayout = (TabLayout) findViewById(R.id.tabDots);
        tabLayout.setupWithViewPager(mViewPager, true);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                if (position >= 3) {

                    findViewById(R.id.tabLayout).setVisibility(View.GONE);
                } else {

                    findViewById(R.id.tabLayout).setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        nextButton = (Button) findViewById(R.id.action_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
            }
        });

        cameraFragment = CameraFragment.newInstance();
        cameraFragment.setOnInteractionListener(mCameraFragmentOnInteractionListener);

        resultFragment = ResultFragment.newInstance();
        resultFragment.setOnInteractionListener(mResultFragmentInteractionListener);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            Fragment fragment = null;

            switch (position) {

                case 0:

                    fragment = LogoAndTextFragment.newInstance(position + 1);
                    break;
                case 1:

                    fragment = ImageAndTextFragment.newInstance(position + 1);
                    break;
                case 2:

                    fragment = ImageAndTextFragment.newInstance(position + 1);
                    break;
                case 3:

                    cameraFragment.setFoot("left");
                    fragment = cameraFragment;
                    break;

                case 4:

                    fragment = resultFragment;
                    break;
            }

            return fragment;
        }

        @Override
        public int getCount() {
            return 5;
        }


    }
}
