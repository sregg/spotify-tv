package com.sregg.android.tv.spotify.controllers;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.text.TextUtils;
import android.util.Log;
import com.spotify.sdk.android.player.*;
import com.squareup.picasso.Picasso;
import com.sregg.android.tv.spotify.BusProvider;
import com.sregg.android.tv.spotify.SpotifyTvApplication;
import com.sregg.android.tv.spotify.enums.Control;
import com.sregg.android.tv.spotify.events.*;
import com.sregg.android.tv.spotify.settings.UserPreferences;
import com.sregg.android.tv.spotify.utils.Utils;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.*;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    private PlayingState mPlayingState;

    private boolean mIsShuffleOn = false;

    public SpotifyPlayerController(Player player, SpotifyService spotifyService) {
        Context context = SpotifyTvApplication.getInstance().getApplicationContext();

        mPlayer = player;

        mPlayer.addPlayerNotificationCallback(this);
        mPlayer.addConnectionStateCallback(this);
        setPlayerBitrate(UserPreferences.getInstance(context).getBitrate());

        mNowPlayingSession = new MediaSession(context, TAG);
        mNowPlayingSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // for the MediaBrowserService
        //setSessionToken(mNowPlayingSession.getSessionToken());

        mSpotifyService = spotifyService;

        // init playing state with dummy data
        resetPlayingState();
    }

    public void resetPlayingState() {
        mPlayingState = new PlayingState("", "");
    }

    public void play(Object spotifyObject) {
        String currentObjectUri = Utils.getUriFromSpotiyObject(spotifyObject);

        mPlayingState = new PlayingState(currentObjectUri, null);

        if (spotifyObject instanceof TrackSimple || spotifyObject instanceof Playlist ||  spotifyObject instanceof PlaylistSimple) {
            mPlayer.play(currentObjectUri);
        } else if (spotifyObject instanceof AlbumSimple) {
            // get album's tracks
            AlbumSimple albumSimple = (AlbumSimple) spotifyObject;
            SpotifyTvApplication.getInstance().getSpotifyService().getAlbumTracks(albumSimple.id, new Callback<Pager<Track>>() {
                @Override
                public void success(Pager<Track> trackPager, Response response) {
                    List<String> trackUris = new ArrayList<>(trackPager.total);
                    for (Track track : trackPager.items) {
                        trackUris.add(track.uri);
                    }
                    mPlayer.play(trackUris);
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

    public PlayingState getPlayingState() {
        return mPlayingState;
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

        mPlayingState.setCurrentTrackUri(playerState.trackUri);

        switch (eventType) {
            case PLAY:
                BusProvider.post(new OnPlay(mPlayingState));
                break;
            case PAUSE:
                BusProvider.post(new OnPause(mPlayingState));
                break;
            case TRACK_CHANGED:
                BusProvider.post(new OnTrackChanged(mPlayingState));
                trackNowPlayingTrack(playerState.trackUri);
                break;
            case END_OF_CONTEXT:
                if (mNowPlayingSession.isActive()) {
                    mNowPlayingSession.setActive(false);
                }
                resetPlayingState();
                break;
        }
    }

    private void trackNowPlayingTrack(String currentTrackUri) {
        // load track from id (from uri)
        mSpotifyService.getTrack(Utils.getIdFromUri(currentTrackUri), new Callback<Track>() {
            @Override
            public void success(final Track track, Response response) {
                updateNowPlayingMetadata(track);

                trackLastFm(track);
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private void updateNowPlayingMetadata(Track track) {
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

    private void trackLastFm(final Track track) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                LastFmApi.getInstance().scrobbleSpotifyTrack(track);
            }
        }).start();
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
                resetPlayingState();
                BusProvider.post(new OnTrackChanged(mPlayingState));
                break;
            case SHUFFLE:
                mIsShuffleOn = !mIsShuffleOn;
                mPlayer.setShuffle(mIsShuffleOn);
                BusProvider.post(new OnShuffleChanged(mIsShuffleOn));

                // reload current object if not null
                if (!TextUtils.isEmpty(mPlayingState.getCurrentObjectUri())) {
                    play(mPlayingState.getCurrentObjectUri());
                }
                break;
        }
    }

    public void setPlayerBitrate(PlaybackBitrate selectedBitrate) {
        if (mPlayer != null) {
            mPlayer.setPlaybackBitrate(selectedBitrate);
        }
    }
}
