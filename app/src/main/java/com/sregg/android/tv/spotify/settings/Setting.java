package com.sregg.android.tv.spotify.settings;

/**
 * Created by simonreggiani on 15-05-25.
 */
public abstract class Setting {
    private String mFontId;
    private String mTitle;
    private String mSubtitle;

    public Setting(String fontId, String titleRes, String subtitle) {
        mFontId = fontId;
        mTitle = titleRes;
        mSubtitle = subtitle;
    }

    public abstract void onClick();

    public String getFontId() {
        return mFontId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getSubtitle() {
        return mSubtitle;
    }
}
