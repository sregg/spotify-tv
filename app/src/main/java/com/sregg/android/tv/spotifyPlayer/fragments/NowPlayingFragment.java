package com.sregg.android.tv.spotifyPlayer.fragments;

import android.os.Bundle;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.util.Log;

import com.squareup.otto.Subscribe;
import com.sregg.android.tv.spotifyPlayer.BusProvider;
import com.sregg.android.tv.spotifyPlayer.R;
import com.sregg.android.tv.spotifyPlayer.SpotifyTvApplication;
import com.sregg.android.tv.spotifyPlayer.events.PlayerStateChanged;
import com.sregg.android.tv.spotifyPlayer.events.PlayingState;
import com.sregg.android.tv.spotifyPlayer.presenters.NowPlayingDetailsPresenter;
import com.sregg.android.tv.spotifyPlayer.presenters.PlaylistTrackRowPresenter;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TrackSimple;

/**
 * Created by Björn Dahlgren on 27/07/15.
 */
public class NowPlayingFragment extends TracksDetailsFragment {

    private static final String TAG = NowPlayingFragment.class.getSimpleName();

    private static final long ACTION_VIEW_ARTIST = 2;

    private SpotifyTvApplication mApp;
    private PlayingState mPlayingState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        mApp = SpotifyTvApplication.getInstance();

        setupFragment();
    }

    @Override
    public void onDestroy() {
        BusProvider.getInstance().unregister(this);

        super.onDestroy();
    }

    private void setupFragment() {
        BusProvider.getInstance().register(this);
        setPlayingState(mApp.getSpotifyPlayerController().getPlayingState());
    }

    private void setPlayingState(PlayingState playingState) {
        this.mPlayingState = playingState;

        if (this.mPlayingState != null && this.mPlayingState.getCurrentTrack() != null) {
            onContentLoaded(this.mPlayingState.getCurrentTrack());
            loadDetailsRowImage(getCurrentTrackImageUrl());
        }
    }

    @Override
    List<Action> getDetailActions() {
        List<Action> actions = new ArrayList<>();
        actions.add(new Action(
                ACTION_VIEW_ARTIST, getResources().getString(R.string.go_to_artist),
                null
        ));
        return actions;
    }

    @Override
    protected boolean onActionClicked(Action action) {
        if (super.onActionClicked(action)) {
            return true;
        }

        return false;
    }

    private String getCurrentTrackImageUrl() {
        if (mPlayingState.getCurrentTrack() instanceof Track) {
            Track track = (Track) mPlayingState.getCurrentTrack();

            if (track.album != null && track.album.images != null && track.album.images.size() > 0) {
                return track.album.images.get(0).url;
            }
        }

        return null;
    }

    @Override
    protected Presenter getDetailsPresenter() {
        return new NowPlayingDetailsPresenter();
    }

    @Override
    protected Presenter getTrackRowPresenter(OnItemViewClickedListener onTrackRowItemClicked) {
        return new PlaylistTrackRowPresenter(onTrackRowItemClicked);
    }

    @Override
    protected List<TrackSimple> getTracks() {
        return mPlayingState.getTracksQueue();
    }

    @Override
    protected List<String> getTrackUris() {
        return mPlayingState.getTrackUrisQueue();
    }

    @Override
    protected String getObjectUri() {
        return mPlayingState.getCurrentObjectUri();
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onPlayerStateChanged(PlayerStateChanged playerState) {
        setPlayingState(playerState.getPlayingState());
    }
}
