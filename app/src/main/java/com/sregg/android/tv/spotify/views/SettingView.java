package com.sregg.android.tv.spotify.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.IconTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.sregg.android.tv.spotify.R;
import com.sregg.android.tv.spotify.settings.Setting;

/**
 * Created by simonreggiani on 15-02-08.
 */
public class SettingView extends LinearLayout {

    private IconTextView mIconTextView;
    private TextView mTitleTV;
    private TextView mSubtitleTV;

    public SettingView(Context context) {
        super(context);
        init();
    }

    public SettingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SettingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public SettingView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setOrientation(VERTICAL);
        int size = getResources().getDimensionPixelSize(R.dimen.setting_view_size);
        setLayoutParams(new ViewGroup.LayoutParams(size, size));
        setBackgroundColor(getResources().getColor(R.color.setting_bg));
        setFocusable(true);
        setFocusableInTouchMode(true);
        setGravity(Gravity.CENTER);

        inflate(getContext(), R.layout.setting_view, this);

        mIconTextView = (IconTextView) findViewById(R.id.setting_icon);
        mTitleTV = (TextView) findViewById(R.id.setting_title);
        mSubtitleTV = (TextView) findViewById(R.id.setting_subtitle);
    }

    public void setSetting(Setting setting) {
        mIconTextView.setText(setting.getFontId());
        mTitleTV.setText(setting.getTitle());
        mSubtitleTV.setText(setting.getSubtitle());
    }
}
