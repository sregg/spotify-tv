package com.sregg.android.tv.spotify.events;

import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by simon_xomo on 15-05-07.
 */
public class OnQueueChanged {
    private List<Track> mTracks;

    public OnQueueChanged(List<Track> tracks) {
        mTracks = tracks;
    }

    public List<Track> getTracks() {
        return mTracks;
    }
}
