package com.tryfit.camera;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.crashlytics.android.Crashlytics;
import com.tryfit.R;


import io.fabric.sdk.android.Fabric;

public class InstructionsActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = InstructionsActivity.class.getSimpleName();
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private TabLayout tabLayout;
    private Button nextButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_instructions);


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


                if (position == mViewPager.getAdapter().getCount() - 2) {
                    nextButton.setText(R.string.begin);
                    nextButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                } else {
                    nextButton.setText(R.string.action_next);
                    nextButton.setCompoundDrawablesWithIntrinsicBounds(null, null, InstructionsActivity.this.getResources().getDrawable(R.drawable.ic_arrow_forward_black_24dp), null);
                }

                if (position == mViewPager.getAdapter().getCount() - 1) {
                    finish();
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

        Fabric.with(this, new Crashlytics());
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.view_pager + ":" + mViewPager.getCurrentItem());
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
                    instructions = getResources().getString(R.string.my_instruction1);
                    assetPath = "file:///android_asset/step1.jpg";
                    fragment = ImageAndTextFragment.newInstance(instructions, assetPath);
                    break;
                case 1:
                    instructions = getResources().getString(R.string.my_instruction2);
                    assetPath = "file:///android_asset/step2.jpg";
                    fragment = ImageAndTextFragment.newInstance(instructions, assetPath);
                    break;
                case 2:
                    instructions = getResources().getString(R.string.my_instruction3);
                    assetPath = "file:///android_asset/step3.jpg";
                    fragment = ImageAndTextFragment.newInstance(instructions, assetPath);
                    break;
                case 3:
                    instructions = getResources().getString(R.string.my_instruction4);
                    assetPath = "file:///android_asset/step4.jpg";
                    fragment = ImageAndTextFragment.newInstance(instructions, assetPath);
                    break;
                case 4:
                    instructions = getResources().getString(R.string.my_instruction4);
                    assetPath = "file:///android_asset/step4.jpg";
                    fragment = ImageAndTextFragment.newInstance(instructions, assetPath);
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
