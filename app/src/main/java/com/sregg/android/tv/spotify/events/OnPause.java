package com.sregg.android.tv.spotify.events;

/**
* Created by simonreggiani on 15-02-07.
*/
public class OnPause extends AbsPlayingEvent {

    public OnPause(String currentObjectUri) {
        super(currentObjectUri);
    }
}
