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
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import com.sregg.android.tv.spotify.Constants;
import com.sregg.android.tv.spotify.R;
import com.sregg.android.tv.spotify.SpotifyTvApplication;
import com.sregg.android.tv.spotify.activities.SearchActivity;
import com.sregg.android.tv.spotify.enums.Control;
import com.sregg.android.tv.spotify.presenters.*;
import com.sregg.android.tv.spotify.settings.LastFmSetting;
import com.sregg.android.tv.spotify.settings.QualitySetting;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.*;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.util.*;


public class MainFragment extends BrowseFragment {
    private static final String TAG = "MainFragment";

    private SpotifyService mSpotifyService;
    private ArrayObjectAdapter mNewReleasesAdapter;
    private ArrayObjectAdapter mFeaturedPlaylistsAdapter;
    private ArrayObjectAdapter mPlaylistsAdapter;
    private ArrayObjectAdapter mSavedSongsAdapter;
    private ArrayObjectAdapter mSavedAlbumsAdapter;
    private ArrayObjectAdapter mSavedArtistsAdapter;
    private ArrayObjectAdapter mRowsAdapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onActivityCreated(savedInstanceState);

        SpotifyTvApplication app = SpotifyTvApplication.getInstance();
        mSpotifyService = app.getSpotifyService();

        setupUIElements();
        setupEventListeners();

        setupMainAdapter();

        setupFeaturedPlaylists();

        setupNewReleases();

        loadUserLibraryRows();

        loadControlsRow();

        loadSettingsRow();
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
                SpotifyTvApplication.getInstance().onItemClick(getActivity(), item);
            }
        });
    }

    private void setupFeaturedPlaylists() {
        mFeaturedPlaylistsAdapter = new ArrayObjectAdapter(new PlaylistSimpleCardPresenter());
        HeaderItem featuredPlaylistsHeader = new HeaderItem(0, getString(R.string.featured_playlists), null);
        mRowsAdapter.add(new ListRow(featuredPlaylistsHeader, mFeaturedPlaylistsAdapter));
    }

    private void loadFeaturedPlaylists(User user) {
        Map<String, Object> options = new HashMap<>();
        options.put(SpotifyService.COUNTRY, user.country);
        options.put("timestamp", DateFormat.format("yyyy-MM-dd'T'hh:mm:ss", new Date()));
        mSpotifyService.getFeaturedPlaylists(options, new Callback<FeaturedPlaylists>() {
            @Override
            public void success(FeaturedPlaylists featuredPlaylists, Response response) {
                mFeaturedPlaylistsAdapter.addAll(0, featuredPlaylists.playlists.items);
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private void setupNewReleases() {
        mNewReleasesAdapter = new ArrayObjectAdapter(new AlbumCardPresenter());
        HeaderItem newReleasesHeader = new HeaderItem(0, getString(R.string.new_releases), null);
        mRowsAdapter.add(new ListRow(newReleasesHeader, mNewReleasesAdapter));
    }

    private void loadNewReleases(User user) {
        Map<String, Object> options = new HashMap<>();
        options.put(SpotifyService.COUNTRY, user.country);
        mSpotifyService.getNewReleases(options, new Callback<NewReleases>() {
            @Override
            public void success(NewReleases newReleases, Response response) {
                mNewReleasesAdapter.addAll(0, newReleases.albums.items);
            }

            @Override
            public void failure(RetrofitError error) {

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
                loadFeaturedPlaylists(user);
                loadNewReleases(user);
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
        starredPlaylist.owner = user;
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
        ArrayObjectAdapter controlsAdapter = new ArrayObjectAdapter(controlPresenter);

        controlsAdapter.add(Control.SHUFFLE);
        controlsAdapter.add(Control.PREVIOUS);
        controlsAdapter.add(Control.PLAY);
        controlsAdapter.add(Control.PAUSE);
        controlsAdapter.add(Control.STOP);
        controlsAdapter.add(Control.NEXT);

        mRowsAdapter.add(new ListRow(controlsHeader, controlsAdapter));
    }

    private void loadSettingsRow() {
        HeaderItem settingsHeader = new HeaderItem(getString(R.string.settings), null);

        SettingPresenter settingPresenter = new SettingPresenter();
        ArrayObjectAdapter settingsAdapter = new ArrayObjectAdapter(settingPresenter);

        settingsAdapter.add(new QualitySetting());
        settingsAdapter.add(new LastFmSetting());

        mRowsAdapter.add(new ListRow(settingsHeader, settingsAdapter));
    }
}
