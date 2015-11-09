package com.sregg.android.tv.spotifyPlayer.rows;

import android.support.v17.leanback.widget.Row;

import kaaes.spotify.webapi.android.models.TrackSimple;

public class TrackRow extends Row {

    private TrackSimple mTrack;

    public TrackRow(TrackSimple track) {
        super(null);
        mTrack = track;
        verify();
    }

    public TrackSimple getTrack() {
        return mTrack;
    }

    private void verify() {
        if (mTrack == null) {
            throw new IllegalArgumentException("Tracks cannot be null");
        }
    }
}
