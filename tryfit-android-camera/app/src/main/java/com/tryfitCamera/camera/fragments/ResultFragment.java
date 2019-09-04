package com.tryfitCamera.camera.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.github.chrisbanes.photoview.PhotoView;
import com.tryfitCamera.camera.listeners.OnFragmentInteractionListener;
import com.tryfitCamera.camera.tryfitlib.TryFitLibResult;
import com.tryfitCamera.tryfit.R;

import butterknife.BindView;


public class ResultFragment extends Fragment {


    @BindView(R.id.processed_imageview_result_fragment)
    PhotoView mImage;

    private OnFragmentInteractionListener mListener;

    public ResultFragment() {
    }

    public static ResultFragment newInstance() {
        ResultFragment fragment = new ResultFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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

    public void setFootData(TryFitLibResult data) {
        mImage.setImageBitmap(data.getProcessedBitmap());
    }


}
