package com.sregg.android.tv.spotify.providers;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import com.sregg.android.tv.spotify.Constants;
import com.sregg.android.tv.spotify.R;
import com.sregg.android.tv.spotify.SpotifyTvApplication;

import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.AlbumsPager;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.PlaylistsPager;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;

public class SearchProvider extends ContentProvider {

    private static final String[] CURSOR_COLUMNS = {
            SearchManager.SUGGEST_COLUMN_TEXT_1, // Required
            SearchManager.SUGGEST_COLUMN_CONTENT_TYPE, // Required
            SearchManager.SUGGEST_COLUMN_PRODUCTION_YEAR, // Required
            SearchManager.SUGGEST_COLUMN_RESULT_CARD_IMAGE,
            SearchManager.SUGGEST_COLUMN_TEXT_2,
            SearchManager.SUGGEST_COLUMN_DURATION,
            SearchManager.SUGGEST_COLUMN_INTENT_DATA,
    };

    @Override
    public String getType(Uri uri) {
        return SearchManager.SUGGEST_MIME_TYPE;
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String query = selectionArgs[0];
        MatrixCursor cursor = new MatrixCursor(CURSOR_COLUMNS);

        searchArtists(query, cursor);
        searchAlbums(query, cursor);
        searchTracks(query, cursor);
        searchPlaylists(query, cursor);

        return cursor;
    }

    private void searchArtists(String query, MatrixCursor cursor) {
        ArtistsPager artistsPager = SpotifyTvApplication.getInstance().getSpotifyService().searchArtists(query, getSearchOptions());

        if (artistsPager != null && artistsPager.artists != null) {
            for (Artist artist : artistsPager.artists.items) {
                MatrixCursor.RowBuilder row = cursor.newRow();

                row.add(SearchManager.SUGGEST_COLUMN_TEXT_1, artist.name);
                row.add(SearchManager.SUGGEST_COLUMN_CONTENT_TYPE, artist.type);
                row.add(SearchManager.SUGGEST_COLUMN_INTENT_DATA, artist.uri);
                row.add(SearchManager.SUGGEST_COLUMN_TEXT_2, getContext().getString(R.string.artists));

                if (artist.images != null && artist.images.size() > 0) {
                    row.add(SearchManager.SUGGEST_COLUMN_RESULT_CARD_IMAGE, artist.images.get(0).url);
                }
            }
        }
    }

    private void searchAlbums(String query, MatrixCursor cursor) {
        AlbumsPager albumsPager = SpotifyTvApplication.getInstance().getSpotifyService().searchAlbums(query, getSearchOptions());

        if (albumsPager != null && albumsPager.albums != null) {
            for (AlbumSimple album : albumsPager.albums.items) {
                MatrixCursor.RowBuilder row = cursor.newRow();

                row.add(SearchManager.SUGGEST_COLUMN_TEXT_1, album.name);
                row.add(SearchManager.SUGGEST_COLUMN_CONTENT_TYPE, album.type);
                row.add(SearchManager.SUGGEST_COLUMN_INTENT_DATA, album.uri);
                row.add(SearchManager.SUGGEST_COLUMN_TEXT_2, getContext().getString(R.string.albums));

                if (album.images != null && album.images.size() > 0) {
                    row.add(SearchManager.SUGGEST_COLUMN_RESULT_CARD_IMAGE, album.images.get(0).url);
                }
            }
        }
    }

    private void searchTracks(String query, MatrixCursor cursor) {
        TracksPager tracksPager = SpotifyTvApplication.getInstance().getSpotifyService().searchTracks(query, getSearchOptions());

        if (tracksPager != null && tracksPager.tracks != null) {
            for (Track track : tracksPager.tracks.items) {
                MatrixCursor.RowBuilder row = cursor.newRow();

                row.add(SearchManager.SUGGEST_COLUMN_TEXT_1, track.name);
                row.add(SearchManager.SUGGEST_COLUMN_CONTENT_TYPE, track.type);
                row.add(SearchManager.SUGGEST_COLUMN_DURATION, track.duration_ms);
                row.add(SearchManager.SUGGEST_COLUMN_INTENT_DATA, track.uri);

                if (track.artists != null && track.artists.size() > 0) {
                    row.add(SearchManager.SUGGEST_COLUMN_TEXT_2, track.artists.get(0).name);
                }

                if (track.album != null && track.album.images != null && track.album.images.size() > 0) {
                    row.add(SearchManager.SUGGEST_COLUMN_RESULT_CARD_IMAGE, track.album.images.get(0).url);
                }
            }
        }
    }

    private void searchPlaylists(String query, MatrixCursor cursor) {
        PlaylistsPager playlistsPager = SpotifyTvApplication.getInstance().getSpotifyService().searchPlaylists(query, getSearchOptions());

        if (playlistsPager != null && playlistsPager.playlists != null) {
            for (PlaylistSimple playlist : playlistsPager.playlists.items) {
                MatrixCursor.RowBuilder row = cursor.newRow();

                row.add(SearchManager.SUGGEST_COLUMN_TEXT_1, playlist.name);
                row.add(SearchManager.SUGGEST_COLUMN_CONTENT_TYPE, playlist.type);
                row.add(SearchManager.SUGGEST_COLUMN_INTENT_DATA, playlist.uri);
                row.add(SearchManager.SUGGEST_COLUMN_TEXT_2, getContext().getString(R.string.playlists));

                if (playlist.images != null && playlist.images.size() > 0) {
                    row.add(SearchManager.SUGGEST_COLUMN_RESULT_CARD_IMAGE, playlist.images.get(0).url);
                }
            }
        }
    }

    private Map<String, Object> getSearchOptions() {
        Map<String, Object> options = new HashMap<>();
        options.put(SpotifyService.MARKET, Constants.MARKET_FROM_TOKEN);
        return options;
    }

    /* Below are Provider Methods not used by search */

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
