package com.sregg.android.tv.spotify.events;

/**
* Created by simonreggiani on 15-02-04.
*/
public class OnPlay extends AbsPlayingEvent {
    public OnPlay(PlayingState playingState) {
        super(playingState);
    }
}
