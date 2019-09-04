package com.tryfit.scans;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tryfit.R;
import com.tryfit.camera.PaperDetectionActivity;
import com.tryfit.camera.TryFitCameraActivity;
import com.tryfit.common.OnFragmentInteractionListener;
import com.tryfit.common.db.RealmHelper;
import com.tryfit.common.db.models.Client;
import com.tryfit.common.db.models.Scan;
import com.tryfit.common.rest.TryFitWebServiceProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Scan2DFragment extends Fragment {

    private static final String TAG = Scan2DFragment.class.getSimpleName();
    public static final int REQUEST_CODE_SCAN_USING_CAMERA = 0;

    @BindView(R.id.indicator_left)
    ImageView indicatorLeft;
    @BindView(R.id.indicator_right)
    ImageView indicatorRight;
    @BindView(R.id.stick_length_left)
    TextView stickLengthLeft;
    @BindView(R.id.stick_length_right)
    TextView stickLengthRight;
    @BindView(R.id.ball_width_left)
    TextView ballWidthLeft;
    @BindView(R.id.ball_width_right)
    TextView ballWidthRight;

    private OnFragmentInteractionListener mListener;

    public Scan2DFragment() {
    }

    public static Scan2DFragment newInstance() {
        return new Scan2DFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_scan_2d, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateViews();
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

    @OnClick(R.id.action_scan_left)
    public void scanLeft() {
        Intent intent = new Intent(getActivity(), TryFitCameraActivity.class);
        intent.putExtra(TryFitCameraActivity.EXTRA_FOOT, "left");
        startActivityForResult(intent, REQUEST_CODE_SCAN_USING_CAMERA);
    }

    @OnClick(R.id.action_scan_right)
    public void scanRight() {
        Intent intent = new Intent(getActivity(), TryFitCameraActivity.class);
        intent.putExtra(TryFitCameraActivity.EXTRA_FOOT, "right");
        startActivityForResult(intent, REQUEST_CODE_SCAN_USING_CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SCAN_USING_CAMERA:
                if (data != null) {
                    send2DScanData(data.getStringExtra(PaperDetectionActivity.EXTRA_FOOT), data.getFloatExtra(PaperDetectionActivity.EXTRA_STICK_LENGTH, 0f),
                            data.getFloatExtra(PaperDetectionActivity.EXTRA_BALL_WIDTH, 0f));
                }
                break;
        }
    }

    public void updateViews() {
        Realm realm = Realm.getDefaultInstance();
        Client client = RealmHelper.getCurrentClient(realm);
        if (client != null) {
            Scan scan2d = client.getScan2D();
            if (scan2d != null && scan2d.getLeftMeasures() != null &&
                    scan2d.getLeftMeasures().getStickLength() != 0. && scan2d.getLeftMeasures().getBallWidth() != 0.) {
                stickLengthLeft.setText(String.format(Locale.getDefault(), "%.1fmm", scan2d.getLeftMeasures().getStickLength()));
                ballWidthLeft.setText(String.format(Locale.getDefault(), "%.1fmm", scan2d.getLeftMeasures().getBallWidth()));
                indicatorLeft.setImageDrawable(getActivity().getDrawable(R.drawable.ic_check_black_24dp));
                indicatorLeft.setColorFilter(getActivity().getResources().getColor(R.color.indicatorGreen));
            } else {
                stickLengthLeft.setText("0mm");
                ballWidthLeft.setText("0mm");
                indicatorLeft.setImageDrawable(getActivity().getDrawable(R.drawable.ic_add_black_24dp));
                indicatorLeft.setColorFilter(getActivity().getResources().getColor(R.color.colorOutline));
            }

            if (scan2d != null && scan2d.getRightMeasures() != null &&
                    scan2d.getRightMeasures().getStickLength() != 0. && scan2d.getRightMeasures().getBallWidth() != 0.) {
                stickLengthRight.setText(String.format(Locale.getDefault(), "%.1fmm", scan2d.getRightMeasures().getStickLength()));
                ballWidthRight.setText(String.format(Locale.getDefault(), "%.1fmm", scan2d.getRightMeasures().getBallWidth()));
                indicatorRight.setImageDrawable(getActivity().getDrawable(R.drawable.ic_check_black_24dp));
                indicatorRight.setColorFilter(getActivity().getResources().getColor(R.color.indicatorGreen));
            } else {
                stickLengthRight.setText("0mm");
                ballWidthRight.setText("0mm");
                indicatorRight.setImageDrawable(getActivity().getDrawable(R.drawable.ic_add_black_24dp));
                indicatorRight.setColorFilter(getActivity().getResources().getColor(R.color.colorOutline));
            }
        } else {
            Log.e(TAG, "client is null");
        }
        realm.close();
    }

    private void send2DScanData(final String foot, final float stickLength, final float ballWidth) {
        Realm realm = Realm.getDefaultInstance();
        Client client = RealmHelper.getCurrentClient(realm);
        String clientId = client.getId();
        realm.close();

        try {
            JSONObject body = new JSONObject();
            JSONObject measuresObj = new JSONObject();
            measuresObj.put("stickLength", stickLength);
            measuresObj.put("ballWidth", ballWidth);
            if (foot.equals("left")) {
                body.put("leftMeasures", measuresObj);
            } else {
                body.put("rightMeasures", measuresObj);
            }

            TryFitWebServiceProvider.getInstance().sendFootData(clientId, body.toString()).enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getActivity(), "Saved", Toast.LENGTH_SHORT).show();
                        save2DScanData(foot, stickLength, ballWidth);
                        updateViews();
                    } else {
                        Log.e(TAG, "Response: " + response.code());
                        showError();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                    Log.e(TAG, "Response: " + t.getLocalizedMessage());
                    showError();
                }

                private void showError() {
                    Toast.makeText(getActivity(), "Failed to save 2D scan data", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    private void save2DScanData(String foot, float stickLength, float ballWidth) {
        Realm realm = Realm.getDefaultInstance();
        RealmHelper.saveScan2D(realm, foot, stickLength, ballWidth);
        realm.close();
    }
}
