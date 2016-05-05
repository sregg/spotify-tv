package com.sregg.android.tv.spotifyPlayer.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import java.util.HashMap;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
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
    @Nullable
    protected List<TrackSimple> getTracks() {
        return mPlaylistTracks;
    }

    @Override
    @Nullable
    protected List<String> getTrackUris() {
        return mPlaylistTrackUris;
    }

    @Override
    @Nullable
    protected String getObjectUri() {
        return null != mPlaylist ? mPlaylist.uri : null;
    }

    @Override
    protected Object getObject() {
        return mPlaylist;
    }

    private void loadPlaylist() {
        // load artist from API to get their image
        SpotifyTvApplication.getInstance().getSpotifyService().getPlaylist(mUserId, mPlaylistId, new Callback<Playlist>() {
            @Override
            public void success(final Playlist playlist, Response response) {
                if (!isAdded()) {
                    return;
                }
                mPlaylist = playlist;


                mPlaylistTracks = new ArrayList<>(playlist.tracks.total);
                mPlaylistTrackUris = new ArrayList<>(playlist.tracks.total);
                loadPlaylistTracks(0);
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private void loadPlaylistTracks(final int offset) {
        HashMap<String, Object> options = new HashMap<>();
        options.put(SpotifyService.OFFSET, Integer.toString(offset));
        options.put(SpotifyService.LIMIT, Integer.toString(Constants.PAGE_LIMIT_PLAYLIST_TRACKS));
        SpotifyTvApplication.getInstance().getSpotifyService().getPlaylistTracks(mUserId, mPlaylistId, options, new Callback<Pager<PlaylistTrack>>() {
            @Override
            public void success(Pager<PlaylistTrack> playlistTrackPager, Response response) {
                for (PlaylistTrack playlistTrack : playlistTrackPager.items) {
                    mPlaylistTracks.add(playlistTrack.track);
                    mPlaylistTrackUris.add(playlistTrack.track.uri);
                }

                if (playlistTrackPager.next != null) {
                    loadPlaylistTracks(playlistTrackPager.offset + Constants.PAGE_LIMIT);
                } else {
                    onContentLoaded();
                    scrollToCurrentTrack();
                }
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
                if (currentTrack != null && track.id.equals(currentTrack.id)) {
                    playingTrackPosition = mPlaylistTracks.indexOf(track);
                    break;
                }
            }

            setSelectedPosition(playingTrackPosition);
        }
    }


}
