package com.sregg.android.tv.spotify.events;

import java.util.List;

/**
 * Created by simonreggiani on 15-06-03.
 */
public class PlayingState {
    private final String mCurrentObjectUri;
    private String mCurrentTrackUri;
    private List<String> mTrackUrisQueue;

    public PlayingState(String currentObjectUri, String currentTrackUri, List<String> trackUrisQueue) {
        mCurrentObjectUri = currentObjectUri;
        mCurrentTrackUri = currentTrackUri;
        mTrackUrisQueue = trackUrisQueue;
    }

    /**
     * @return the currently playing object (can be a playlist, an album or a single track)
     */
    public String getCurrentObjectUri() {
        return mCurrentObjectUri;
    }

    public void setCurrentTrackUri(String currentTrackUri) {
        mCurrentTrackUri = currentTrackUri;
    }

    public boolean isCurrentObjectOrTrack(String objectUri) {
        return mCurrentObjectUri.equals(objectUri) || mCurrentTrackUri.equals(objectUri);
    }

    public List<String> getTrackUrisQueue() {
        return mTrackUrisQueue;
    }
}
