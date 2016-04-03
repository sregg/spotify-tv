package com.sregg.android.tv.spotify.fragments;

import android.os.Bundle;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.DetailsOverviewRow;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.util.Log;

import com.squareup.otto.Subscribe;
import com.sregg.android.tv.spotify.BusProvider;
import com.sregg.android.tv.spotify.R;
import com.sregg.android.tv.spotify.SpotifyTvApplication;
import com.sregg.android.tv.spotify.events.OnPause;
import com.sregg.android.tv.spotify.events.OnPlay;
import com.sregg.android.tv.spotify.events.OnTrackChanged;
import com.sregg.android.tv.spotify.events.PlayingState;
import com.sregg.android.tv.spotify.presenters.NowPlayingDetailsPresenter;
import com.sregg.android.tv.spotify.presenters.PlaylistTrackRowPresenter;

import java.util.List;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TrackSimple;

/**
 * Created by Björn Dahlgren on 27/07/15.
 */
public class NowPlayingFragment extends TracksDetailsFragment {

    private static final String TAG = NowPlayingFragment.class.getSimpleName();

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
            DetailsOverviewRow detailsRow = new DetailsOverviewRow(this.mPlayingState.getCurrentTrack());

            detailsRow.addAction(new Action(
                    0,
                    getResources().getString(R.string.lb_playback_controls_play),
                    null,
                    getActivity().getResources().getDrawable(R.drawable.lb_ic_play)
            ));
            detailsRow.addAction(new Action(
                    1, getResources().getString(R.string.go_to_artist),
                    null
            ));

            setDetailsRow(detailsRow);
            setupTracksRows(mPlayingState.getTracksQueue());
            loadDetailsRowImage(getCurrentTrackImageUrl());
        }
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
    public void onTrackChanged(OnTrackChanged onTrackChanged) {
        setPlayingState(onTrackChanged.getPlayingState());
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onPlay(OnPlay onPlay) {
        setPlayingState(onPlay.getPlayingState());
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onPause(OnPause onPause) {
        setPlayingState(onPause.getPlayingState());
    }
}
