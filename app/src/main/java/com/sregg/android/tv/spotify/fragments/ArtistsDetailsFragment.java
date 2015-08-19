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
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v7.graphics.Palette;
import android.util.DisplayMetrics;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.sregg.android.tv.spotify.Constants;
import com.sregg.android.tv.spotify.R;
import com.sregg.android.tv.spotify.SpotifyTvApplication;
import com.sregg.android.tv.spotify.activities.ArtistsAlbumsActivity;
import com.sregg.android.tv.spotify.controllers.SpotifyPlayerController;
import com.sregg.android.tv.spotify.presenters.AlbumCardPresenter;
import com.sregg.android.tv.spotify.presenters.ArtistCardPresenter;
import com.sregg.android.tv.spotify.presenters.TrackCardPresenter;
import com.sregg.android.tv.spotify.utils.BlurTransformation;
import com.sregg.android.tv.spotify.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Artists;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/*
 * Show a Grid of an Artist's Albums
 */
public class ArtistsDetailsFragment extends BrowseFragment {
    private static final String TAG = "ArtistsDetailsFragment";
    private BackgroundManager mBackgroundManager;
    private DisplayMetrics mMetrics;
    private Handler mHandler;
    private BackgroundTarget mTarget;
    private ArrayObjectAdapter mRowsAdapter;
    private ArrayObjectAdapter mTopTrackAdapter;
    private SpotifyService mSpotifyService;
    private ArrayObjectAdapter mRelatedArtistsAdapter;

    private enum AlbumType {
        ALBUM, SINGLE, APPEARS_ON, COMPILATION
    }

    private String mArtistId;

    private Map<AlbumType, ArrayObjectAdapter> mAlbumsAdapters;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        mHandler = new Handler();

        Intent intent = getActivity().getIntent();

        mArtistId = intent.getStringExtra(ArtistsAlbumsActivity.ARG_ARTIST_ID);

        String artistName = intent.getStringExtra(ArtistsAlbumsActivity.ARG_ARTIST_NAME);
        setTitle(artistName);

        mSpotifyService = SpotifyTvApplication.getInstance().getSpotifyService();

        setupFragment();

        setupBackground();
    }

    private void setupFragment() {
        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        setAdapter(mRowsAdapter);

        setupTopTracksRow();

        setupAlbumsRows();

        setupRelatedArtistsRows();

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
                        for (int i = mTopTrackAdapter.indexOf(item); i < mTopTrackAdapter.size() && i < Constants.MAX_SONGS_PLAYED; i++) {
                            trackUris.add(((Track) mTopTrackAdapter.get(i)).uri);
                        }
                        spotifyPlayerController.play(trackUri, trackUris);
                    }
                } else {
                    SpotifyTvApplication.getInstance().launchDetailScreen(getActivity(), item);
                }
            }
        });
    }

    private void setupAlbumsRows() {
        AlbumType[] albumTypes = AlbumType.values();

        mAlbumsAdapters = new HashMap<>(albumTypes.length);

        for (AlbumType albumType : albumTypes) {
            ArrayObjectAdapter adapter = new ArrayObjectAdapter(new AlbumCardPresenter());
            String headerResourceName = "artists_album_type_" + albumType.name().toLowerCase();
            HeaderItem header = new HeaderItem(0, Utils.getStringByName(getActivity(), headerResourceName));
            mRowsAdapter.add(new ListRow(header, adapter));
            mAlbumsAdapters.put(albumType, adapter);
        }

        loadAlbums(0);
    }

    private void loadAlbums(final int offset) {

        HashMap<String, Object> options = new HashMap<>();
        options.put(SpotifyService.OFFSET, Integer.toString(offset));
        options.put(SpotifyService.LIMIT, Integer.toString(Constants.PAGE_LIMIT));
        options.put(SpotifyService.MARKET, SpotifyTvApplication.getCurrentUserCountry());
        mSpotifyService.getArtistAlbums(mArtistId, options, new Callback<Pager<Album>>() {
            @Override
            public void success(Pager<Album> albumPager, Response response) {
                for (Album album : albumPager.items) {
                    try {
                        AlbumType albumType = AlbumType.valueOf(album.album_type.toUpperCase());

                        // add to corresponding adapter
                        ArrayObjectAdapter adapter = mAlbumsAdapters.get(albumType);
                        adapter.add(adapter.size(), album);
                    } catch (IllegalArgumentException e) {
                        Log.d(TAG, String.format("Album Type %s is unknown", album.album_type), e);
                    }
                }

                if (albumPager.next != null) {
                    loadAlbums(albumPager.offset + Constants.PAGE_LIMIT);
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private void setupTopTracksRow() {
        mTopTrackAdapter = new ArrayObjectAdapter(new TrackCardPresenter());
        HeaderItem header = new HeaderItem(0, getString(R.string.artists_top_tracks));
        mRowsAdapter.add(new ListRow(header, mTopTrackAdapter));

        loadTopTracks();
    }

    private void loadTopTracks() {
        mSpotifyService.getArtistTopTrack(mArtistId, SpotifyTvApplication.getCurrentUserCountry(), new Callback<Tracks>() {
            @Override
            public void success(Tracks tracks, Response response) {
                mTopTrackAdapter.addAll(0, tracks.tracks);
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private void setupRelatedArtistsRows() {
        mRelatedArtistsAdapter = new ArrayObjectAdapter(new ArtistCardPresenter());
        HeaderItem header = new HeaderItem(0, getString(R.string.artists_related_artists));
        mRowsAdapter.add(new ListRow(header, mRelatedArtistsAdapter));

        loadRelatedArtists();
    }

    private void loadRelatedArtists() {
        mSpotifyService.getRelatedArtists(mArtistId, new Callback<Artists>() {
            @Override
            public void success(Artists artists, Response response) {
                mRelatedArtistsAdapter.addAll(0, artists.artists);
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private void setupBackground() {
        mBackgroundManager = BackgroundManager.getInstance(getActivity());
        mBackgroundManager.attach(getActivity().getWindow());
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);

        // load artist from API to get their image
        mSpotifyService.getArtist(mArtistId, new Callback<Artist>() {
            @Override
            public void success(Artist artist, Response response) {
                if (artist.images != null && !artist.images.isEmpty()) {
                    final String imageUrl = artist.images.get(0).url;

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            loadBackgroundImage(imageUrl);
                        }
                    });
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private void loadBackgroundImage(String imageUrl) {
        mTarget = new BackgroundTarget();

        Picasso.with(getActivity())
                .load(imageUrl)
                .transform(new BlurTransformation(getActivity()))
                .resize(mMetrics.widthPixels, mMetrics.heightPixels)
                .centerCrop()
                .into(mTarget);
    }

    private void setBackgroundBitmap(Bitmap bitmap) {
        Drawable bitmapDrawable = new BitmapDrawable(getResources(), bitmap);
        mBackgroundManager.setDrawable(bitmapDrawable);
    }

    private class BackgroundTarget implements Target {
        @Override
        public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    setBackgroundBitmap(bitmap);
                }
            });

            // set background based on the color palette
            Palette.generateAsync(bitmap, new Palette.PaletteAsyncListener() {
                public void onGenerated(Palette palette) {
                    setFastLaneBackgroundColor(palette);
                }
            });
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    }

    private void setFastLaneBackgroundColor(Palette palette) {
        Palette.Swatch swatch = palette.getDarkVibrantSwatch();

        if (swatch == null) {
            swatch = palette.getDarkMutedSwatch();
        }

        if (swatch != null) {
            setBrandColor(swatch.getRgb());
        }
    }
}
