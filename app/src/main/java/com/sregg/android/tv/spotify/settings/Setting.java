package com.sregg.android.tv.spotify.settings;

import android.app.Activity;

/**
 * Created by simonreggiani on 15-05-25.
 */
public abstract class Setting {
    private String mFontId;
    private String mTitle;

    public Setting(String fontId, String titleRes) {
        mFontId = fontId;
        mTitle = titleRes;
    }

    public abstract void onClick(Activity activity);

    public String getFontId() {
        return mFontId;
    }

    public String getTitle() {
        return mTitle;
    }
}
