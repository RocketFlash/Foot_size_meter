package com.tryfit.scanning;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.tryfit.R;
import com.tryfit.common.OnFragmentInteractionListener;
import com.tryfit.common.db.RealmHelper;
import com.tryfit.common.db.models.Client;
import com.tryfit.common.db.models.FITModel;
import com.tryfit.common.db.models.Scan;
import com.tryfit.common.parsers.FITParser;
import com.tryfit.common.parsers.ScanParser;
import com.tryfit.common.train.ICallback;
import com.tryfit.common.train.TrainHelper;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;

public class MakeScanFragment extends Fragment {

    private static final String TAG = MakeScanFragment.class.getSimpleName();

    @BindView(R.id.scanner_image)
    ImageView mScannerImage;

    ProgressDialog mProgressDialog;

    private OnFragmentInteractionListener mListener;

    interface OnClientCreatedListener {
        void onClientCreated(Client client);

        void onError(String message);
    }

    interface OnScanFinishedListener {
        void onScanFinished(Scan scan);

        void onError(String message);
    }

    interface OnModelsLoadedListener {
        void onModelLoaded(FITModel leftFootModel, FITModel rightFootModel);

        void onError(String message);
    }

    public MakeScanFragment() {
    }

    public static MakeScanFragment newInstance() {
        return new MakeScanFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage("Scanning...");
        mProgressDialog.setCancelable(false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_make_scan, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Picasso.with(getActivity()).load("file:///android_asset/scanner_active.png").into(mScannerImage);
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

    @OnClick(R.id.action_start_scan)
    void scan() {
        mProgressDialog.show();
        createNewClient(new OnClientCreatedListener() {
            @Override
            public void onClientCreated(final Client client) {

                client.setName("New User");
                startScan(client, new OnScanFinishedListener() {
                    @Override
                    public void onScanFinished(Scan scan) {

                        client.setScan(scan);
                        Realm realm = Realm.getDefaultInstance();
                        RealmHelper.saveCurrentClient(realm, client);
                        realm.close();

                        loadScanModels(scan, new OnModelsLoadedListener() {
                            @Override
                            public void onModelLoaded(FITModel leftFootModel, FITModel rightFootModel) {

                                Realm realm = Realm.getDefaultInstance();
                                RealmHelper.saveFootModel(realm, leftFootModel, RealmHelper.KEY_LEFT_FOOT_MODEL);
                                RealmHelper.saveFootModel(realm, rightFootModel, RealmHelper.KEY_RIGHT_FOOT_MODEL);
                                realm.close();

                                mProgressDialog.dismiss();
                                mListener.onFragmentInteraction(R.id.action_start_scan);
                            }

                            @Override
                            public void onError(String message) {
                                mProgressDialog.dismiss();
                                Toast.makeText(getActivity(), "Failed to load scan data", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onError(String message) {
                        mProgressDialog.dismiss();
                        Toast.makeText(getActivity(), "Failed to start new scan", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String message) {
                mProgressDialog.dismiss();
                Toast.makeText(getActivity(), "Failed to create new client", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadScanModels(Scan scan, final OnModelsLoadedListener listener) {
        TrainHelper.getInstance().getModelsFromScanner(scan.getClientID(), new ICallback() {
            @Override
            public void onResult(ArrayList args, String error) {
                if (error != null && !error.isEmpty()) {
                    Log.e(TAG, "loadScanModels " + error);
                    listener.onError(error);
                } else {
                    ByteArrayInputStream is = new ByteArrayInputStream((byte[]) args.get(1));
                    FITModel leftFootModel = FITParser.parse(is);
                    is = new ByteArrayInputStream((byte[]) args.get(2));
                    FITModel rightFootModel = FITParser.parse(is);
                    listener.onModelLoaded(leftFootModel, rightFootModel);
                }
            }
        });
    }

    private void startScan(Client client, final OnScanFinishedListener listener) {
        TrainHelper.getInstance().startScan(client.getId(), new ICallback() {
            @Override
            public void onResult(ArrayList args, String error) {
                if (error != null && !error.isEmpty()) {
                    Log.e(TAG, "startScan " + error);
                    listener.onError(error);
                } else {
                    final Scan scan = ScanParser.parseScan((JSONObject) args.get(0));
                    Log.d(TAG, "Scan: " + scan);
                    listener.onScanFinished(scan);
                }
            }
        });
    }

    public void createNewClient(final OnClientCreatedListener listener) {
        final Client newClient = new Client();
        JSONObject newClientJson = new JSONObject(newClient.toMap());

        TrainHelper.getInstance().createClient(newClientJson, new ICallback() {
            @Override
            public void onResult(ArrayList args, String error) {
                if (error != null && !error.isEmpty()) {
                    Log.e(TAG, "createNewClient " + error);
                    listener.onError(error);
                } else {
                    newClient.setId((String) args.get(0));
                    listener.onClientCreated(newClient);
                }
            }
        });
    }
}
