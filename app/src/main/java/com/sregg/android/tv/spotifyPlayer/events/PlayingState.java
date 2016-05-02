package com.sregg.android.tv.spotifyPlayer.events;

import java.util.List;

import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TrackSimple;

/**
 * Created by simonreggiani on 15-06-03.
 */
public class PlayingState {
    private final String mCurrentObjectUri;
    private final List<TrackSimple> mTracksQueue;
    private String mCurrentTrackUri;
    private List<String> mTrackUrisQueue;
    private TrackSimple mCurrentTrack;

    public PlayingState(String currentObjectUri, String currentTrackUri, List<String> trackUrisQueue, List<TrackSimple> tracksQueue) {
        mCurrentObjectUri = currentObjectUri;
        mCurrentTrackUri = currentTrackUri;
        mTrackUrisQueue = trackUrisQueue;
        mTracksQueue = tracksQueue;
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

    public boolean isCurrentObject(String objectUri) {
        return mCurrentObjectUri.equals(objectUri);
    }

    public boolean isCurrentTrack(String trackUri) {
        return mCurrentTrackUri.equals(trackUri);
    }

    public List<String> getTrackUrisQueue() {
        return mTrackUrisQueue;
    }

    public void setCurrentTrack(TrackSimple currentTrack) {
        mCurrentTrack = currentTrack;
    }

    public TrackSimple getCurrentTrack() {
        return mCurrentTrack;
    }

    public List<TrackSimple> getTracksQueue() {
        return mTracksQueue;
    }
}
