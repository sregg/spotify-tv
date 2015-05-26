package com.sregg.android.tv.spotify.settings;

import com.sregg.android.tv.spotify.R;
import com.sregg.android.tv.spotify.SpotifyTvApplication;

/**
 * Created by simonreggiani on 15-05-25.
 */
public class QualitySetting extends Setting {
    public QualitySetting() {
        super("{fa-music}",
                SpotifyTvApplication.getInstance().getString(R.string.settings_quality),
                ""); // TODO subtitle = quality
    }

    @Override
    public void onClick() {

    }
}
