package com.sregg.android.tv.spotifyPlayer.events;

/**
* Created by simonreggiani on 15-02-04.
*/
public class OnTrackChanged extends AbsPlayingEvent {
    public OnTrackChanged(PlayingState playingState) {
        super(playingState);
    }
}
