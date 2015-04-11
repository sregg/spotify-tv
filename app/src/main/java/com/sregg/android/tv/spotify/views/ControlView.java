package com.sregg.android.tv.spotify.views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.IconTextView;
import com.squareup.otto.Subscribe;
import com.sregg.android.tv.spotify.BusProvider;
import com.sregg.android.tv.spotify.R;
import com.sregg.android.tv.spotify.enums.Control;
import com.sregg.android.tv.spotify.events.OnShuffleChanged;

/**
 * Created by simonreggiani on 15-02-08.
 */
public class ControlView extends FrameLayout {

    private IconTextView mIconTextView;
    private Control mControl;

    public ControlView(Context context) {
        super(context);
        init();
    }

    public ControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ControlView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        BusProvider.getInstance().register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        BusProvider.unregister(this);
        super.onDetachedFromWindow();
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onShuffleChanged(OnShuffleChanged onShuffleChanged) {
        if (mControl == Control.SHUFFLE) {
            toggleControlColor(onShuffleChanged.mIsShuffleOn);
        }
    }

    public void toggleControlColor(boolean isOn) {
        int color;
        if (isOn) {
            color = getResources().getColor(R.color.control_on);
        } else {
            color = Color.WHITE;
        }
        mIconTextView.setTextColor(color);
    }


    private void init() {
        inflate(getContext(), R.layout.control_view, this);
        mIconTextView = (IconTextView) findViewById(R.id.control_icon);
        toggleControlColor(false);
    }

    public IconTextView getIconTextView() {
        return mIconTextView;
    }

    public void setControl(Control control) {
        mControl = control;
    }
}
