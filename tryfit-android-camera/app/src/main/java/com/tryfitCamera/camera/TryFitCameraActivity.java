package com.tryfitCamera.camera;

/**
 * Created by Rauf Yagfarov on 28/09/2017.
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.crashlytics.android.Crashlytics;
import com.tryfitCamera.camera.fragments.CameraFragment;
import com.tryfitCamera.camera.fragments.ImageAndTextFragment;
import com.tryfitCamera.camera.fragments.ResultFragment;
import com.tryfitCamera.camera.listeners.OnFragmentInteractionListener;
import com.tryfitCamera.tryfit.R;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

public class TryFitCameraActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, OnFragmentInteractionListener {

    private static final String TAG = TryFitCameraActivity.class.getSimpleName();

    static {
        System.loadLibrary("tryfit-lib");
    }

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private TabLayout tabLayout;
    private Button nextButton;
    private CameraFragment cameraFragment;
    private ResultFragment resultFragment;

    @Override
    public void onFragmentInteraction(int resourceId, Object... args) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_try_fit_camera);


        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
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

        Timber.plant(new Timber.DebugTree());
        Fabric.with(this, new Crashlytics());
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.view_pager + ":" + mViewPager.getCurrentItem());
        if (fragment != null && (fragment instanceof CameraFragment)) {
            ((CameraFragment) fragment).onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Fragment fragment = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.view_pager + ":" + mViewPager.getCurrentItem());
        if (fragment != null && (fragment instanceof CameraFragment)) {
            ((CameraFragment) fragment).onActivityResult(requestCode, resultCode, data);
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            Fragment fragment = null;
            String instructions;
            String assetPath;

            switch (position) {
                case 0:
                    instructions = getResources().getString(R.string.t_section_text_1);
                    assetPath = "file:///android_asset/t_instruction1.jpg";
                    fragment = ImageAndTextFragment.newInstance(instructions, assetPath);
                    break;
                case 1:
                    instructions = getResources().getString(R.string.t_section_text_2);
                    assetPath = "file:///android_asset/t_instruction2.jpg";
                    fragment = ImageAndTextFragment.newInstance(instructions, assetPath);
                    break;
                case 2:
                    instructions = getResources().getString(R.string.t_section_text_3);
                    assetPath = "file:///android_asset/t_instruction3.jpg";
                    fragment = ImageAndTextFragment.newInstance(instructions, assetPath);
                    break;
                case 3:
                    if (cameraFragment == null) {
                        cameraFragment = CameraFragment.newInstance();
                    }
                    fragment = cameraFragment;
                    break;

                case 4:
                    if (resultFragment == null) {
                        resultFragment = ResultFragment.newInstance();
                    }
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
