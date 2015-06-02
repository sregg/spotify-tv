package com.sregg.android.tv.spotify.settings;

import android.content.Context;
import android.content.SharedPreferences;
import com.spotify.sdk.android.player.PlaybackBitrate;

/**
 * Created by simonreggiani on 15-05-26.
 */
public class UserPreferences {
    private static final String SHARED_PREFS_NAME = "UserPrefs";
    private static final String PREF_KEY_BITRATE = "bitrate";
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
}
