package com.sregg.android.tv.spotify.controllers;

import android.graphics.Bitmap;
import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.PlayerStateCallback;
import com.spotify.sdk.android.player.Spotify;
import com.squareup.picasso.Picasso;
import com.sregg.android.tv.spotify.BusProvider;
import com.sregg.android.tv.spotify.SpotifyTvApplication;
import com.sregg.android.tv.spotify.enums.Control;
import com.sregg.android.tv.spotify.events.OnPause;
import com.sregg.android.tv.spotify.events.OnPlay;
import com.sregg.android.tv.spotify.events.OnQueueChanged;
import com.sregg.android.tv.spotify.events.OnShuffleChanged;
import com.sregg.android.tv.spotify.events.OnTrackEnd;
import com.sregg.android.tv.spotify.events.OnTrackStart;
import com.sregg.android.tv.spotify.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import kaaes.spotify.webapi.android.models.Track;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * <p>Plays tracks, playlists and albums into the Spotify SDK</p>
 * <p>Fires OTTO playback events like {@link com.sregg.android.tv.spotify.events.OnPlay}, {@link com.sregg.android.tv.spotify.events.OnPause}, etc..</p>
 * <p>Also updates the Now Playing Card in the Home Screen</p>
 */
public class SpotifyPlayerController implements PlayerNotificationCallback, ConnectionStateCallback {

    public static final String TAG = "SpotifyPlayerController";
    private final Player mPlayer;
    private final MediaSession mNowPlayingSession;
    private final SpotifyService mSpotifyService;

    // can be a playlist, an album, an artist or a track
    private Object mCurrentSpotifyObject;

    private boolean mIsShuffleOn = false;

    public SpotifyPlayerController(Player player, SpotifyService spotifyService) {
        mPlayer = player;

        mPlayer.addPlayerNotificationCallback(this);
        mPlayer.addConnectionStateCallback(this);

        mNowPlayingSession = new MediaSession(SpotifyTvApplication.getInstance().getApplicationContext(), TAG);
        mNowPlayingSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // for the MediaBrowserService
        //setSessionToken(mNowPlayingSession.getSessionToken());

        mSpotifyService = spotifyService;
    }

    public void play(Object spotifyObject) {
        mCurrentSpotifyObject = spotifyObject;

        if (spotifyObject instanceof Track) {
            Track track = (Track) spotifyObject;
            playTracks(Collections.singletonList(track));
        } else if (spotifyObject instanceof Playlist) {
            // get playlist's tracks
            Playlist playlist = (Playlist) spotifyObject;
            SpotifyTvApplication.getInstance().getSpotifyService().getPlaylistTracks(SpotifyTvApplication.getInstance().getCurrentUserId(), playlist.id, new Callback<Pager<PlaylistTrack>>() {
                @Override
                public void success(Pager<PlaylistTrack> trackPager, Response response) {
                    // transform PlaylistTracks into Tracks
                    List<Track> tracks = new ArrayList<>(trackPager.items.size());
                    for (PlaylistTrack playlistTrack : trackPager.items) {
                        tracks.add(playlistTrack.track);
                    }
                    playTracks(tracks);
                }

                @Override
                public void failure(RetrofitError error) {

                }
            });
        } else if (spotifyObject instanceof AlbumSimple) {
            // get album's tracks
            AlbumSimple albumSimple = (AlbumSimple) spotifyObject;
            SpotifyTvApplication.getInstance().getSpotifyService().getAlbumTracks(albumSimple.id, new Callback<Pager<Track>>() {
                @Override
                public void success(Pager<Track> trackPager, Response response) {
                    playTracks(trackPager.items);
                }

                @Override
                public void failure(RetrofitError error) {

                }
            });
        }

        if (!mNowPlayingSession.isActive()) {
            mNowPlayingSession.setActive(true);
        }
    }

    private void playTracks(final List<Track> tracks) {
        // play track uris in Player
        List<String> trackUris = new ArrayList<>(tracks.size());
        for (Track track : tracks) {
            trackUris.add(track.uri);
        }
        mPlayer.play(trackUris);

        // update current queue (on UI thread)
        new Handler(Looper.getMainLooper())
                .post(new Runnable() {
                    @Override
                    public void run() {
                        BusProvider.post(new OnQueueChanged(tracks));

                    }
                });
    }

    public void togglePauseResume() {
        mPlayer.getPlayerState(new PlayerStateCallback() {
            @Override
            public void onPlayerState(PlayerState playerState) {
                if (playerState.playing) {
                    mPlayer.pause();
                } else {
                    mPlayer.resume();
                }
            }
        });
    }

    public String getCurrentObjectUri() {
        return Utils.getUriFromSpotiyObject(mCurrentSpotifyObject);
    }

    public boolean isShuffleOn() {
        return mIsShuffleOn;
    }

    public void terminate() {
        if (mNowPlayingSession.isActive()) {
            mNowPlayingSession.setActive(false);
        }
        Spotify.destroyPlayer(mPlayer);
    }

    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
        Log.d(TAG, String.format("%s - isPlaying: %s - trackUri: %s - positionInMs:%s",
                eventType.name(), playerState.playing, playerState.trackUri, playerState.positionInMs));

        String currentObjectUri = getCurrentObjectUri();

        switch (eventType) {
            case PLAY:
                BusProvider.post(new OnPlay(currentObjectUri));
                break;
            case PAUSE:
                BusProvider.post(new OnPause(currentObjectUri));
                break;
            case TRACK_START:
                BusProvider.post(new OnTrackStart(currentObjectUri));
                updateNowPlayingMetadata(playerState.trackUri);
                break;
            case TRACK_END:
                BusProvider.post(new OnTrackEnd(currentObjectUri));
                break;
            case END_OF_CONTEXT:
                if (mNowPlayingSession.isActive()) {
                    mNowPlayingSession.setActive(false);
                }
                mCurrentSpotifyObject = null;
                break;
        }
    }

    private void updateNowPlayingMetadata(String currentTrackUri) {
        // laod track from id (from uri)
        mSpotifyService.getTrack(Utils.getIdFromUri(currentTrackUri), new Callback<Track>() {
            @Override
            public void success(final Track track, Response response) {
                MediaMetadata.Builder metadataBuilder = new MediaMetadata.Builder();

                // track title
                metadataBuilder.putString(MediaMetadata.METADATA_KEY_TITLE,
                        track.name);

                // artists
                metadataBuilder.putString(MediaMetadata.METADATA_KEY_ARTIST,
                        Utils.getTrackArtists(track));

                // album picture
                try {
                    Bitmap bitmap = Picasso.with(SpotifyTvApplication.getInstance().getApplicationContext())
                            .load(track.album.images.get(0).url)
                            .get();
                    metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ART,
                            bitmap);
                } catch (IOException e) {
                    Log.e(TAG, "Error downloading track picture for now playing card", e);
                }

                mNowPlayingSession.setMetadata(metadataBuilder.build());
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    @Override
    public void onPlaybackError(ErrorType errorType, String s) {

    }

    @Override
    public void onLoggedIn() {

    }

    @Override
    public void onLoggedOut() {

    }

    @Override
    public void onLoginFailed(Throwable throwable) {

    }

    @Override
    public void onTemporaryError() {

    }

    @Override
    public void onConnectionMessage(String s) {

    }

    public void onControlClick(Control control) {
        switch (control) {
            case PLAY:
                mPlayer.resume();
                break;
            case PAUSE:
                mPlayer.pause();
                break;
            case NEXT:
                mPlayer.skipToNext();
                break;
            case PREVIOUS:
                mPlayer.skipToPrevious();
                break;
            case STOP:
                if (mNowPlayingSession.isActive()) {
                    mNowPlayingSession.setActive(false);
                }
                mPlayer.pause();
                mPlayer.clearQueue();
                BusProvider.post(new OnTrackEnd(getCurrentObjectUri()));
                mCurrentSpotifyObject = null;
                break;
            case SHUFFLE:
                mIsShuffleOn = !mIsShuffleOn;
                mPlayer.setShuffle(mIsShuffleOn);
                BusProvider.post(new OnShuffleChanged(mIsShuffleOn));

                // reload current object if not null
                if (mCurrentSpotifyObject != null) {
                    play(mCurrentSpotifyObject);
                }
                break;
        }
    }
}
