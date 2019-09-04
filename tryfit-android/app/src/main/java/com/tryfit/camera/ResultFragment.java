package com.tryfit.camera;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tryfit.R;

import java.io.FileInputStream;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;


public class ResultFragment extends Fragment {


    @BindView(R.id.foot_parameters_result_f)
    LinearLayout textRersult;
    @BindView(R.id.stick_length_result_f)
    TextView stickLengthResult;
    @BindView(R.id.ball_width_result_f)
    TextView ballWidthResult;
    @BindView(R.id.processed_imageview_result_f)
    ImageView imageView;
    @BindView(R.id.buttonOk_f)
    Button buttonOK;
    @BindView(R.id.buttonBad_f)
    Button buttonBad;


    private float stickLength;
    private float ballWidth;

    public static final String EXTRA_FOOT = "foot";
    public static final String EXTRA_STICK_LENGTH = "stickLength";
    public static final String EXTRA_BALL_WIDTH = "ballWidth";


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

    public void updateData(String filename, int sLength, int bWidth) {

        Bitmap processedImage = null;
        try {
            FileInputStream is = getActivity().openFileInput(filename);
            processedImage = BitmapFactory.decodeStream(is);
            is.close();
            imageView.setImageBitmap(processedImage);
            int scale = getActivity().getBaseContext().getResources().getDisplayMetrics().heightPixels;
            imageView.getLayoutParams().height = (int) (((float) 3 / 5) * scale);
            imageView.requestLayout();
            imageView.setVisibility(View.VISIBLE);
            stickLength = (float) sLength;
            ballWidth = (float) bWidth;
            stickLengthResult.setText(String.valueOf(sLength) + " mm");
            ballWidthResult.setText(String.valueOf(bWidth) + " mm");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.fragment_result, container, false);
        ButterKnife.bind(this, rootView);
        imageView.setVisibility(View.GONE);

        stickLength = 0;
        ballWidth = 0;

        buttonBad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((TryFitCameraActivity) getActivity()).setPreviousItemInActivity();
            }
        });

        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((TryFitCameraActivity) getActivity()).sendResults(stickLength, ballWidth);
            }
        });

        return rootView;
    }

}
