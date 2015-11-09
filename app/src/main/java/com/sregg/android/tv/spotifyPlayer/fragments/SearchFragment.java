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

import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.widget.*;
import android.text.TextUtils;
import android.util.Log;
import com.sregg.android.tv.spotifyPlayer.Constants;
import com.sregg.android.tv.spotifyPlayer.R;
import com.sregg.android.tv.spotifyPlayer.SpotifyTvApplication;
import com.sregg.android.tv.spotifyPlayer.controllers.SpotifyPlayerController;
import com.sregg.android.tv.spotifyPlayer.presenters.AlbumCardPresenter;
import com.sregg.android.tv.spotifyPlayer.presenters.ArtistCardPresenter;
import com.sregg.android.tv.spotifyPlayer.presenters.PlaylistCardPresenter;
import com.sregg.android.tv.spotifyPlayer.presenters.TrackCardPresenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.*;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/*
 * This class demonstrates how to do in-app search
 */
public class SearchFragment extends android.support.v17.leanback.app.SearchFragment
        implements android.support.v17.leanback.app.SearchFragment.SearchResultProvider {
    private static final String TAG = "SearchFragment";
    private static final int SEARCH_DELAY_MS = 300;

    private ArrayObjectAdapter mRowsAdapter;
    private Handler mHandler = new Handler();
    private SearchRunnable mDelayedLoad;
    private ArrayObjectAdapter mTrackRowAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        setSearchResultProvider(this);
        setOnItemViewClickedListener(new OnItemViewClickedListener() {
            @Override
            public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
                if (item instanceof Track) {
                    String trackUri = ((Track) item).uri;
                    SpotifyPlayerController spotifyPlayerController = SpotifyTvApplication.getInstance().getSpotifyPlayerController();
                    if (spotifyPlayerController.getPlayingState().isCurrentTrack(trackUri)) {
                        spotifyPlayerController.togglePauseResume();
                    } else {
                        // get song and following ones
                        List<String> trackUris = new ArrayList<>();
                        for (int i = mTrackRowAdapter.indexOf(item); i < mTrackRowAdapter.size() && i < Constants.MAX_SONGS_PLAYED; i++) {
                            trackUris.add(((Track) mTrackRowAdapter.get(i)).uri);
                        }
                        spotifyPlayerController.play(trackUri, trackUris);
                    }
                } else {
                    SpotifyTvApplication.getInstance().launchDetailScreen(getActivity(), item);
                }
            }
        });
        mDelayedLoad = new SearchRunnable();
    }

    @Override
    public ObjectAdapter getResultsAdapter() {
        return mRowsAdapter;
    }

    private void queryByWords(String words) {
        mRowsAdapter.clear();
        if (!TextUtils.isEmpty(words)) {
            mDelayedLoad.setSearchQuery(words);
            mHandler.removeCallbacks(mDelayedLoad);
            mHandler.postDelayed(mDelayedLoad, SEARCH_DELAY_MS);
        }
    }

    @Override
    public boolean onQueryTextChange(String newQuery) {
        Log.i(TAG, String.format("Search Query Text Change %s", newQuery));
        queryByWords(newQuery);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.i(TAG, String.format("Search Query Text Submit %s", query));
        queryByWords(query);
        return true;
    }

    private void loadRows(String query) {
        // search artists
        searchArtists(query);

        // search albums
        searchAlbums(query);

        // search songs
        searchSongs(query);

        // search playlists
        searchPlaylists(query);
    }

    private void searchArtists(String query) {
        SpotifyTvApplication.getInstance().getSpotifyService().searchArtists(query, getSearchOptions(), new Callback<ArtistsPager>() {
            @Override
            public void success(ArtistsPager artistsPager, Response response) {
                ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new ArtistCardPresenter());
                for (Artist artist : artistsPager.artists.items) {
                    listRowAdapter.add(artist);
                }
                HeaderItem header = new HeaderItem(getString(R.string.artists));
                mRowsAdapter.add(new ListRow(header, listRowAdapter));

                // TODO next pages ?
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private void searchAlbums(String query) {
        SpotifyTvApplication.getInstance().getSpotifyService().searchAlbums(query, getSearchOptions(), new Callback<AlbumsPager>() {
            @Override
            public void success(AlbumsPager albumsPager, Response response) {
                ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new AlbumCardPresenter());
                for (AlbumSimple artist : albumsPager.albums.items) {
                    listRowAdapter.add(artist);
                }
                HeaderItem header = new HeaderItem(getString(R.string.albums));
                mRowsAdapter.add(new ListRow(header, listRowAdapter));

                // TODO next pages ?
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private Map<String, Object> getSearchOptions() {
        Map<String, Object> options = new HashMap<>();
        options.put(SpotifyService.MARKET, Constants.MARKET_FROM_TOKEN);
        return options;
    }

    private void searchSongs(String query) {
        SpotifyTvApplication.getInstance().getSpotifyService().searchTracks(query, getSearchOptions(), new Callback<TracksPager>() {
            @Override
            public void success(TracksPager tracksPager, Response response) {
                mTrackRowAdapter = new ArrayObjectAdapter(new TrackCardPresenter());
                for (Track track : tracksPager.tracks.items) {
                    mTrackRowAdapter.add(track);
                }
                HeaderItem header = new HeaderItem(getString(R.string.songs));
                mRowsAdapter.add(new ListRow(header, mTrackRowAdapter));

                // TODO next pages ?
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }


    private void searchPlaylists(String query) {
        SpotifyTvApplication.getInstance().getSpotifyService().searchPlaylists(query, getSearchOptions(), new Callback<PlaylistsPager>() {
            @Override
            public void success(PlaylistsPager playlistsPager, Response response) {
                ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new PlaylistCardPresenter());
                for (PlaylistSimple playlistSimple : playlistsPager.playlists.items) {
                    listRowAdapter.add(playlistSimple);
                }
                HeaderItem header = new HeaderItem(getString(R.string.playlists));
                mRowsAdapter.add(new ListRow(header, listRowAdapter));

                // TODO next pages ?
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private class SearchRunnable implements Runnable {

        private volatile String searchQuery;

        public SearchRunnable() {
        }

        public void run() {
            loadRows(searchQuery);
        }

        public void setSearchQuery(String value) {
            this.searchQuery = value;
        }
    }
}
