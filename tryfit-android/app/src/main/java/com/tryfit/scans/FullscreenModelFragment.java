package com.tryfit.scans;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tryfit.MainActivity;
import com.tryfit.R;
import com.tryfit.common.OnFragmentInteractionListener;
import com.tryfit.common.db.RealmHelper;
import com.tryfit.common.db.models.FITModel;
import com.tryfit.scans.opengl.Mesh;
import com.tryfit.scans.opengl.MyGLRenderer;
import com.tryfit.scans.opengl.MyGLSurfaceView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

public class FullscreenModelFragment extends Fragment {

    private static final String TAG = FullscreenModelFragment.class.getSimpleName();
    @BindView(R.id.gl_surface_fullscreen)
    MyGLSurfaceView glSurfaceView;

    private OnFragmentInteractionListener mListener;
    private MyGLRenderer mRenderer;

    public FullscreenModelFragment() {
    }

    public static FullscreenModelFragment newInstance() {
        return new FullscreenModelFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Realm realm = Realm.getDefaultInstance();
        FITModel leftFootModel = RealmHelper.getFootModel(realm, RealmHelper.KEY_LEFT_FOOT_MODEL);
        FITModel rightFootModel = RealmHelper.getFootModel(realm, RealmHelper.KEY_RIGHT_FOOT_MODEL);

        Mesh leftMesh = Mesh.fromRealmMesh(leftFootModel.getMeshes().get(0));
        Mesh rightMesh = Mesh.fromRealmMesh(rightFootModel.getMeshes().get(0));
        realm.close();

        mRenderer = new MyGLRenderer(leftMesh, rightMesh);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_fullscreen_model, container, false);
        ButterKnife.bind(this, rootView);

        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setZOrderOnTop(true);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        glSurfaceView.setRenderer(mRenderer);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
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
    public void onResume() {
        super.onResume();

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.scans));
        }

        ((MainActivity) getActivity()).selectTab(MainActivity.TABS.None);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
