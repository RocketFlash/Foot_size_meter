package com.tryfit.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tryfit.tryfit.R;

public class ImageAndTextFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    public ImageAndTextFragment() {
    }

    public static ImageAndTextFragment newInstance(int sectionNumber) {
        ImageAndTextFragment fragment = new ImageAndTextFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_image_and_text, container, false);
        TextView textView = (TextView) rootView.findViewById(R.id.section_label);
        final ImageView imageView = (ImageView) rootView.findViewById(R.id.picture);

        switch (getArguments().getInt(ARG_SECTION_NUMBER)) {

            case 2:

                textView.setText(getString(R.string.section_text_2));
                Picasso.with(getActivity()).load("file:///android_asset/instruction1.jpg").into(imageView);

                break;

            case 3:

                textView.setText(getString(R.string.section_text_3));
                Picasso.with(getActivity()).load("file:///android_asset/instruction2.jpg").into(imageView);

                break;

            case 6:

                textView.setText(getString(R.string.section_text_6));
                Picasso.with(getActivity()).load("file:///android_asset/instruction3.jpg").into(imageView);

                break;
        }

        return rootView;
    }
}
