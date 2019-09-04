package com.tryfit.fittings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.XmlResourceParser;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import com.tryfit.R;
import com.tryfit.common.db.models.Group;

import java.util.List;

/**
 * Created by alexeyreznik on 20/07/2017.
 */

public class GroupsView extends LinearLayout {
    private final Context mContext;
    private HorizontalScrollView mGroupsScroll;
    private LinearLayout mGroupsBar;

    private OnGroupSelectedListener mListener;
    private List<Group> mGroups;

    public interface OnGroupSelectedListener {
        void onGroupSelected(String groupId);
    }

    public GroupsView(Context context) {
        super(context);
        this.mContext = context;
        init();
    }

    public void setOnGroupSelectedListener(OnGroupSelectedListener listener) {
        this.mListener = listener;
    }

    public GroupsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init();
    }

    public GroupsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init();
    }

    public GroupsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mContext = context;
        init();
    }

    private void init() {
        LayoutInflater.from(mContext).inflate(R.layout.groups_view_layout, this, true);
        mGroupsScroll = (HorizontalScrollView) findViewById(R.id.groups_scroll_container);
        mGroupsScroll.setSmoothScrollingEnabled(true);
        mGroupsBar = new LinearLayout(mContext);
        mGroupsBar.setOrientation(LinearLayout.HORIZONTAL);
        mGroupsBar.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mGroupsScroll.addView(mGroupsBar);
    }

    public void clear() {
        mGroupsBar.removeAllViews();
        mGroupsScroll.smoothScrollTo(0, 0);
    }

    public void setGroups(List<Group> groups, String selectedGroupId) {
        this.mGroups = groups;
        for (final Group group : mGroups) {
            final ToggleButton button = new ToggleButton(mContext);
            try {
                @SuppressLint("ResourceType") XmlResourceParser parser = getResources().getXml(R.color.group_button_textcolor);
                ColorStateList colors = ColorStateList.createFromXml(getResources(), parser);
                button.setTextColor(colors);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            button.setBackgroundDrawable(getResources().getDrawable(R.drawable.group_button_background));
            final float scale = getContext().getResources().getDisplayMetrics().density;
            int margin = (int) (6 * scale + 0.5f);
            int padding = (int) (4 * scale + 0.5f);
            int height = (int) (32 * scale + 0.5f);
            button.setHeight(height);
            button.setPadding(padding, 0, padding, 0);
            LayoutParams params = new LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    height
            );
            params.setMargins(margin, margin, margin, margin);
            button.setLayoutParams(params);
            button.setAllCaps(false);

            button.setText(group.getName());
            button.setTextOn(group.getName());
            button.setTextOff(group.getName());
            button.setTag(group.getId());

            button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    for (int i = 0; i < mGroupsBar.getChildCount(); i++) {
                        ToggleButton button = (ToggleButton) mGroupsBar.getChildAt(i);
                        if (!button.equals(view)) {
                            if (button.isChecked()) {
                                button.setChecked(false);
                                button.setEnabled(true);
                            }
                        } else {
                            button.setEnabled(false);
                            if (mListener != null) {
                                mListener.onGroupSelected(group.getId());
                            }
                        }
                    }
                }
            });

            mGroupsBar.addView(button);
        }

        for (int i = 0; i < mGroupsBar.getChildCount(); i++) {
            ToggleButton view = (ToggleButton) mGroupsBar.getChildAt(i);

            if (view.getTag().equals(selectedGroupId)) {
                view.setChecked(true);

                int screenWidth = getResources().getDisplayMetrics().widthPixels;
                int scrollX = (i * mGroupsBar.getMeasuredWidth()/mGroupsBar.getChildCount() - (screenWidth / 2));
                mGroupsScroll.smoothScrollTo(scrollX, 0);
            }
        }
    }
}
