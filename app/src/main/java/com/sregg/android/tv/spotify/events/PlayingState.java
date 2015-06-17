package com.sregg.android.tv.spotify.events;

/**
 * Created by simonreggiani on 15-06-03.
 */
public class PlayingState {
    private final String mCurrentObjectUri;
    private String mCurrentTrackUri;

    public PlayingState(String currentObjectUri, String currentTrackUri) {
        mCurrentObjectUri = currentObjectUri;
        mCurrentTrackUri = currentTrackUri;
    }

    /**
     * @return the currently playing object (can be a playlist, an album or a single track)
     */
    public String getCurrentObjectUri() {
        return mCurrentObjectUri;
    }

    /**
     * @return the currently playing track in the currently playing playlist/album
     */
    public String getCurrentTrackUri() {
        return mCurrentTrackUri;
    }

    public void setCurrentTrackUri(String currentTrackUri) {
        mCurrentTrackUri = currentTrackUri;
    }

    public boolean isCurrentObjectOrTrack(String objectUri) {
        return mCurrentObjectUri.equals(objectUri) || mCurrentTrackUri.equals(objectUri);
    }
}
