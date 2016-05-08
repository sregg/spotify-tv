package com.sregg.android.tv.spotifyPlayer.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.spotify.sdk.android.player.PlaybackBitrate;

/**
 * Created by simonreggiani on 15-05-26.
 */
public class UserPreferences {
    private static final String SHARED_PREFS_NAME = "UserPrefs";
    private static final String PREF_KEY_BITRATE = "bitrate";
    private static final String PREF_KEY_SHUFFLE = "shuffle";
    private static final String PREF_KEY_LASTFM_USERNAME = "lastfm_username";
    private static final String PREF_KEY_LASTFM_PASSWORD = "lastfm_password";
    private static final String PREF_KEY_SECTION_ENABLED = "section_enabled";
    private static UserPreferences INSTANCE;
    private final SharedPreferences mSharedPreferences;

    private UserPreferences(Context context) {
        mSharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static UserPreferences getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new UserPreferences(context);
        }

        return INSTANCE;
    }

    public void setBitrate(PlaybackBitrate bitrate) {
        mSharedPreferences.edit().putString(PREF_KEY_BITRATE, bitrate.name()).apply();
    }

    public PlaybackBitrate getBitrate() {
        String valueString = mSharedPreferences.getString(PREF_KEY_BITRATE, PlaybackBitrate.BITRATE_NORMAL.name());
        return PlaybackBitrate.valueOf(valueString);
    }

    public void setShuffle(boolean shuffle) {
        mSharedPreferences.edit().putBoolean(PREF_KEY_SHUFFLE, shuffle).apply();
    }

    public boolean getShuffle() {
        return mSharedPreferences.getBoolean(PREF_KEY_SHUFFLE, false);
    }

    public void setLastFmUsername(String username) {
        mSharedPreferences.edit().putString(PREF_KEY_LASTFM_USERNAME, username).apply();
    }

    @Nullable
    public String getLastFmUsername() {
        return mSharedPreferences.getString(PREF_KEY_LASTFM_USERNAME, null);
    }

    public void setLastFmPassword(String password) {
        mSharedPreferences.edit().putString(PREF_KEY_LASTFM_PASSWORD, password).apply();
    }

    @Nullable
    public String getLastFmPassword() {
        return mSharedPreferences.getString(PREF_KEY_LASTFM_PASSWORD, null);
    }

    public boolean isSectionEnabled(String section) {
        return mSharedPreferences.getBoolean(PREF_KEY_SECTION_ENABLED + section, true);
    }

    public void setSectionEnabled(String section, boolean enabled) {
        mSharedPreferences.edit().putBoolean(PREF_KEY_SECTION_ENABLED + section, enabled).apply();
    }
}
