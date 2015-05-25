package com.sregg.android.tv.spotify.utils;

import com.sregg.android.tv.spotify.SpotifyTvApplication;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.Track;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SpotifyUriLoader {

    private static final String ALBUM_URI = "spotify:album:";
    private static final String ARTIST_URI = "spotify:artist:";
    private static final String TRACK_URI = "spotify:track:";
    private static final String USER_URI = "spotify:user:";
    private static final String USER_PLAYLIST_URI = ":playlist:";

    public static void loadObjectFromUri(final SpotifyService service, final String uri, final SpotifyObjectLoaderCallback callback) {
        if (uri.startsWith(ALBUM_URI)) {
            loadAlbumFromUri(service, uri, callback);
        } else if (uri.startsWith(ARTIST_URI)) {
            loadArtistFromUri(service, uri, callback);
        } else if (uri.startsWith(USER_URI) && uri.contains(USER_PLAYLIST_URI)) {
            loadPlaylistFromUri(service, uri, callback);
        } else if (uri.startsWith(TRACK_URI)) {
            loadTrackFromUri(service, uri, callback);
        }
    }

    private static void loadAlbumFromUri(SpotifyService service, String uri, final SpotifyObjectLoaderCallback callback) {
        SpotifyTvApplication.getInstance().getSpotifyService().getAlbum(uri.replace(ALBUM_URI, ""), new Callback<Album>() {
            @Override
            public void success(Album album, Response response) {
                if (callback != null) {
                    callback.success(album, response);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                if (callback != null) {
                    callback.failure(error);
                }
            }
        });
    }

    private static void loadArtistFromUri(SpotifyService service, String uri, final SpotifyObjectLoaderCallback callback) {
        SpotifyTvApplication.getInstance().getSpotifyService().getArtist(uri.replace(ARTIST_URI, ""), new Callback<Artist>() {
            @Override
            public void success(Artist artist, Response response) {
                if (callback != null) {
                    callback.success(artist, response);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                if (callback != null) {
                    callback.failure(error);
                }
            }
        });
    }

    private static void loadPlaylistFromUri(SpotifyService service, String uri, final SpotifyObjectLoaderCallback callback) {
        String[] uriParts = uri.split(USER_PLAYLIST_URI);
        if (uriParts.length == 2) {
            String userId = uriParts[0].replace(USER_URI, "");
            String playlistId = uriParts[1];
            SpotifyTvApplication.getInstance().getSpotifyService().getPlaylist(userId, playlistId, new Callback<Playlist>() {
                @Override
                public void success(Playlist playlist, Response response) {
                    if (callback != null) {
                        callback.success(playlist, response);
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    if (callback != null) {
                        callback.failure(error);
                    }
                }
            });
        }
    }

    private static void loadTrackFromUri(SpotifyService service, String uri, final SpotifyObjectLoaderCallback callback) {
        SpotifyTvApplication.getInstance().getSpotifyService().getTrack(uri.replace(TRACK_URI, ""), new Callback<Track>() {
            @Override
            public void success(Track track, Response response) {
                if (callback != null) {
                    callback.success(track, response);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                if (callback != null) {
                    callback.failure(error);
                }
            }
        });
    }

    public interface SpotifyObjectLoaderCallback {
        public void success(Object object, Response response);
        public void failure(RetrofitError error);
    }

}
