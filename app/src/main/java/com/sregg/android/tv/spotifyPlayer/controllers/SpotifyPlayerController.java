package com.sregg.android.tv.spotifyPlayer.controllers;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.PlayConfig;
import com.spotify.sdk.android.player.PlaybackBitrate;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.PlayerStateCallback;
import com.spotify.sdk.android.player.Spotify;
import com.sregg.android.tv.spotifyPlayer.BusProvider;
import com.sregg.android.tv.spotifyPlayer.Constants;
import com.sregg.android.tv.spotifyPlayer.SpotifyTvApplication;
import com.sregg.android.tv.spotifyPlayer.enums.Control;
import com.sregg.android.tv.spotifyPlayer.events.ContentState;
import com.sregg.android.tv.spotifyPlayer.events.OnPause;
import com.sregg.android.tv.spotifyPlayer.events.OnPlay;
import com.sregg.android.tv.spotifyPlayer.events.OnShuffleChanged;
import com.sregg.android.tv.spotifyPlayer.events.OnTrackChanged;
import com.sregg.android.tv.spotifyPlayer.events.PlayerStateChanged;
import com.sregg.android.tv.spotifyPlayer.settings.UserPreferences;
import com.sregg.android.tv.spotifyPlayer.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TrackSimple;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;


/**
 * <p>Plays tracks, playlists and albums into the Spotify SDK</p>
 * <p>Fires OTTO playback events like {@link com.sregg.android.tv.spotifyPlayer.events.OnPlay}, {@link com.sregg.android.tv.spotifyPlayer.events.OnPause}, etc..</p>
 * <p>Also updates the Now Playing Card in the Home Screen</p>
 */
public class SpotifyPlayerController implements PlayerNotificationCallback, ConnectionStateCallback, AudioManager.OnAudioFocusChangeListener {

    public static final String TAG = "SpotifyPlayerController";

    private static final int SKIP_DURATION_MS = 10 * 1000;

    private final Player mPlayer;
    private final SpotifyService mSpotifyService;
    private final Handler mHandler;

    private final MediaPlayerSessionController mediaSessionController;
    private final UserPreferences mUserPreferences;

    private ContentState mContentState;

    private final AudioManager audioManager;
    private final Context context;

    public SpotifyPlayerController(Player player, SpotifyService spotifyService) {
        context = SpotifyTvApplication.getInstance().getApplicationContext();
        mHandler = new Handler(context.getMainLooper());
        mUserPreferences = UserPreferences.getInstance(context);

        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mPlayer = player;

        mPlayer.addPlayerNotificationCallback(this);
        mPlayer.addConnectionStateCallback(this);
        setPlayerBitrate(mUserPreferences.getBitrate());
        setShuffle(mUserPreferences.getShuffle());

        mediaSessionController = new MediaPlayerSessionController(context, this);

        mSpotifyService = spotifyService;

        // init playing state with dummy data
        resetPlayingState();
    }

    public void resetPlayingState() {
        mContentState = new ContentState("", "", null, null);
    }

    public void play(@Nullable String currentObjectUri, @Nullable List<String> trackUris, @Nullable List<TrackSimple> tracks) {
        play(currentObjectUri, trackUris.get(0), trackUris, tracks);
    }

    public void play(@Nullable String currentObjectUri, @Nullable String currentTrackUri, @Nullable List<String> trackUris, @Nullable List<TrackSimple> tracks) {
        if (trackUris == null) {
            trackUris = new ArrayList<>();
        }

        if (!SpotifyTvApplication.isCurrentUserPremium()) {
            Toast.makeText(SpotifyTvApplication.getInstance().getApplicationContext(), "You need a premium Spotify account to play music on this app", Toast.LENGTH_SHORT).show();
        } else {
            mContentState = new ContentState(currentObjectUri, currentTrackUri, trackUris, tracks);

            PlayConfig playConfig = PlayConfig.createFor(trackUris);
            int trackIndex = Math.max(0, trackUris.indexOf(currentTrackUri));

            playConfig.withTrackIndex(trackIndex);

            if (requestAudioFocus()) {
                mPlayer.play(playConfig);
                setShuffle(isShuffleOn());
            }
        }
    }

    private boolean requestAudioFocus() {
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private void loseAudioFocus() {
        audioManager.abandonAudioFocus(this);
    }


    public void togglePauseResume() {
        mPlayer.getPlayerState(new PlayerStateCallback() {
            @Override
            public void onPlayerState(PlayerState playerState) {
                if (playerState.playing) {
                    mPlayer.pause();
                    loseAudioFocus();
                } else if (requestAudioFocus()) {
                    mPlayer.resume();
                }
            }
        });
    }

    public
    @Nullable
    ContentState getPlayingState() {
        return mContentState;
    }

    public void getPlayerState(PlayerStateCallback callback) {
        mPlayer.getPlayerState(callback);
    }

    public void triggerPlayerStateUpdate() {
        getPlayerState(new PlayerStateCallback() {
            @Override
            public void onPlayerState(PlayerState playerState) {
                postPlayerStateChanged(playerState);
            }
        });
    }

    public boolean isShuffleOn() {
        return mUserPreferences.getShuffle();
    }

    public void terminate() {
        mediaSessionController.stopNowPlayingSession();
        Spotify.destroyPlayer(mPlayer);
    }

    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
        Log.d(TAG, String.format("%s - isPlaying: %s - trackUri: %s - positionInMs:%s",
                eventType.name(), playerState.playing, playerState.trackUri, playerState.positionInMs));

        mContentState.setCurrentTrackUri(playerState.trackUri);

        switch (eventType) {
            case PLAY:
                BusProvider.post(new OnPlay(mContentState));
                Answers.getInstance().logCustom(new CustomEvent(Constants.ANSWERS_EVENT_PLAYER_PLAY));
                mediaSessionController.updateNowPlayingSession(playerState, mContentState);
                postPlayerStateChanged(playerState);
                break;
            case PAUSE:
                BusProvider.post(new OnPause(mContentState));
                Answers.getInstance().logCustom(new CustomEvent(Constants.ANSWERS_EVENT_PLAYER_PAUSE));
                loseAudioFocus();
                mediaSessionController.updateNowPlayingSession(playerState, mContentState);
                postPlayerStateChanged(playerState);
                break;
            case TRACK_CHANGED:
                trackNowPlayingTrack(playerState.trackUri);
                Answers.getInstance().logCustom(new CustomEvent(Constants.ANSWERS_EVENT_PLAYER_TRACK_CHANGE));
                postPlayerStateChanged(playerState);
                break;
            case END_OF_CONTEXT:
                mediaSessionController.stopNowPlayingSession();
                resetPlayingState();
                loseAudioFocus();
                postPlayerStateChanged(playerState);
                break;
        }

    }

    private void postPlayerStateChanged(PlayerState playerState) {
        BusProvider.post(new PlayerStateChanged(playerState, mContentState));
    }

    private void trackNowPlayingTrack(String currentTrackUri) {
        // load track from id (from uri)
        mSpotifyService.getTrack(Utils.getIdFromUri(currentTrackUri), new Callback<Track>() {
            @Override
            public void success(final Track track, Response response) {
                mContentState.setCurrentTrack(track);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mediaSessionController.updateNowPlayingMetadata(track);
                        trackLastFm(track);

                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                BusProvider.post(new OnTrackChanged(mContentState));
                            }
                        });
                    }
                }).start();
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }


    private void trackLastFm(final Track track) {
        try {
            LastFmApi.getInstance().scrobbleSpotifyTrack(track);
        } catch (Exception e) {
            // know issue in de.umass.lastfm.Track.Track.parseIntoScrobbleResult()
            Log.w(TAG, "Error while scrobbling to last.fm", e);
        }
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
                if (requestAudioFocus()) {
                    mPlayer.resume();
                }
                break;
            case PAUSE:
                mPlayer.pause();
                loseAudioFocus();
                break;
            case NEXT:
                mPlayer.skipToNext();
                break;
            case PREVIOUS:
                mPlayer.skipToPrevious();
                break;
            case STOP:
                stopPlayer();
                break;
            case SHUFFLE:
                boolean shuffle = !isShuffleOn();
                mUserPreferences.setShuffle(shuffle);
                setShuffle(shuffle);
                BusProvider.post(new OnShuffleChanged(shuffle));

                // reload current object if not null
                if (!TextUtils.isEmpty(mContentState.getCurrentObjectUri())) {
                    play(mContentState.getCurrentObjectUri(), mContentState.getTrackUrisQueue(), mContentState.getTracksQueue());
                }
                break;
            case FAST_FORWARD:
                mPlayer.getPlayerState(new PlayerStateCallback() {
                    @Override
                    public void onPlayerState(PlayerState playerState) {
                        final int fastForwardPosition = playerState.positionInMs + SKIP_DURATION_MS;
                        if (fastForwardPosition < playerState.durationInMs) {
                            mPlayer.seekToPosition(fastForwardPosition);
                        } else {
                            mPlayer.skipToNext();
                        }
                    }
                });
                break;
            case REWIND:
                mPlayer.getPlayerState(new PlayerStateCallback() {
                    @Override
                    public void onPlayerState(PlayerState playerState) {
                        final int currentPosition = playerState.positionInMs;
                        if (currentPosition < SKIP_DURATION_MS) {
                            mPlayer.seekToPosition(0);
                        } else {
                            final int rewindPosition = currentPosition - SKIP_DURATION_MS;
                            mPlayer.seekToPosition(rewindPosition);
                        }
                    }
                });
                break;
            default:
                break;
        }
    }

    private void stopPlayer() {
        mediaSessionController.stopNowPlayingSession();
        if (mPlayer != null) {
            mPlayer.pause();
            mPlayer.clearQueue();
            resetPlayingState();
        }
        loseAudioFocus();
        BusProvider.post(new OnTrackChanged(mContentState));
    }

    public void setPlayerBitrate(PlaybackBitrate selectedBitrate) {
        if (mPlayer != null) {
            mPlayer.setPlaybackBitrate(selectedBitrate);
        }
    }

    public void setShuffle(boolean shuffle) {
        if (mPlayer != null) {
            mPlayer.setShuffle(shuffle);
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                Timber.d("audio focus gained");
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                Timber.d("audio focus lossed");
                stopPlayer();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                Timber.d("audio focus lossed transient");
                if (mPlayer != null) {
                    mPlayer.pause();
                }
                break;
        }
    }
}
