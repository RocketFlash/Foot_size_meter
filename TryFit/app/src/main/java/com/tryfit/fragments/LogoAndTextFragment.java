package com.tryfit.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tryfit.tryfit.R;

public class LogoAndTextFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    public LogoAndTextFragment() {
    }

    public static LogoAndTextFragment newInstance(int sectionNumber) {
        LogoAndTextFragment fragment = new LogoAndTextFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_logo_and_text, container, false);
        TextView textView = (TextView) rootView.findViewById(R.id.section_label);

        switch (getArguments().getInt(ARG_SECTION_NUMBER)) {

            case 1:

                textView.setText(getString(R.string.section_text_1));
                break;
        }

        return rootView;
    }
}
