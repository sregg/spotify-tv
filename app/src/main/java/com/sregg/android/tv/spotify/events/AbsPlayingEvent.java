package com.sregg.android.tv.spotify.events;

/**
* Created by simonreggiani on 15-02-07.
*/
public class AbsPlayingEvent {
    private final String mCurrentObjectUri;

    public AbsPlayingEvent(String currentObjectUri) {
        mCurrentObjectUri = currentObjectUri;
    }

    public String getCurrentObjectUri() {
        return mCurrentObjectUri;
    }
}
