package com.tryfit;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tryfit.common.OnFragmentInteractionListener;
import com.tryfit.common.db.RealmHelper;
import com.tryfit.common.db.models.Client;
import com.tryfit.common.rest.GraphQLRequest;
import com.tryfit.common.rest.GraphQLRequestBuilder;
import com.tryfit.common.rest.TryFitWebServiceProvider;
import com.tryfit.fittings.FittingsFragment;
import com.tryfit.fittings.FittingsItemFragment;
import com.tryfit.profile.ProfileFragment;
import com.tryfit.qrcode.QRCodeFragment;
import com.tryfit.scanning.ScannerActivity;
import com.tryfit.scans.FullscreenModelFragment;
import com.tryfit.scans.ScansFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements OnFragmentInteractionListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.tabBar)
    LinearLayout mTabBar;
    @BindView(R.id.tabScansImage)
    ImageView mTabScansImage;
    @BindView(R.id.tabScansText)
    TextView mTabScansText;
    @BindView(R.id.tabFittingsImage)
    ImageView mTabFittingsImage;
    @BindView(R.id.tabFittingsText)
    TextView mTabFittingsText;
    @BindView(R.id.tabProfileImage)
    ImageView mTabProfileImage;
    @BindView(R.id.tabProfileText)
    TextView mTabProfileText;
    @BindView(R.id.tabQRImage)
    ImageView mTabQrImage;
    @BindView(R.id.tabQRText)
    TextView mTabQRText;
    @BindView(R.id.fragment_container)
    FrameLayout mMainContainer;

    private TABS mSelectedTab = TABS.None;

    private int mColorAccent;
    private int mColorOutline;

    public enum TABS {
        None,
        Scans,
        Fittings,
        Profile,
        QRCode
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.textColorAppBar));
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        mColorAccent = getResources().getColor(R.color.colorAccent);
        mColorOutline = getResources().getColor(R.color.colorOutline);

        selectScans();
    }

    @OnClick(R.id.tabScans)
    void selectScans() {
        if (mSelectedTab != TABS.Scans) {
            changeFragment(ScansFragment.newInstance(), true);
            selectTab(TABS.Scans);
        }
    }

    @OnClick(R.id.tabFittings)
    void selectFittings() {
        if (mSelectedTab != TABS.Fittings) {
            changeFragment(FittingsFragment.newInstance(), true);
            selectTab(TABS.Fittings);
        }
    }

    @OnClick(R.id.tabProfile)
    void selectProfile() {
        if (mSelectedTab != TABS.Profile) {
            changeFragment(ProfileFragment.newInstance(), true);
            selectTab(TABS.Profile);
        }
    }

    @OnClick(R.id.tabQR)
    void selectQRCode() {
        if (mSelectedTab != TABS.QRCode) {
            changeFragment(QRCodeFragment.newInstance(), true);
            selectTab(TABS.QRCode);
        }
    }

    @Override
    public void onFragmentInteraction(int resourceId, Object... args) {
        switch (resourceId) {
            case R.id.gl_surface_container:
                changeFragment(FullscreenModelFragment.newInstance(), false);
                selectTab(TABS.None);
                break;
            case R.id.gl_surface_fullscreen:
                changeFragment(ScansFragment.newInstance(), true);
                selectScans();
                break;
            case R.id.recycler_view:
                changeFragment(FittingsItemFragment.newInstance((int) args[0]), false);
                selectTab(TABS.None);
                break;
            case R.id.action_menu_new_3d_scan:
                Intent intent = new Intent(this, ScannerActivity.class);
                startActivity(intent);
                break;
            case R.id.scanner_view_container:
                loginInPlugin((String) args[0]);
                break;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
        if (backStackEntryCount == 1) {
            new AlertDialog.Builder(this)
                    .setMessage("Are you sure you want to exit TryFit?")
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            finish();
                        }
                    })
                    .setCancelable(false)
                    .create()
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void changeFragment(Fragment fragment, boolean clearStack) {
        if (clearStack) {
            clearBackStack();
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .setCustomAnimations(R.anim.abc_fade_in, R.anim.abc_fade_out, R.anim.abc_popup_enter, R.anim.abc_popup_exit)
                .commitAllowingStateLoss();
    }

    public void selectTab(TABS tab) {
        if (tab != TABS.None) {
            mTabScansImage.setColorFilter(null);
            mTabScansText.setTextColor(mColorOutline);
            mTabFittingsImage.setColorFilter(null);
            mTabFittingsText.setTextColor(mColorOutline);
            mTabProfileImage.setColorFilter(null);
            mTabProfileText.setTextColor(mColorOutline);
            mTabQrImage.setColorFilter(null);
            mTabQRText.setTextColor(mColorOutline);
            switch (tab) {
                case Scans:
                    mTabScansImage.setColorFilter(mColorAccent);
                    mTabScansText.setTextColor(mColorAccent);
                    break;
                case Fittings:
                    mTabFittingsImage.setColorFilter(mColorAccent);
                    mTabFittingsText.setTextColor(mColorAccent);
                    break;
                case Profile:
                    mTabProfileImage.setColorFilter(mColorAccent);
                    mTabProfileText.setTextColor(mColorAccent);
                    break;
                case QRCode:
                    mTabQrImage.setColorFilter(mColorAccent);
                    mTabQRText.setTextColor(mColorAccent);
                    break;
            }
            mTabBar.setVisibility(View.VISIBLE);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(false);
                actionBar.setDisplayShowHomeEnabled(false);
            }
        } else {
            mTabBar.setVisibility(View.GONE);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowHomeEnabled(true);
            }
        }
        mSelectedTab = tab;
    }

    private void clearBackStack() {
        FragmentManager fm = getSupportFragmentManager();
        for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
        }
    }

    private void loginInPlugin(String qrCode) {
        String searchPattern = "try.fit?l";
        int idx = qrCode.indexOf(searchPattern) + 1;
        String sid = qrCode.substring(idx + searchPattern.length());

        Realm realm = Realm.getDefaultInstance();
        Client client = RealmHelper.getCurrentClient(realm);
        final String clientId = (client != null) ? client.getId() : null;
        realm.close();

        Log.d(TAG, "clientId: " + clientId + " sid: " + sid);
        GraphQLRequest request = GraphQLRequestBuilder.buildLoginInPluginRequest(clientId, sid);
        TryFitWebServiceProvider.getInstance().loginInPlugin(request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, t.getLocalizedMessage());
            }
        });
    }
}
