package com.tryfit.qrcode;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.zxing.Result;
import com.tryfit.MainActivity;
import com.tryfit.R;
import com.tryfit.common.OnFragmentInteractionListener;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class QRCodeFragment extends Fragment implements ZXingScannerView.ResultHandler {

    private static final String TAG = QRCodeFragment.class.getSimpleName();
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 0;

    private OnFragmentInteractionListener mListener;
    private ZXingScannerView mScannerView;

    public QRCodeFragment() {
    }

    public static QRCodeFragment newInstance() {
        return new QRCodeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_qrcode, container, false);
        mScannerView = new ZXingScannerView(getActivity());
        FrameLayout viewContainer = rootView.findViewById(R.id.scanner_view_container);
        viewContainer.addView(mScannerView, 0);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (checkCameraPermission()) {
            mScannerView.setResultHandler(this);
            mScannerView.startCamera();
        }
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

    @Override
    public void onResume() {
        super.onResume();
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) actionBar.setTitle(getString(R.string.qr_code));
        ((MainActivity) getActivity()).selectTab(MainActivity.TABS.QRCode);
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult");
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mScannerView.setResultHandler(this);
                    mScannerView.startCamera();
                } else {
                    Toast.makeText(getActivity(), "Permission to use camera denied!", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);
            return false;
        } else {
            return true;
        }

    }

    @Override
    public void handleResult(Result result) {
        if (result != null) {
            mListener.onFragmentInteraction(R.id.scanner_view_container, result.getText());
        }
        mScannerView.startCamera();
    }
}
