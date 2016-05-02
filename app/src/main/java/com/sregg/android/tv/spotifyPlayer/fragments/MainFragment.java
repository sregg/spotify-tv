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

package com.sregg.android.tv.spotifyPlayer.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;

import com.squareup.otto.Subscribe;
import com.sregg.android.tv.spotifyPlayer.BusProvider;
import com.sregg.android.tv.spotifyPlayer.Constants;
import com.sregg.android.tv.spotifyPlayer.R;
import com.sregg.android.tv.spotifyPlayer.SpotifyTvApplication;
import com.sregg.android.tv.spotifyPlayer.activities.NowPlayingActivity;
import com.sregg.android.tv.spotifyPlayer.activities.SearchActivity;
import com.sregg.android.tv.spotifyPlayer.adapters.PagingAdapter;
import com.sregg.android.tv.spotifyPlayer.controllers.SpotifyPlayerController;
import com.sregg.android.tv.spotifyPlayer.enums.Control;
import com.sregg.android.tv.spotifyPlayer.events.OnTrackChanged;
import com.sregg.android.tv.spotifyPlayer.events.ContentState;
import com.sregg.android.tv.spotifyPlayer.presenters.AlbumCardPresenter;
import com.sregg.android.tv.spotifyPlayer.presenters.ArtistCardPresenter;
import com.sregg.android.tv.spotifyPlayer.presenters.CategoryCardPresenter;
import com.sregg.android.tv.spotifyPlayer.presenters.ControlPresenter;
import com.sregg.android.tv.spotifyPlayer.presenters.PlaylistCardPresenter;
import com.sregg.android.tv.spotifyPlayer.presenters.PlaylistSimpleCardPresenter;
import com.sregg.android.tv.spotifyPlayer.presenters.SettingPresenter;
import com.sregg.android.tv.spotifyPlayer.presenters.TrackCardPresenter;
import com.sregg.android.tv.spotifyPlayer.settings.CustomizeUiSetting;
import com.sregg.android.tv.spotifyPlayer.settings.LastFmSetting;
import com.sregg.android.tv.spotifyPlayer.settings.QualitySetting;
import com.sregg.android.tv.spotifyPlayer.settings.Setting;
import com.sregg.android.tv.spotifyPlayer.settings.UserPreferences;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.CategoriesPager;
import kaaes.spotify.webapi.android.models.FeaturedPlaylists;
import kaaes.spotify.webapi.android.models.NewReleases;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.SavedTrack;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TrackSimple;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MainFragment extends BrowseFragment {
    private static final String TAG = "MainFragment";

    private ArrayObjectAdapter mNewReleasesAdapter = new ArrayObjectAdapter(new AlbumCardPresenter());
    private ArrayObjectAdapter mNowPlayingAdapter = new ArrayObjectAdapter(new TrackCardPresenter());
    private ArrayObjectAdapter mFeaturedPlaylistsAdapter = new ArrayObjectAdapter(new PlaylistSimpleCardPresenter());
    private ArrayObjectAdapter mCategoriesAdapter = new ArrayObjectAdapter(new CategoryCardPresenter());
    private PagingAdapter mPlaylistsAdapter;
    private ArrayObjectAdapter mSavedSongsAdapter = new ArrayObjectAdapter(new TrackCardPresenter());
    private ArrayObjectAdapter mSavedAlbumsAdapter = new ArrayObjectAdapter(new AlbumCardPresenter());
    private ArrayObjectAdapter mSavedArtistsAdapter = new ArrayObjectAdapter(new ArtistCardPresenter());
    private ArrayObjectAdapter mRowsAdapter;
    private HeaderItem mNowPlayingHeader;

    private ListRow mNowPlayingListRow;
    private boolean mPlaylistsLoading;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onActivityCreated(savedInstanceState);

        setupUIElements();
        setupEventListeners();

        setupSections();
    }

    private void setupSections() {
        setupMainAdapter();

        setupNowPlaying();

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
                if (row.getHeaderItem() == mNowPlayingHeader) {
                    startActivity(new Intent(getActivity(), NowPlayingActivity.class));
                } else if (item instanceof Setting) {
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
                        List<TrackSimple> tracks = new ArrayList<TrackSimple>();
                        List<String> trackUris = new ArrayList<>();
                        for (int i = mSavedSongsAdapter.indexOf(item); i < mSavedSongsAdapter.size() && i < Constants.MAX_SONGS_PLAYED; i++) {
                            tracks.add((TrackSimple) mSavedSongsAdapter.get(i));
                            trackUris.add(((Track) mSavedSongsAdapter.get(i)).uri);
                        }
                        spotifyPlayerController.play(trackUri, trackUris, tracks);
                    }
                } else {
                    SpotifyTvApplication.getInstance().launchDetailScreen(getActivity(), item);
                }
            }
        });

        setOnItemViewSelectedListener(new OnItemViewSelectedListener() {
            @Override
            public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
                if (row instanceof ListRow && ((ListRow) row).getAdapter() instanceof PagingAdapter) {
                    PagingAdapter pagingAdapter = (PagingAdapter) ((ListRow) row).getAdapter();
                    pagingAdapter.onItemSelected(item);
                }
            }
        });
    }

    private boolean isSectionEnabled(int sectionResId) {
        return UserPreferences.getInstance(getActivity()).isSectionEnabled(getString(sectionResId));
    }

    private SpotifyService getSpotifyService() {
        return SpotifyTvApplication.getInstance().getSpotifyService();
    }

    private void setupNowPlaying() {
        mNowPlayingHeader = new HeaderItem(0, getString(R.string.now_playing));

        TrackSimple currentTrack = SpotifyTvApplication.getInstance().getSpotifyPlayerController().getPlayingState().getCurrentTrack();
        if (currentTrack != null) {
            mNowPlayingAdapter.removeItems(0, mNowPlayingAdapter.size());
            mNowPlayingAdapter.add(currentTrack);
            mNowPlayingListRow = new ListRow(mNowPlayingHeader, mNowPlayingAdapter);
            mRowsAdapter.add(0, mNowPlayingListRow);
        } else {
            mNowPlayingListRow = null;
        }
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
        getSpotifyService().getFeaturedPlaylists(options, new Callback<FeaturedPlaylists>() {
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
        getSpotifyService().getNewReleases(options, new Callback<NewReleases>() {
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
        getSpotifyService().getCategories(options, new Callback<CategoriesPager>() {
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
            mPlaylistsAdapter = new PagingAdapter(new PlaylistCardPresenter()) {
                @Override
                public void onLoadMore(int offset) {
                    loadPlaylists(offset);
                }
            };

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
        loadPlaylists(0);
    }

    private void loadPlaylists(int offset) {
        if (mPlaylistsLoading){
            return;
        }

        mPlaylistsLoading = true;
        Map<String, Object> options = new HashMap<>();
        options.put(SpotifyService.OFFSET, offset);
        options.put(SpotifyService.LIMIT, Constants.PAGE_LIMIT);
        getSpotifyService().getPlaylists(SpotifyTvApplication.getCurrentUserId(), options, new Callback<Pager<PlaylistSimple>>() {
            @Override
            public void success(Pager<PlaylistSimple> playlistPager, Response response) {
                mPlaylistsAdapter.addAll(playlistPager.total, mPlaylistsAdapter.size(), playlistPager.items);
                mPlaylistsLoading = false;
            }

            @Override
            public void failure(RetrofitError error) {
                mPlaylistsLoading = false;
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
        getSpotifyService().getMySavedTracks(options, new Callback<Pager<SavedTrack>>() {
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

    @SuppressWarnings("unused")
    @Subscribe
    public void onTrackChanged(OnTrackChanged onTrackChanged) {
        ContentState contentState = onTrackChanged.getPlayingState();
        mNowPlayingAdapter.removeItems(0, mNowPlayingAdapter.size());
        if (contentState.getCurrentTrack() != null) {
            mNowPlayingAdapter.add(0, contentState.getCurrentTrack());
        }

        if (mNowPlayingListRow == null) {
            mNowPlayingListRow = new ListRow(mNowPlayingHeader, mNowPlayingAdapter);
            mRowsAdapter.add(0, mNowPlayingListRow);
        }
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
