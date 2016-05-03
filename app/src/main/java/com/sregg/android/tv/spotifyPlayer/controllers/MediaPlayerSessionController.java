package com.sregg.android.tv.spotifyPlayer.controllers;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;

import com.spotify.sdk.android.player.PlayerState;
import com.squareup.picasso.Picasso;
import com.sregg.android.tv.spotifyPlayer.SpotifyTvApplication;
import com.sregg.android.tv.spotifyPlayer.events.ContentState;
import com.sregg.android.tv.spotifyPlayer.utils.Utils;

import java.io.IOException;

import kaaes.spotify.webapi.android.models.Track;
import timber.log.Timber;

public class MediaPlayerSessionController {

    @NonNull private final MediaSessionCompat mNowPlayingSession;
    @NonNull
    private final SpotifyPlayerController player;

    public MediaPlayerSessionController(@NonNull Context context, @NonNull SpotifyPlayerController player) {
        this.player = player;
        mNowPlayingSession = new MediaSessionCompat(context, "spotifytv");
        mNowPlayingSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        // for the MediaBrowserService
        //setSessionToken(mNowPlayingSession.getSessionToken());
    }

    private void startNowPlayingSession() {
        Timber.d("Starting now playing session");
        mNowPlayingSession.setActive(true);
        mNowPlayingSession.setCallback(new MediaButtonReceiver(player));
    }

    public void stopNowPlayingSession() {
        Timber.d("Stopping now playing session");
        if (mNowPlayingSession.isActive()) {
            mNowPlayingSession.setActive(false);
            mNowPlayingSession.setCallback(null);
        }
    }

    public void updateNowPlayingSession(@NonNull PlayerState playerState, @NonNull ContentState contentState) {
        Timber.d("Updating now playing session");
        if (playerState.playing && !mNowPlayingSession.isActive()) {
            startNowPlayingSession();
        }

        updatePlaybackState(playerState, contentState);
    }

    private void updatePlaybackState(@NonNull PlayerState playerState, @NonNull ContentState contentState) {
        int position = playerState.positionInMs;

        //noinspection WrongConstant
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(getAvailableActions(playerState, contentState));

        //noinspection WrongConstant
        stateBuilder.setState(getState(playerState), position, 1.0f);

        mNowPlayingSession.setPlaybackState(stateBuilder.build());
    }

    public int getState(@NonNull PlayerState playerState) {
        if (playerState.playing) {
            return PlaybackStateCompat.STATE_PLAYING;
        } else if (!TextUtils.isEmpty(playerState.trackUri)) {
            return PlaybackStateCompat.STATE_PAUSED;
        } else {
            return PlaybackStateCompat.STATE_STOPPED;
        }
    }

    private long getAvailableActions(PlayerState playerState, ContentState contentState) {
        long actions = PlaybackStateCompat.ACTION_PLAY_PAUSE |
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
                PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH;


        if (TextUtils.isEmpty(playerState.trackUri)) {
            return actions;
        }

        if (playerState.playing) {
            actions |= PlaybackStateCompat.ACTION_PAUSE;
        } else {
            actions |= PlaybackStateCompat.ACTION_PLAY;
        }

        if (null != contentState.getTrackUrisQueue()) {
            int currentIndex = contentState.getTrackUrisQueue().indexOf(playerState.trackUri);

            if (currentIndex > 0) {
                actions |= PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;
            }
            if (currentIndex < contentState.getTrackUrisQueue().size() - 1) {
                actions |= PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
            }
        }
        return actions;
    }

    public void updateNowPlayingMetadata(Track track) {
        MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();

        // track title
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE,
                track.name);

        // artists
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST,
                Utils.getTrackArtists(track));

        // album picture
        try {
            Bitmap bitmap = Picasso.with(SpotifyTvApplication.getInstance().getApplicationContext())
                    .load(track.album.images.get(0).url)
                    .get();
            metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART,
                    bitmap);
        } catch (IOException e) {
            Timber.e(e, "Error downloading track picture for now playing card");
        }

        mNowPlayingSession.setMetadata(metadataBuilder.build());
    }
}
