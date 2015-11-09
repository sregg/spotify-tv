package com.sregg.android.tv.spotifyPlayer.events;

/**
* Created by simonreggiani on 15-02-07.
*/
public class OnPause extends AbsPlayingEvent {

    public OnPause(PlayingState playingState) {
        super(playingState);
    }
}
