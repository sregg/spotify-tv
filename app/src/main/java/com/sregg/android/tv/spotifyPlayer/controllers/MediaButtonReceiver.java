package com.sregg.android.tv.spotifyPlayer.controllers;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.KeyEvent;

import com.sregg.android.tv.spotifyPlayer.enums.Control;

import timber.log.Timber;

/**
 * Created by gw111zz on 24/01/2016.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MediaButtonReceiver extends MediaSessionCompat.Callback {

    private static final String TAG = "MediaButtonReceiver";

    private final SpotifyPlayerController mPlayer;

    public MediaButtonReceiver(SpotifyPlayerController player) {
        mPlayer = player;
    }

    @Override
    public boolean onMediaButtonEvent(Intent mediaButtonIntent) {
        final KeyEvent event = (KeyEvent) mediaButtonIntent.getExtras().get(Intent.EXTRA_KEY_EVENT);
        Timber.d("onMediaButtonEvent: %s", (event == null ? "null" : event));

        if (event == null) {
            return super.onMediaButtonEvent(mediaButtonIntent);
        }

        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            final int keyCode = event.getKeyCode();
            switch (keyCode) {
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    mPlayer.onControlClick(Control.PLAY);
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    mPlayer.onControlClick(Control.PREVIOUS);
                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    mPlayer.onControlClick(Control.NEXT);
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    mPlayer.onControlClick(Control.PAUSE);
                    break;
                case KeyEvent.KEYCODE_MEDIA_STOP:
                    mPlayer.onControlClick(Control.STOP);
                    break;
                case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                    mPlayer.onControlClick(Control.FAST_FORWARD);
                    break;
                case KeyEvent.KEYCODE_MEDIA_REWIND:
                    mPlayer.onControlClick(Control.REWIND);
                    break;
                default:
                    return super.onMediaButtonEvent(mediaButtonIntent);
            }
        }

        return true;
    }
}
