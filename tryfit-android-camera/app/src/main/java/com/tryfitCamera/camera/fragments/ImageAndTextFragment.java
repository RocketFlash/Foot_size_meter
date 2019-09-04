package com.tryfitCamera.camera.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tryfitCamera.tryfit.R;


public class ImageAndTextFragment extends Fragment {
    private static final String ARG_INSTRUCTIONS = "instructions";
    private static final String ARG_ASSET_PATH = "asset_path";

    public ImageAndTextFragment() {
    }

    public static ImageAndTextFragment newInstance(String instructions, String assetPath) {
        ImageAndTextFragment fragment = new ImageAndTextFragment();
        Bundle args = new Bundle();
        args.putString(ARG_INSTRUCTIONS, instructions);
        args.putString(ARG_ASSET_PATH, assetPath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_image_and_text, container, false);
        TextView textView = (TextView) rootView.findViewById(R.id.section_label);
        final ImageView imageView = (ImageView) rootView.findViewById(R.id.picture);

        String instructions = getArguments().getString(ARG_INSTRUCTIONS);
        textView.setText(instructions);

        String assetPath = getArguments().getString(ARG_ASSET_PATH);
        Picasso.with(getActivity()).load(assetPath).into(imageView);


        return rootView;
    }
}
