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

import com.squareup.otto.Subscribe;
import com.sregg.android.tv.spotify.BusProvider;
import com.sregg.android.tv.spotify.Constants;
import com.sregg.android.tv.spotify.R;
import com.sregg.android.tv.spotify.SpotifyTvApplication;
import com.sregg.android.tv.spotify.activities.SearchActivity;
import com.sregg.android.tv.spotify.controllers.SpotifyPlayerController;
import com.sregg.android.tv.spotify.enums.Control;
import com.sregg.android.tv.spotify.presenters.*;
import com.sregg.android.tv.spotify.settings.CustomizeUiSetting;
import com.sregg.android.tv.spotify.settings.LastFmSetting;
import com.sregg.android.tv.spotify.settings.QualitySetting;
import com.sregg.android.tv.spotify.settings.Setting;
import com.sregg.android.tv.spotify.settings.UserPreferences;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.*;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.util.*;


public class MainFragment extends BrowseFragment {
    private static final String TAG = "MainFragment";

    private SpotifyService mSpotifyService;
    private ArrayObjectAdapter mNewReleasesAdapter = new ArrayObjectAdapter(new AlbumCardPresenter());
    private ArrayObjectAdapter mFeaturedPlaylistsAdapter = new ArrayObjectAdapter(new PlaylistSimpleCardPresenter());
    private ArrayObjectAdapter mCategoriesAdapter = new ArrayObjectAdapter(new CategoryCardPresenter());
    private ArrayObjectAdapter mPlaylistsAdapter = new ArrayObjectAdapter(new PlaylistCardPresenter());
    private ArrayObjectAdapter mSavedSongsAdapter = new ArrayObjectAdapter(new TrackCardPresenter());
    private ArrayObjectAdapter mSavedAlbumsAdapter = new ArrayObjectAdapter(new AlbumCardPresenter());
    private ArrayObjectAdapter mSavedArtistsAdapter = new ArrayObjectAdapter(new ArtistCardPresenter());
    private ArrayObjectAdapter mRowsAdapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onActivityCreated(savedInstanceState);

        SpotifyTvApplication app = SpotifyTvApplication.getInstance();
        mSpotifyService = app.getSpotifyService();

        setupUIElements();
        setupEventListeners();

        setupSections();
    }

    private void setupSections() {
        setupMainAdapter();

        setupFeaturedPlaylists();

        setupNewReleases();

        setupCategories();

        setupUserLibraryRows();

        loadControlsRow();

        loadSettingsRow();

        setAdapter(mRowsAdapter);
    }

    @Subscribe
    public void onCustomizeUiSettingChanged(CustomizeUiSetting.OnCustomizeUiSettingChanged event) {
        setupSections();
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
                if (item instanceof Setting) {
                    ((Setting) item).onClick(getActivity());
                } else if (item instanceof Control) {
                    SpotifyTvApplication.getInstance().getSpotifyPlayerController().onControlClick(((Control) item));
                } else if (item instanceof Track) {
                    String trackUri = ((Track) item).uri;
                    SpotifyPlayerController spotifyPlayerController = SpotifyTvApplication.getInstance().getSpotifyPlayerController();
                    if (spotifyPlayerController.getPlayingState().isCurrentTrack(trackUri)) {
                        spotifyPlayerController.togglePauseResume();
                    } else {
                        // get song and following ones
                        List<String> trackUris = new ArrayList<>();
                        for (int i = mSavedSongsAdapter.indexOf(item); i < mSavedSongsAdapter.size() && i < Constants.MAX_SONGS_PLAYED; i++) {
                            trackUris.add(((Track) mSavedSongsAdapter.get(i)).uri);
                        }
                        spotifyPlayerController.play(trackUri, trackUris);
                    }
                } else {
                    SpotifyTvApplication.getInstance().launchDetailScreen(getActivity(), item);
                }
            }
        });
    }

    private boolean isSectionEnabled(int sectionResId) {
        return UserPreferences.getInstance(getActivity()).isSectionEnabled(getString(sectionResId));
    }

    private void setupFeaturedPlaylists() {
        if (isSectionEnabled(R.string.featured_playlists)) {
            HeaderItem featuredPlaylistsHeader = new HeaderItem(0, getString(R.string.featured_playlists));
            mRowsAdapter.add(new ListRow(featuredPlaylistsHeader, mFeaturedPlaylistsAdapter));
            loadFeaturedPlaylists();
        }
    }

    private void loadFeaturedPlaylists() {
        Map<String, Object> options = new HashMap<>();
        options.put(SpotifyService.COUNTRY, SpotifyTvApplication.getCurrentUserCountry());
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
        if (isSectionEnabled(R.string.new_releases)) {
            HeaderItem newReleasesHeader = new HeaderItem(0, getString(R.string.new_releases));
            mRowsAdapter.add(new ListRow(newReleasesHeader, mNewReleasesAdapter));
            loadNewReleases();
        }
    }

    private void loadNewReleases() {
        Map<String, Object> options = new HashMap<>();
        options.put(SpotifyService.COUNTRY, SpotifyTvApplication.getCurrentUserCountry());
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

    private void setupCategories() {
        if (isSectionEnabled(R.string.categories)) {
            HeaderItem newReleasesHeader = new HeaderItem(0, getString(R.string.categories));
            mRowsAdapter.add(new ListRow(newReleasesHeader, mCategoriesAdapter));
            loadCategories();
        }
    }

    private void loadCategories() {
        Map<String, Object> options = new HashMap<>();
        options.put(SpotifyService.COUNTRY, SpotifyTvApplication.getCurrentUserCountry());
        mSpotifyService.getCategories(options, new Callback<CategoriesPager>() {
            @Override
            public void success(CategoriesPager categoriesPager, Response response) {
                mCategoriesAdapter.addAll(0, categoriesPager.categories.items);
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private void setupUserLibraryRows() {
        // playlist row
        if (isSectionEnabled(R.string.my_playlists)) {
            HeaderItem playListHeader = new HeaderItem(0, getString(R.string.my_playlists));
            mRowsAdapter.add(new ListRow(playListHeader, mPlaylistsAdapter));
            loadPlaylists();
        }

        // Albums row
        boolean showMyAlbums = isSectionEnabled(R.string.my_albums);
        if (showMyAlbums) {
            HeaderItem albumsHeader = new HeaderItem(0, getString(R.string.my_albums));
            mRowsAdapter.add(new ListRow(albumsHeader, mSavedAlbumsAdapter));
        }

        // Artists row
        boolean showMyArtists = isSectionEnabled(R.string.my_artists);
        if (showMyArtists) {
            HeaderItem artistsHeader = new HeaderItem(0, getString(R.string.my_artists));
            mRowsAdapter.add(new ListRow(artistsHeader, mSavedArtistsAdapter));
        }

        // Songs row
        boolean showMySongs = isSectionEnabled(R.string.my_songs);
        if (showMySongs) {
            HeaderItem songsHeader = new HeaderItem(0, getString(R.string.my_songs));
            mRowsAdapter.add(new ListRow(songsHeader, mSavedSongsAdapter));
        }

        // load saved songs
        if (showMyAlbums || showMyArtists || showMySongs) {
            loadSavedSongs();
        }

    }
    private void loadPlaylists() {
        mPlaylistsAdapter.clear();

        mSpotifyService.getPlaylists(SpotifyTvApplication.getCurrentUserId(), new Callback<Pager<Playlist>>() {
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

        mSpotifyService.getMySavedTracks(new Callback<Pager<SavedTrack>>() {
            @Override
            public void success(Pager<SavedTrack> savedTrackPager, Response response) {
                ArrayList<Track> songs = new ArrayList<>();
                ArrayList<AlbumSimple> albums = new ArrayList<>();
                ArrayList<ArtistSimple> artists = new ArrayList<>();

                final List<String> albumIds = new ArrayList<>();
                final List<String> artistIds = new ArrayList<>();

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
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private void loadControlsRow() {
        HeaderItem controlsHeader = new HeaderItem(getString(R.string.controls));

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
        HeaderItem settingsHeader = new HeaderItem(getString(R.string.settings));

        SettingPresenter settingPresenter = new SettingPresenter();
        ArrayObjectAdapter settingsAdapter = new ArrayObjectAdapter(settingPresenter);

        settingsAdapter.add(new QualitySetting());
        settingsAdapter.add(new LastFmSetting());
        settingsAdapter.add(new CustomizeUiSetting());

        mRowsAdapter.add(new ListRow(settingsHeader, settingsAdapter));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BusProvider.getInstance().register(this);
    }

    @Override
    public void onDestroy() {
        BusProvider.getInstance().unregister(this);

        super.onDestroy();
    }
}
