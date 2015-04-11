package com.sregg.android.tv.spotify.events;

/**
* Created by simonreggiani on 15-02-04.
*/
public class OnTrackStart extends AbsPlayingEvent {

    public OnTrackStart(String currentObjectUri) {
        super(currentObjectUri);
    }
}
