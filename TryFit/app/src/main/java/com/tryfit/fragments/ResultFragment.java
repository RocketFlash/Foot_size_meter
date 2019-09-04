package com.tryfit.fragments;

import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;
import com.tryfit.tryfitlib.TryFitLibResult;
import com.tryfit.listeners.OnInteractionListener;
import com.tryfit.tryfit.R;

public class ResultFragment extends Fragment {

    private ScrollView scrollView;
    private ImageView footDrawingLeft;
    private ImageView footDrawingRight;
    private TextView stickLengthLeft;
    private TextView stickLengthRight;
    private TextView ballWidthLeft;
    private TextView ballWidthRight;
    private PhotoView processed;
    private LinearLayout rightContainerInactive;
    private OnInteractionListener onInteractionListener;
    private LinearLayout rightContainer;
    private LinearLayout leftContainer;
    private LinearLayout leftContainerInactive;
    private Button rescanLeft;
    private Button rescanRight;
    private Button createProfile;
    private LinearLayout registerPanel;

    private TryFitLibResult leftFootData;
    private TryFitLibResult rightFootData;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_result, container, false);

        scrollView = (ScrollView) rootView.findViewById(R.id.scroll_view);
        footDrawingLeft = (ImageView) rootView.findViewById(R.id.foot_drawing_left);
        footDrawingRight = (ImageView) rootView.findViewById(R.id.foot_drawing_right);
        stickLengthLeft = (TextView) rootView.findViewById(R.id.stick_length_left);
        stickLengthRight = (TextView) rootView.findViewById(R.id.stick_length_right);
        ballWidthLeft = (TextView) rootView.findViewById(R.id.ball_width_left);
        ballWidthRight = (TextView) rootView.findViewById(R.id.ball_width_right);
        processed = (PhotoView) rootView.findViewById(R.id.processed);
        rightContainer = (LinearLayout) rootView.findViewById(R.id.container_right);
        rightContainerInactive = (LinearLayout) rootView.findViewById(R.id.container_inactive_right);
        leftContainer = (LinearLayout) rootView.findViewById(R.id.container_left);
        leftContainerInactive = (LinearLayout) rootView.findViewById(R.id.container_inactive_left);
        createProfile = (Button) rootView.findViewById(R.id.create_profile);
        registerPanel = (LinearLayout) rootView.findViewById(R.id.register_panel);

        rightContainerInactive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onInteractionListener.onInteraction(view.getId());
            }
        });

        leftContainerInactive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onInteractionListener.onInteraction(view.getId());
            }
        });

        rescanLeft = (Button) rootView.findViewById(R.id.rescan_left);
        rescanLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onInteractionListener.onInteraction(view.getId());
            }
        });

        rescanRight = (Button) rootView.findViewById(R.id.rescan_right);
        rescanRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onInteractionListener.onInteraction(view.getId());
            }
        });

        footDrawingLeft.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        footDrawingLeft.getViewTreeObserver()
                                .removeOnGlobalLayoutListener(this);


                        Picasso.
                                with(getActivity()).
                                load("file:///android_asset/foot_top_left.png").
                                resize(0, footDrawingLeft.getHeight()).
                                into(footDrawingLeft);
                    }
                });

        footDrawingRight.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        footDrawingLeft.getViewTreeObserver()
                                .removeOnGlobalLayoutListener(this);


                        Picasso.
                                with(getActivity()).
                                load("file:///android_asset/foot_top_right.png").
                                resize(0, footDrawingRight.getHeight()).
                                into(footDrawingRight);
                    }
                });

        createProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (createProfile.getTag().equals("Off")) {

                    createProfile.setTag("On");
                    Drawable drawable = getResources().getDrawable(R.drawable.ic_arrow_drop_up_black_24dp, getActivity().getTheme());
                    createProfile.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
                    registerPanel.setVisibility(View.VISIBLE);
                    scrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            scrollView.fullScroll(View.FOCUS_DOWN);
                        }
                    });
                } else {

                    createProfile.setTag("Off");
                    Drawable drawable = getResources().getDrawable(R.drawable.ic_arrow_drop_down_black_24dp, getActivity().getTheme());
                    createProfile.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
                    registerPanel.setVisibility(View.GONE);
                }
            }
        });

        updateViews();
        return rootView;
    }

    public void updateViews() {

        if (leftFootData != null) {

            if (stickLengthLeft != null) {

                stickLengthLeft.setText(leftFootData.getStickLength() + " mm");
            }

            if (ballWidthLeft != null) {

                ballWidthLeft.setText(leftFootData.getBallWidth() + " mm");
            }

            if (leftContainerInactive != null) {

                leftContainerInactive.setVisibility(View.GONE);
            }

            if (leftContainer != null) {

                leftContainer.setVisibility(View.VISIBLE);
            }

            if (processed != null) {

                processed.setImageBitmap(leftFootData.getProcessed());
            }

        } else {

            if (leftContainerInactive != null) {

                leftContainerInactive.setVisibility(View.VISIBLE);
            }

            if (leftContainer != null) {

                leftContainer.setVisibility(View.GONE);
            }
        }

        if (rightFootData != null) {

            if (stickLengthRight != null) {

                stickLengthRight.setText(rightFootData.getStickLength() + " mm");
            }

            if (ballWidthRight != null) {

                ballWidthRight.setText(rightFootData.getBallWidth() + " mm");
            }

            if (rightContainerInactive != null) {

                rightContainerInactive.setVisibility(View.GONE);
            }

            if (rightContainer != null) {

                rightContainer.setVisibility(View.VISIBLE);
            }

            if (processed != null) {

                processed.setImageBitmap(rightFootData.getProcessed());
            }

        } else {

            if (rightContainerInactive != null) {

                rightContainerInactive.setVisibility(View.VISIBLE);
            }

            if (rightContainer != null) {

                rightContainer.setVisibility(View.GONE);
            }
        }
    }

    public void setData(TryFitLibResult data) {

        String foot = data.getFeet();

        if (foot.equals("left")) {

            leftFootData = data;

        } else {

            rightFootData = data;

        }

        updateViews();
    }

    public void setOnInteractionListener(OnInteractionListener onInteractionListener) {
        this.onInteractionListener = onInteractionListener;
    }
}
