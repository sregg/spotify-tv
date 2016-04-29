package com.sregg.android.tv.spotifyPlayer.events;

/**
* Created by simonreggiani on 15-02-07.
*/
public class AbsPlayingEvent {
    private final ContentState mContentState;

    public AbsPlayingEvent(ContentState contentState) {
        mContentState = contentState;
    }

    public ContentState getPlayingState() {
        return mContentState;
    }
}
