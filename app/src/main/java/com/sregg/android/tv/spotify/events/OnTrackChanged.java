package com.sregg.android.tv.spotify.events;

/**
* Created by simonreggiani on 15-02-04.
*/
public class OnTrackChanged extends AbsPlayingEvent {

    public OnTrackChanged(String currentObjectUri) {
        super(currentObjectUri);
    }
}
