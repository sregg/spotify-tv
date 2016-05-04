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
import com.sregg.android.tv.spotifyPlayer.activities.ArtistsAlbumsActivity;
import com.sregg.android.tv.spotifyPlayer.events.ContentState;
import com.sregg.android.tv.spotifyPlayer.events.OnTrackChanged;
import com.sregg.android.tv.spotifyPlayer.presenters.NowPlayingDetailsPresenter;
import com.sregg.android.tv.spotifyPlayer.presenters.PlaylistTrackRowPresenter;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.TrackSimple;

/**
 * Created by Björn Dahlgren on 27/07/15.
 */
public class NowPlayingFragment extends TracksDetailsFragment {

    private static final String TAG = NowPlayingFragment.class.getSimpleName();

    private static final long ACTION_VIEW_ARTIST = 2;

    private SpotifyTvApplication mApp;
    private ContentState mContentState;

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
        updateContent();
    }

    private void updateContent() {
        this.mContentState = mApp.getSpotifyPlayerController().getPlayingState();

        if (this.mContentState != null && this.mContentState.getCurrentTrack() != null) {
            onContentLoaded();
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
        if (action.getId() == ACTION_VIEW_ARTIST) {
            ArtistSimple artist = null;

            TrackSimple track = (TrackSimple) getObject();
            if (track.artists.size() > 0) {
                artist = track.artists.get(0);
            }

            if (artist != null) {
                ArtistsAlbumsActivity.launch(getActivity(), artist.id, artist.name);
            }
            return true;
        }

        return false;
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
        return mContentState.getTracksQueue();
    }

    @Override
    protected List<String> getTrackUris() {
        return mContentState.getTrackUrisQueue();
    }

    @Override
    protected String getObjectUri() {
        return mContentState != null ? mContentState.getCurrentObjectUri() : null;
    }

    @Override
    protected Object getObject() {
        return mContentState != null ? mContentState.getCurrentTrack() : null;
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onTrackChanged(OnTrackChanged trackChanged) {
        updateContent();
    }
}
