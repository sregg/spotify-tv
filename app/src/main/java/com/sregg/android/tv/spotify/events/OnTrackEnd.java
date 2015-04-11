package com.sregg.android.tv.spotify.events;

/**
* Created by simonreggiani on 15-02-04.
*/
public class OnTrackEnd extends AbsPlayingEvent {

    public OnTrackEnd(String currentObjectUri) {
        super(currentObjectUri);
    }
}
