package com.sregg.android.tv.spotifyPlayer.events;

import android.support.annotation.NonNull;

import com.spotify.sdk.android.player.PlayerState;

public class PlayerStateChanged {

    private final PlayerState playerState;
    private final ContentState contentState;

    public PlayerStateChanged(@NonNull PlayerState playerState, @NonNull ContentState contentState) {
        this.playerState = playerState;
        this.contentState = contentState;
    }

    @NonNull
    public PlayerState getPlayerState() {
        return playerState;
    }

    @NonNull
    public ContentState getContentState() {
        return contentState;
    }
}
