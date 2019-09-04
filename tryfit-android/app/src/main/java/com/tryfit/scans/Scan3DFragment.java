package com.tryfit.scans;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tryfit.MainActivity;
import com.tryfit.R;
import com.tryfit.common.OnFragmentInteractionListener;
import com.tryfit.common.db.RealmHelper;
import com.tryfit.common.db.models.Client;
import com.tryfit.common.db.models.FITModel;
import com.tryfit.common.db.models.Measures;
import com.tryfit.common.db.models.Scan;
import com.tryfit.common.parsers.FITParser;
import com.tryfit.common.rest.ClientInfoResponse;
import com.tryfit.common.rest.GraphQLRequest;
import com.tryfit.common.rest.GraphQLRequestBuilder;
import com.tryfit.common.rest.TryFitWebServiceProvider;
import com.tryfit.common.utils.QRCodeParser;
import com.tryfit.common.utils.SharedPrefsHelper;
import com.tryfit.login.SignInActivity;
import com.tryfit.scans.opengl.Mesh;
import com.tryfit.scans.opengl.MyGLRenderer;
import com.tryfit.scans.opengl.MyGLSurfaceView;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Scan3DFragment extends Fragment {

    private static final String TAG = Scan3DFragment.class.getSimpleName();

    @BindView(R.id.stick_length_left)
    TextView mStickLengthLeft;
    @BindView(R.id.stick_length_right)
    TextView mStickLengthRight;
    @BindView(R.id.ball_width_left)
    TextView mBallWidthLeft;
    @BindView(R.id.ball_width_right)
    TextView mBallWidthRight;
    @BindView(R.id.heel_width_left)
    TextView mHeelWidthLeft;
    @BindView(R.id.heel_width_right)
    TextView mHeelWidthRight;
    @BindView(R.id.toe_height_left)
    TextView mToeHeightLeft;
    @BindView(R.id.toe_height_right)
    TextView mToeHeightRight;
    @BindView(R.id.ball_girth_left)
    TextView mBallGirthLeft;
    @BindView(R.id.ball_girth_right)
    TextView mBallGirthRight;
    @BindView(R.id.instep_girth_left)
    TextView mInstepGirthLeft;
    @BindView(R.id.instep_girth_right)
    TextView mInstepGirthRight;
    @BindView(R.id.heel_girth_left)
    TextView mHeelGirthLeft;
    @BindView(R.id.heel_girth_right)
    TextView mHeelGirthRight;
    @BindView(R.id.ankle_girth_left)
    TextView mAnkleGirthLeft;
    @BindView(R.id.ankle_girth_right)
    TextView mAnkleGirthRight;
    @BindView(R.id.calf_girth_left)
    TextView mCalfGirthLeft;
    @BindView(R.id.calf_girth_right)
    TextView mCalfGirthRight;
    @BindView(R.id.foot_measures_table)
    LinearLayout mMeasuresTable;
    @BindView(R.id.gl_surface_container)
    FrameLayout mGlSurfaceContainer;
    @BindView(R.id.loading)
    ProgressBar mLoading;
    @BindView(R.id.empty_scan)
    RelativeLayout mEmptyScanView;

    private OnFragmentInteractionListener mListener;
    private ProgressDialog mProgress;

    public Scan3DFragment() {
    }

    public static Scan3DFragment newInstance() {
        return new Scan3DFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProgress = new ProgressDialog(getActivity());
        mProgress.setMessage(getString(R.string.loading));
        mProgress.setCancelable(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_scans, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_menu_new_3d_scan:
                mListener.onFragmentInteraction(R.id.action_menu_new_3d_scan);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_scan_3d, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String accessToken = SharedPrefsHelper.getString(getActivity(), SharedPrefsHelper.SP_ACCESS_TOKEN, "");
        if (!accessToken.isEmpty()) {
            Realm realm = Realm.getDefaultInstance();
            Client client = RealmHelper.getCurrentClient(realm);
            final String clientId = (client != null) ? client.getId() : null;
            realm.close();
            if (clientId != null) {
                //Client info is already loaded
                updateViews();
            } else {
                //Load client info
                getClientInfo(accessToken);
            }
        } else {
            launchSignInActivity();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.scans));
        }

        ((MainActivity) getActivity()).selectTab(MainActivity.TABS.Scans);
    }

    @Override
    public void onPause() {
        super.onPause();
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void launchSignInActivity() {
        Intent intent = new Intent(getActivity(), SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void getClientInfo(String accessToken) {
        String clientId = QRCodeParser.parseClientId(getActivity(), accessToken);
        if (clientId != null) {
            mProgress.show();
            GraphQLRequest request = GraphQLRequestBuilder.buildGetClientInfoRequest(clientId);
            TryFitWebServiceProvider.getInstance().getClientInfo(request).enqueue(new Callback<ClientInfoResponse>() {
                @Override
                public void onResponse(@NonNull Call<ClientInfoResponse> call, @NonNull Response<ClientInfoResponse> response) {
                    if (response.isSuccessful()) {
                        ClientInfoResponse body = response.body();
                        if (body != null) {
                            Client client = body.getData().getClient();
                            Log.d(TAG, "Client: " + client);
                            Realm realm = Realm.getDefaultInstance();
                            RealmHelper.saveCurrentClient(realm, client);
                            realm.close();
                            updateViews();
                        } else {
                            Log.e(TAG, "Response body is null");
                            showError();
                        }
                    } else {
                        Log.e(TAG, "Code: " + response.code());
                        showError();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ClientInfoResponse> call, @NonNull Throwable t) {
                    Log.e(TAG, t.getLocalizedMessage());
                    showError();
                }

                void showError() {
                    Toast.makeText(getActivity(), "Failed to get client info", Toast.LENGTH_SHORT).show();
                    launchSignInActivity();
                }
            });
        } else {
            launchSignInActivity();
        }
    }

    private void updateViews() {
        mEmptyScanView.setVisibility(View.GONE);
        mMeasuresTable.setVisibility(View.GONE);

        Realm realm = Realm.getDefaultInstance();
        Client client = RealmHelper.getCurrentClient(realm);
        if (client != null) {
            Scan scan = client.getScan();
            if (scan != null) {

                Measures leftFootMeasures = scan.getLeftMeasures();
                Measures rightFootMeasures = scan.getRightMeasures();
                if (leftFootMeasures != null && rightFootMeasures != null) {
                    displayMeasurementsTable(leftFootMeasures, rightFootMeasures);
                } else {
                    Log.d(TAG, "Foot measures are null");
                }

                FITModel leftFootModel = RealmHelper.getFootModel(realm, RealmHelper.KEY_LEFT_FOOT_MODEL);
                FITModel rightFootModel = RealmHelper.getFootModel(realm, RealmHelper.KEY_RIGHT_FOOT_MODEL);

                if (leftFootModel != null && rightFootModel != null) {
                    mProgress.dismiss();
                    displayFootModels(leftFootModel, rightFootModel);
                } else {
                    Log.d(TAG, "Foot models are null. Loading");
                    loadFootModels(client.getId());
                }
            } else {
                Log.d(TAG, "scan is null");
                mEmptyScanView.setVisibility(View.VISIBLE);
                mProgress.dismiss();
            }
        } else {
            Log.e(TAG, "client is null");
            mProgress.dismiss();
        }
        realm.close();
    }

    private void displayFootModels(FITModel leftFootModel, FITModel rightFootModel) {
        MyGLSurfaceView mGlSurfaceView = new MyGLSurfaceView(getActivity());
        mGlSurfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onFragmentInteraction(R.id.gl_surface_container);
                }
            }
        });

        Mesh leftMesh = Mesh.fromRealmMesh(leftFootModel.getMeshes().get(0));
        Mesh rightMesh = Mesh.fromRealmMesh(rightFootModel.getMeshes().get(0));

        MyGLRenderer mRenderer = new MyGLRenderer(leftMesh, rightMesh);
        mGlSurfaceView.setEGLContextClientVersion(2);
        mGlSurfaceView.setZOrderOnTop(true);
        mGlSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        mGlSurfaceView.setRenderer(mRenderer);
        mGlSurfaceContainer.removeAllViews();
        mGlSurfaceContainer.addView(mGlSurfaceView);
    }

    private void displayMeasurementsTable(@NonNull Measures leftFootMeasures, @NonNull Measures rightFootMeasures) {
        mStickLengthLeft.setText(String.format(Locale.getDefault(), "%.1fmm", leftFootMeasures.getStickLength()));
        mBallWidthLeft.setText(String.format(Locale.getDefault(), "%.1fmm", leftFootMeasures.getBallWidth()));
        mHeelWidthLeft.setText(String.format(Locale.getDefault(), "%.1fmm", leftFootMeasures.getHeelWidth()));
        mToeHeightLeft.setText(String.format(Locale.getDefault(), "%.1fmm", leftFootMeasures.getToeHeight()));
        mBallGirthLeft.setText(String.format(Locale.getDefault(), "%.1fmm", leftFootMeasures.getBallGirth()));
        mInstepGirthLeft.setText(String.format(Locale.getDefault(), "%.1fmm", leftFootMeasures.getInstepGirth()));
        mHeelGirthLeft.setText(String.format(Locale.getDefault(), "%.1fmm", leftFootMeasures.getHeelGirth()));
        mAnkleGirthLeft.setText(String.format(Locale.getDefault(), "%.1fmm", leftFootMeasures.getAnkleGirth()));
        mCalfGirthLeft.setText(String.format(Locale.getDefault(), "%.1fmm", leftFootMeasures.getCalfGirth()));
        mStickLengthRight.setText(String.format(Locale.getDefault(), "%.1fmm", rightFootMeasures.getStickLength()));
        mBallWidthRight.setText(String.format(Locale.getDefault(), "%.1fmm", rightFootMeasures.getBallWidth()));
        mHeelWidthRight.setText(String.format(Locale.getDefault(), "%.1fmm", rightFootMeasures.getHeelWidth()));
        mToeHeightRight.setText(String.format(Locale.getDefault(), "%.1fmm", rightFootMeasures.getToeHeight()));
        mBallGirthRight.setText(String.format(Locale.getDefault(), "%.1fmm", rightFootMeasures.getBallGirth()));
        mInstepGirthRight.setText(String.format(Locale.getDefault(), "%.1fmm", rightFootMeasures.getInstepGirth()));
        mHeelGirthRight.setText(String.format(Locale.getDefault(), "%.1fmm", rightFootMeasures.getHeelGirth()));
        mAnkleGirthRight.setText(String.format(Locale.getDefault(), "%.1fmm", rightFootMeasures.getAnkleGirth()));
        mCalfGirthRight.setText(String.format(Locale.getDefault(), "%.1fmm", rightFootMeasures.getCalfGirth()));

        mMeasuresTable.setVisibility(View.VISIBLE);
    }

    private void loadFootModels(String clientId) {
        TryFitWebServiceProvider.getInstance().getModels(clientId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    if (body != null) {
                        try {
                            InputStream is = body.byteStream();
                            int size = is.available();
                            Log.d(TAG, "size: " + size);
                            final FITModel leftFootModel = FITParser.parse(is, size / 2);
                            final FITModel rightFootModel = FITParser.parse(is, size / 2);

                            if (leftFootModel != null && rightFootModel != null) {
                                Realm realm = Realm.getDefaultInstance();
                                RealmHelper.saveFootModel(realm, leftFootModel, RealmHelper.KEY_LEFT_FOOT_MODEL);
                                RealmHelper.saveFootModel(realm, rightFootModel, RealmHelper.KEY_RIGHT_FOOT_MODEL);
                                realm.close();

                                mProgress.dismiss();
                                displayFootModels(leftFootModel, rightFootModel);
                            } else {
                                Log.e(TAG, "Failed to parse FITModel");
                                showError();
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            showError();
                        }
                    } else {
                        Log.e(TAG, "Response body is null");
                        showError();
                    }
                } else {
                    Log.e(TAG, "Response: " + response.code());
                    showError();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, t.getLocalizedMessage());
                showError();
            }

            private void showError() {
                mProgress.dismiss();
                Toast.makeText(getActivity(), "Failed to load 3D model", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @OnClick(R.id.action_scan_using_scanner)
    void scanUsingScanner() {
        mListener.onFragmentInteraction(R.id.action_menu_new_3d_scan);
    }
}
