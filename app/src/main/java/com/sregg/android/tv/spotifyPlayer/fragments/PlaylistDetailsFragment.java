package com.sregg.android.tv.spotifyPlayer.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.DetailsOverviewRow;
import android.support.v17.leanback.widget.OnActionClickedListener;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.util.Log;

import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.PlayerStateCallback;
import com.squareup.otto.Subscribe;
import com.sregg.android.tv.spotifyPlayer.BusProvider;
import com.sregg.android.tv.spotifyPlayer.R;
import com.sregg.android.tv.spotifyPlayer.SpotifyTvApplication;
import com.sregg.android.tv.spotifyPlayer.activities.PlaylistActivity;
import com.sregg.android.tv.spotifyPlayer.controllers.SpotifyPlayerController;
import com.sregg.android.tv.spotifyPlayer.events.OnPause;
import com.sregg.android.tv.spotifyPlayer.events.OnPlay;
import com.sregg.android.tv.spotifyPlayer.events.OnTrackChanged;
import com.sregg.android.tv.spotifyPlayer.events.PlayingState;
import com.sregg.android.tv.spotifyPlayer.presenters.PlaylistDetailsPresenter;
import com.sregg.android.tv.spotifyPlayer.presenters.PlaylistTrackRowPresenter;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import kaaes.spotify.webapi.android.models.TrackSimple;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class PlaylistDetailsFragment extends TracksDetailsFragment implements PlayerStateCallback {

    private static final String TAG = PlaylistDetailsFragment.class.getSimpleName();

    private static final long ACTION_PLAY_PAUSE_PLAYLIST = 1;

    private String mPlaylistId;
    private String mUserId;
    private Playlist mPlaylist;
    private List<String> mPlaylistTrackUris;
    private List<TrackSimple> mPlaylistTracks;
    private SpotifyPlayerController playerController;
    private ArrayObjectAdapter actionsAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        BusProvider.getInstance().register(this);

        playerController = SpotifyTvApplication.getInstance().getSpotifyPlayerController();

        Intent intent = getActivity().getIntent();

        mPlaylistId = intent.getStringExtra(PlaylistActivity.ARG_PLAYLIST_ID);
        mUserId = intent.getStringExtra(PlaylistActivity.ARG_USER_ID);

        setupFragment();
        loadPlaylist();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    protected Presenter getDetailsPresenter() {
        return new PlaylistDetailsPresenter();
    }

    @Override
    protected Presenter getTrackRowPresenter(OnItemViewClickedListener onTrackRowItemClicked) {
        return new PlaylistTrackRowPresenter(onTrackRowItemClicked);
    }

    private void setupFragment() {
        setOnActionClickedListener(new OnActionClickedListener() {
            @Override
            public void onActionClicked(Action action) {
                if (action.getId() == ACTION_PLAY_PAUSE_PLAYLIST) {
                    if (isPlaylistPlaying()) {
                        playerController.togglePauseResume();
                    } else {
                        playerController.play(mPlaylist.uri, mPlaylistTrackUris, getTracks());
                    }
                }
            }
        });
    }

    @Override
    protected List<TrackSimple> getTracks() {
        return mPlaylistTracks;
    }

    @Override
    protected List<String> getTrackUris() {
        return mPlaylistTrackUris;
    }

    @Override
    protected String getObjectUri() {
        return mPlaylist.uri;
    }

    private void loadPlaylist() {
        // load artist from API to get their image
        SpotifyTvApplication.getInstance().getSpotifyService().getPlaylist(mUserId, mPlaylistId, new Callback<Playlist>() {
            @Override
            public void success(final Playlist playlist, Response response) {
                mPlaylist = playlist;
                setupDetails(playlist);

                mPlaylistTracks = new ArrayList<>(playlist.tracks.items.size());
                mPlaylistTrackUris = new ArrayList<>(playlist.tracks.items.size());
                for (PlaylistTrack playlistTrack : playlist.tracks.items) {
                    mPlaylistTracks.add(playlistTrack.track);
                    mPlaylistTrackUris.add(playlistTrack.track.uri);
                }
                setupTracksRows(mPlaylistTracks);

                if (playlist.images.size() > 0) {
                    String imageUrl = playlist.images.get(0).url;
                    loadDetailsRowImage(imageUrl);
                }

                scrollToCurrentTrack();
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onTrackChanged(OnTrackChanged onTrackChanged) {
        updatePlayingState();
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onTrackChanged(OnPause onPause) {
        updatePlayingState();
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onTrackChanged(OnPlay onPlay) {
        updatePlayingState();
    }

    private void setupDetails(Playlist playlist) {
        DetailsOverviewRow detailsRow = new DetailsOverviewRow(playlist);

        actionsAdapter = new ArrayObjectAdapter();
        populateActionsAdapter();
        detailsRow.setActionsAdapter(actionsAdapter);

        setDetailsRow(detailsRow);
    }

    private void populateActionsAdapter() {
        boolean playlistPlaying = isPlaylistPlaying();
        Action action = new Action(ACTION_PLAY_PAUSE_PLAYLIST, playlistPlaying ? getResources().getString(R.string.lb_playback_controls_pause) : getResources().getString(R.string.lb_playback_controls_play), null);
        actionsAdapter.add(action);
    }

    private void updatePlayingState(){
        playerController.getPlayerState(this);
    }

    private boolean isPlaylistPlaying() {
        PlayingState currentPlayState = playerController.getPlayingState();
        return currentPlayState != null && currentPlayState.isCurrentObject(mPlaylist.uri);
    }

    /**
     * Attempt to scroll to the track row that is currently playing
     */
    protected void scrollToCurrentTrack() {
        PlayingState currentPlayState = playerController.getPlayingState();
        if (currentPlayState != null && currentPlayState.isCurrentObject(mPlaylist.uri)) {
            TrackSimple currentTrack = currentPlayState.getCurrentTrack();
            //try to scroll to track row that is currently playing
            int playingTrackPosition = 0;
            for (TrackSimple track : mPlaylistTracks) {
                if (track.id.equals(currentTrack.id)) {
                    playingTrackPosition = mPlaylistTracks.indexOf(track);
                    break;
                }
            }

            setSelectedPosition(playingTrackPosition);
        }
    }

    @Override
    public void onPlayerState(PlayerState playerState) {
        boolean playlistPlaying = isPlaylistPlaying() && playerState.playing;
        Action action = (Action) actionsAdapter.get(0);
        action.setLabel1(playlistPlaying ? getResources().getString(R.string.lb_playback_controls_pause) : getResources().getString(R.string.lb_playback_controls_play));
        actionsAdapter.notifyArrayItemRangeChanged(0,1);
    }
}
