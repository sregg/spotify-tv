package com.sregg.android.tv.spotify.events;

/**
 * Created by simonreggiani on 15-02-08.
 */
public class OnShuffleChanged {

    public final boolean mIsShuffleOn;

    public OnShuffleChanged(boolean isShuffleOn) {
        mIsShuffleOn = isShuffleOn;
    }
}
