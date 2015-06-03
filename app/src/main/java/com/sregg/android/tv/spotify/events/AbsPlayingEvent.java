package com.sregg.android.tv.spotify.events;

/**
* Created by simonreggiani on 15-02-07.
*/
public class AbsPlayingEvent {
    private final PlayingState mPlayingState;

    public AbsPlayingEvent(PlayingState playingState) {
        mPlayingState = playingState;
    }

    public PlayingState getPlayingState() {
        return mPlayingState;
    }
}
