package com.sregg.android.tv.spotifyPlayer.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.util.Log;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.sregg.android.tv.spotifyPlayer.Constants;
import com.sregg.android.tv.spotifyPlayer.SpotifyTvApplication;
import com.sregg.android.tv.spotifyPlayer.activities.PlaylistActivity;
import com.sregg.android.tv.spotifyPlayer.events.ContentState;
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

        loadPlaylist();

        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName(Constants.ANSWERS_CONTENT_NAME)
                .putContentType(Constants.ANSWERS_CONTENT_TYPE)
                .putContentId(mPlaylistId));
    }

    @Override
    List<Action> getDetailActions() {
        //no extra actions
        return null;
    }

    @Override
    protected Presenter getDetailsPresenter() {
        return new PlaylistDetailsPresenter();
    }

    @Override
    protected Presenter getTrackRowPresenter(OnItemViewClickedListener onTrackRowItemClicked) {
        return new PlaylistTrackRowPresenter(onTrackRowItemClicked);
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

                mPlaylistTracks = new ArrayList<>(playlist.tracks.items.size());
                mPlaylistTrackUris = new ArrayList<>(playlist.tracks.items.size());
                for (PlaylistTrack playlistTrack : playlist.tracks.items) {
                    mPlaylistTracks.add(playlistTrack.track);
                    mPlaylistTrackUris.add(playlistTrack.track.uri);
                }

                onContentLoaded(playlist);

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

    /**
     * Attempt to scroll to the track row that is currently playing
     */
    protected void scrollToCurrentTrack() {
        ContentState currentPlayState = playerController.getPlayingState();
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


}
