package com.sregg.android.tv.spotifyPlayer.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.sregg.android.tv.spotifyPlayer.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by simonreggiani on 15-02-04.
 */
public class NowPlayingIndicatorView extends LinearLayout {

    private List<AnimationDrawable> mFrameAnimations;
    private boolean mAnimationPlaying;

    public NowPlayingIndicatorView(Context context) {
        super(context);
        init();
    }

    public NowPlayingIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NowPlayingIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public NowPlayingIndicatorView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mFrameAnimations = new ArrayList<>(3);
        mAnimationPlaying = false;

        setWeightSum(3);

        LayoutParams params = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);

        addPeakMeter(params, R.drawable.peak_meter_1);
        addEmptySpace();
        addPeakMeter(params, R.drawable.peak_meter_2);
        addEmptySpace();
        addPeakMeter(params, R.drawable.peak_meter_3);
    }

    private void addEmptySpace() {
        addView(new View(getContext()), new LayoutParams(5, 0));
    }

    private void addPeakMeter(LayoutParams params, int drawableResId) {
        ImageView peakMeterIV = new ImageView(getContext());
        Drawable drawable = getResources().getDrawable(drawableResId);
        drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        peakMeterIV.setBackground(drawable);
        addView(peakMeterIV, params);
        AnimationDrawable frameAnimation = (AnimationDrawable) peakMeterIV.getBackground();
        mFrameAnimations.add(frameAnimation);
    }

    public void stopAnimations() {
        if (mFrameAnimations != null && mAnimationPlaying) {
            for (AnimationDrawable frameAnimation : mFrameAnimations) {
                frameAnimation.stop();
            }
            mAnimationPlaying = false;
        }
    }

    public void startAnimation() {
        if (mFrameAnimations != null && !mAnimationPlaying) {
            for (AnimationDrawable frameAnimation : mFrameAnimations) {
                frameAnimation.start();
            }
            mAnimationPlaying = true;
        }
    }
}
