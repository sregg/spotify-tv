package com.sregg.android.tv.spotifyPlayer.events;

import android.support.annotation.NonNull;

import com.spotify.sdk.android.player.PlayerState;

public class PlayerStateChanged {

    private final PlayerState playerState;
    private final PlayingState playingState;

    public PlayerStateChanged(@NonNull PlayerState playerState, @NonNull PlayingState playingState) {
        this.playerState = playerState;
        this.playingState = playingState;
    }

    @NonNull
    public PlayerState getPlayerState() {
        return playerState;
    }

    @NonNull
    public PlayingState getPlayingState() {
        return playingState;
    }
}
