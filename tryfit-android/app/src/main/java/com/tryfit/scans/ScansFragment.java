package com.tryfit.scans;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tryfit.MainActivity;
import com.tryfit.R;
import com.tryfit.common.OnFragmentInteractionListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ScansFragment extends Fragment {

    @BindView(R.id.action_3d_scan)
    TextView tv3DScan;
    @BindView(R.id.action_2d_scan)
    TextView tv2DScan;

    private TABS mSelectedTab = TABS.None;

    public enum TABS {
        Scan3D,
        Scan2D,
        None
    }

    private OnFragmentInteractionListener mListener;

    public ScansFragment() {
    }

    public static ScansFragment newInstance() {
        return new ScansFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_scans, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        select3D();
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

    @OnClick(R.id.action_3d_scan)
    public void select3D() {
        if (mSelectedTab != TABS.Scan3D) {
            tv3DScan.setTextColor(Color.WHITE);
            tv3DScan.setBackgroundColor(getResources().getColor(R.color.colorAccent));

            tv2DScan.setTextColor(getResources().getColor(R.color.colorAccent));
            tv2DScan.setBackground(getResources().getDrawable(R.drawable.background_accent_outline));

            FragmentManager fm = getChildFragmentManager();
            fm.beginTransaction().replace(R.id.fragment_container, Scan3DFragment.newInstance()).commit();

            mSelectedTab = TABS.Scan3D;
        }
    }

    @OnClick(R.id.action_2d_scan)
    public void select2D() {
        if (mSelectedTab != TABS.Scan2D) {
            tv2DScan.setTextColor(Color.WHITE);
            tv2DScan.setBackgroundColor(getResources().getColor(R.color.colorAccent));

            tv3DScan.setTextColor(getResources().getColor(R.color.colorAccent));
            tv3DScan.setBackground(getResources().getDrawable(R.drawable.background_accent_outline));

            FragmentManager fm = getChildFragmentManager();
            fm.beginTransaction().replace(R.id.fragment_container, Scan2DFragment.newInstance()).commit();

            mSelectedTab = TABS.Scan2D;
        }
    }
}
