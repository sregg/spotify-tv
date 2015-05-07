/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.sregg.android.tv.spotify.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.*;
import android.util.Log;
import android.view.View;

import com.squareup.otto.Subscribe;
import com.sregg.android.tv.spotify.BusProvider;
import com.sregg.android.tv.spotify.Constants;
import com.sregg.android.tv.spotify.R;
import com.sregg.android.tv.spotify.SpotifyTvApplication;
import com.sregg.android.tv.spotify.activities.SearchActivity;
import com.sregg.android.tv.spotify.enums.Control;
import com.sregg.android.tv.spotify.events.OnQueueChanged;
import com.sregg.android.tv.spotify.events.OnTrackEnd;
import com.sregg.android.tv.spotify.presenters.*;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.*;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainFragment extends BrowseFragment {
    private static final String TAG = "MainFragment";

    private SpotifyService mSpotifyService;
    private ArrayObjectAdapter mPlaylistsAdapter;
    private ArrayObjectAdapter mSavedSongsAdapter;
    private ArrayObjectAdapter mSavedAlbumsAdapter;
    private ArrayObjectAdapter mSavedArtistsAdapter;
    private ArrayObjectAdapter mRowsAdapter;
    private ArrayObjectAdapter mControlsAdapter;
    private ArrayObjectAdapter mQueueAdapter;

    @SuppressWarnings("unused")
    @Subscribe
    public void onQueueChanged(OnQueueChanged onQueueChanged) {
        mQueueAdapter.clear();
        mQueueAdapter.addAll(0, onQueueChanged.getTracks());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onActivityCreated(savedInstanceState);

        SpotifyTvApplication app = SpotifyTvApplication.getInstance();
        mSpotifyService = app.getSpotifyService();

        setupUIElements();
        setupEventListeners();

        setupMainAdapter();

        loadUserLibraryRows();

        loadControlsRow();

        loadQueueRow();
    }

    private void setupMainAdapter() {
        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
    }

    private void setupUIElements() {
        setTitle(getString(R.string.browse_title));

        // over title
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);

        // set fastLane (or headers) background color
        setBrandColor(getResources().getColor(R.color.fastlane_background));
        // set search icon color
        setSearchAffordanceColor(getResources().getColor(R.color.search_opaque));
    }

    private void setupEventListeners() {
        setOnSearchClickedListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
            }
        });

        setOnItemViewClickedListener(new OnItemViewClickedListener() {
            @Override
            public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
                SpotifyTvApplication app = SpotifyTvApplication.getInstance();
                if (item instanceof Control) {
                    app.getSpotifyPlayerController().onControlClick(((Control) item));
                } else {
                    app.onItemClick(getActivity(), item);
                }
            }
        });
    }

    private void loadUserLibraryRows() {
        // playlist row
        mPlaylistsAdapter = new ArrayObjectAdapter(new PlaylistCardPresenter());
        HeaderItem playListHeader = new HeaderItem(0, getString(R.string.playlists), null);
        mRowsAdapter.add(new ListRow(playListHeader, mPlaylistsAdapter));

        // Albums row
        mSavedAlbumsAdapter = new ArrayObjectAdapter(new AlbumCardPresenter());
        HeaderItem albumsHeader = new HeaderItem(0, getString(R.string.albums), null);
        mRowsAdapter.add(new ListRow(albumsHeader, mSavedAlbumsAdapter));

        // Artists row
        mSavedArtistsAdapter = new ArrayObjectAdapter(new ArtistCardPresenter());
        HeaderItem artistsHeader = new HeaderItem(0, getString(R.string.artists), null);
        mRowsAdapter.add(new ListRow(artistsHeader, mSavedArtistsAdapter));

        // Songs row
        mSavedSongsAdapter = new ArrayObjectAdapter(new TrackCardPresenter());
        HeaderItem songsHeader = new HeaderItem(0, getString(R.string.songs), null);
        mRowsAdapter.add(new ListRow(songsHeader, mSavedSongsAdapter));

        setAdapter(mRowsAdapter);

        // load playlists (need to load current user first)
        loadCurrentUser();

        // load saved songs
        loadSavedSongs();

    }

    private void loadCurrentUser() {
        mSpotifyService.getMe(new Callback<User>() {
            @Override
            public void success(User user, Response response) {
                SpotifyTvApplication.getInstance().setCurrentUser(user);
                loadPlaylists(user);
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private void loadPlaylists(final User user) {
        mPlaylistsAdapter.clear();

        // add Starred playlist by default
        Playlist starredPlaylist = new Playlist();
        starredPlaylist.name = getString(R.string.starred);
        starredPlaylist.id = Constants.STARRED_PLAYLIST_ID;
        starredPlaylist.uri = String.format("spotify:user:%s:starred", user.id);
        starredPlaylist.tracks = new Pager<>(); // TODO
        mPlaylistsAdapter.add(starredPlaylist);

        mSpotifyService.getPlaylists(user.id, new Callback<Pager<Playlist>>() {
            @Override
            public void success(Pager<Playlist> playlistPager, Response response) {
                mPlaylistsAdapter.addAll(mPlaylistsAdapter.size(), playlistPager.items);
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private void loadSavedSongs() {
        mSavedSongsAdapter.clear();
        mSavedAlbumsAdapter.clear();
        mSavedArtistsAdapter.clear();

        List<String> albumIds = new ArrayList<>();
        List<String> artistIds = new ArrayList<>();
        loadPage(0, albumIds, artistIds);
    }

    private void loadPage(int offset, final List<String> albumIds, final List<String> artistIds) {
        Map<String, Object> options = new HashMap<>();
        options.put(SpotifyService.OFFSET, offset);
        options.put(SpotifyService.LIMIT, Constants.PAGE_LIMIT);
        mSpotifyService.getMySavedTracks(options, new Callback<Pager<SavedTrack>>() {
            @Override
            public void success(Pager<SavedTrack> savedTrackPager, Response response) {
                ArrayList<Track> songs = new ArrayList<>();
                ArrayList<AlbumSimple> albums = new ArrayList<>();
                ArrayList<ArtistSimple> artists = new ArrayList<>();
                for (final SavedTrack savedTrack : savedTrackPager.items) {
                    // add saved track
                    songs.add(savedTrack.track);

                    // add album if not already added
                    AlbumSimple album = savedTrack.track.album;
                    if (!albumIds.contains(album.id)) {
                        albums.add(album);
                        albumIds.add(album.id);
                    }

                    // add artists if not already added
                    for (ArtistSimple artist : savedTrack.track.artists) {
                        if (!artistIds.contains(artist.id)) {
                            artists.add(artist);
                            artistIds.add(artist.id);
                        }
                    }
                }

                mSavedSongsAdapter.addAll(mSavedSongsAdapter.size(), songs);
                mSavedAlbumsAdapter.addAll(mSavedAlbumsAdapter.size(), albums);
                mSavedArtistsAdapter.addAll(mSavedArtistsAdapter.size(), artists);

                // load next page
                if (savedTrackPager.next != null) {
                    loadPage(savedTrackPager.offset + Constants.PAGE_LIMIT, albumIds, artistIds);
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private void loadControlsRow() {
        HeaderItem controlsHeader = new HeaderItem(getString(R.string.controls), null);

        ControlPresenter controlPresenter = new ControlPresenter();
        mControlsAdapter = new ArrayObjectAdapter(controlPresenter);

        mControlsAdapter.add(Control.SHUFFLE);
        mControlsAdapter.add(Control.PREVIOUS);
        mControlsAdapter.add(Control.PLAY);
        mControlsAdapter.add(Control.PAUSE);
        mControlsAdapter.add(Control.STOP);
        mControlsAdapter.add(Control.NEXT);

        mRowsAdapter.add(new ListRow(controlsHeader, mControlsAdapter));
    }

    private void loadQueueRow() {

        // Queue row
        mQueueAdapter = new ArrayObjectAdapter(new TrackCardPresenter());
        HeaderItem queueHeader = new HeaderItem(0, getString(R.string.queue), null);
        mRowsAdapter.add(new ListRow(queueHeader, mQueueAdapter));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BusProvider.register(this);
    }

    @Override
    public void onDestroy() {
        BusProvider.unregister(this);
        super.onDestroy();
    }
}
