package com.tryfit.scanning;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.tryfit.MainActivity;
import com.tryfit.R;
import com.tryfit.common.OnFragmentInteractionListener;
import com.tryfit.common.train.OnTrainConnectedListener;
import com.tryfit.common.train.TrainHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ScannerActivity extends AppCompatActivity implements OnFragmentInteractionListener {

    private static final String TAG = ScannerActivity.class.getSimpleName();

    @BindView(R.id.loading)
    ProgressBar mLoading;
    @BindView(R.id.no_connection)
    RelativeLayout mNoConnection;
    @BindView(R.id.fragment_container)
    FrameLayout mMainContainer;

    private boolean mEmulator = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        ButterKnife.bind(this);

        if (getIntent().getExtras() != null) {
            mEmulator = getIntent().getExtras().getBoolean("EMULATOR");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!TrainHelper.getInstance().isTrainSessionOpen()) {
            connectTrain();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (TrainHelper.getInstance().isTrainSessionOpen()) {
            TrainHelper.getInstance().closeTrainSession();
        }
    }

    private void connectTrain() {
        mNoConnection.setVisibility(View.GONE);
        mMainContainer.setVisibility(View.GONE);
        mLoading.setVisibility(View.VISIBLE);

        String trainSessionUrl;
        if (!mEmulator) {
            trainSessionUrl = TrainHelper.TRAIN_SCANNER_URL;
        } else {
            trainSessionUrl = TrainHelper.TRAIN_TEST_SERVER_URL;
        }
        TrainHelper.getInstance().initTrainSession(trainSessionUrl,
                null, new OnTrainConnectedListener() {
                    @Override
                    public void onTrainConnected() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mLoading.setVisibility(View.GONE);
                                mNoConnection.setVisibility(View.GONE);
                                mMainContainer.setVisibility(View.VISIBLE);
                                changeFragment(MakeScanFragment.newInstance());
                            }
                        });
                    }

                    @Override
                    public void onError(String message) {
                        Log.e(TAG, "Failed to connect Train: " + message);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mLoading.setVisibility(View.GONE);
                                mMainContainer.setVisibility(View.GONE);
                                mNoConnection.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                });
    }

    private void changeFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .setCustomAnimations(R.anim.abc_fade_in, R.anim.abc_fade_out, R.anim.abc_popup_enter, R.anim.abc_popup_exit)
                .commitAllowingStateLoss();
    }

    @Override
    public void onFragmentInteraction(int resourceId, Object... args) {
        switch (resourceId) {
            case R.id.action_start_scan:
                TrainHelper.getInstance().closeTrainSession();

                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                break;
        }
    }

    @OnClick(R.id.action_reconnect)
    public void reconnect() {
        connectTrain();
    }
}
