package com.sregg.android.tv.spotifyPlayer.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.DetailsOverviewRow;
import android.support.v17.leanback.widget.OnActionClickedListener;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.util.Log;

import com.sregg.android.tv.spotifyPlayer.R;
import com.sregg.android.tv.spotifyPlayer.SpotifyTvApplication;
import com.sregg.android.tv.spotifyPlayer.activities.PlaylistActivity;
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

public class PlaylistDetailsFragment extends TracksDetailsFragment {

    private static final String TAG = PlaylistDetailsFragment.class.getSimpleName();

    private static final long ACTION_PLAY_PLAYLIST = 1;

    private String mPlaylistId;
    private String mUserId;
    private Playlist mPlaylist;
    private List<String> mPlaylistTrackUris;
    private List<TrackSimple> mPlaylistTracks;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        Intent intent = getActivity().getIntent();

        mPlaylistId = intent.getStringExtra(PlaylistActivity.ARG_PLAYLIST_ID);
        mUserId = intent.getStringExtra(PlaylistActivity.ARG_USER_ID);

        setupFragment();
        loadPlaylist();
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
                if (action.getId() == ACTION_PLAY_PLAYLIST) {
                    SpotifyTvApplication.getInstance().getSpotifyPlayerController().play(mPlaylist.uri, mPlaylistTrackUris, getTracks());
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
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private void setupDetails(Playlist playlist) {
        DetailsOverviewRow detailsRow = new DetailsOverviewRow(playlist);

        detailsRow.addAction(new Action(
                ACTION_PLAY_PLAYLIST,
                getResources().getString(R.string.lb_playback_controls_play),
                null,
                getActivity().getResources().getDrawable(R.drawable.lb_ic_play)
        ));

        setDetailsRow(detailsRow);
    }
}
