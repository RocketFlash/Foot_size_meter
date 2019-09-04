package com.tryfit.camera;


import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
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
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.tryfit.R;

import io.fabric.sdk.android.Fabric;

public class TryFitCameraActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = TryFitCameraActivity.class.getSimpleName();
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private TabLayout tabLayout;
    private Button nextButton;
    private Boolean firstTime = null;
    public static final String EXTRA_STICK_LENGTH = "stickLength";
    public static final String EXTRA_BALL_WIDTH = "ballWidth";
    public static final String EXTRA_FOOT = "foot";
    public String filename;

    private static final int REQUEST_CAMERA_STATE = 200;
    private static final int PICK_IMAGE_REQUEST = 0;
    private static final int GET_FOOT_PARAMS_RESULT = 999;
    private static final String READ_CAMERA_PERMISSION = Manifest.permission.CAMERA;

    private CameraFragment cameraFragment;
    private ResultFragment resultFragment;

    private String mFoot;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String foot = getIntent().getStringExtra(EXTRA_FOOT);
        if (foot == null) foot = "left";
        mFoot = foot;

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_instructions);

        if (!isPermissionGranted(READ_CAMERA_PERMISSION)) {
            requestPermission(READ_CAMERA_PERMISSION, REQUEST_CAMERA_STATE);
        } else {
            onCreateIfPermissionGranted();
        }

        Fabric.with(this, new Crashlytics());
    }

    public void onCreateIfPermissionGranted() {
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(5);

        if (!isFirstTime()) {
            mViewPager.setCurrentItem(4, true);
            findViewById(R.id.tabLayout).setVisibility(View.GONE);
        }

        tabLayout = (TabLayout) findViewById(R.id.tabDots);
        tabLayout.setupWithViewPager(mViewPager, true);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                if (position >= 4) {

                    findViewById(R.id.tabLayout).setVisibility(View.GONE);
                } else {

                    findViewById(R.id.tabLayout).setVisibility(View.VISIBLE);
                }


                if (position == mViewPager.getAdapter().getCount() - 2) {
                    nextButton.setText(R.string.begin);
                    nextButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                } else {
                    nextButton.setText(R.string.action_next);
                    nextButton.setCompoundDrawablesWithIntrinsicBounds(null, null, TryFitCameraActivity.this.getResources().getDrawable(R.drawable.ic_arrow_forward_black_24dp), null);
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
    }


    public void setFirstItemInActivity() {
        mViewPager.setCurrentItem(0, true);
    }

    public void setNextItemInActivity() {
        mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
    }

    public void setPreviousItemInActivity() {
        mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1, true);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CAMERA_STATE) {
            if (!isPermissionGranted(READ_CAMERA_PERMISSION)) {
                requestPermission(READ_CAMERA_PERMISSION, REQUEST_CAMERA_STATE);
            }
        }

        Fragment fragment = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.view_pager + ":" + mViewPager.getCurrentItem());
    }

    private boolean isPermissionGranted(String permission) {
        int permissionCheck = ActivityCompat.checkSelfPermission(this, permission);
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_STATE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Access approved", Toast.LENGTH_LONG).show();
                onCreateIfPermissionGranted();
            } else {
                Toast.makeText(this, "Access denied", Toast.LENGTH_LONG).show();
                showPermissionDialog(this);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void requestPermission(String permission, int requestCode) {
        ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
    }

    private void showPermissionDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String title = getResources().getString(R.string.app_name);
        builder.setTitle(title);
        builder.setMessage(title + " requires access to camera");

        String positiveText = "Settings";
        builder.setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                openAppSettings();
            }
        });

        String negativeText = "Exit";
        builder.setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void openAppSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, REQUEST_CAMERA_STATE);
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
                    if (cameraFragment == null) {
                        cameraFragment = CameraFragment.newInstance();
                    }
                    fragment = cameraFragment;
                    break;
                case 5:
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
            return 6;
        }
    }

    private boolean isFirstTime() {
        if (firstTime == null) {
            SharedPreferences mPreferences = this.getSharedPreferences("first_time", Context.MODE_PRIVATE);
            firstTime = mPreferences.getBoolean("firstTime", true);
            if (firstTime) {
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putBoolean("firstTime", false);
                editor.apply();
            }
        }
        return firstTime;
    }

    public void sendResults(float flength, float fwidth) {
        Intent dt = new Intent();
        dt.putExtra(EXTRA_FOOT, mFoot);
        dt.putExtra(EXTRA_STICK_LENGTH, flength);
        dt.putExtra(EXTRA_BALL_WIDTH, fwidth);
        setResult(RESULT_OK, dt);
        finish();
    }

}
