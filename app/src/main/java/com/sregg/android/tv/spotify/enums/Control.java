package com.sregg.android.tv.spotify.enums;

/**
 * Created by simonreggiani on 15-02-08.
 */
public enum Control {
    PLAY("{fa-play}"),
    PAUSE("{fa-pause}"),
    NEXT("{fa-step-forward}"),
    PREVIOUS("{fa-step-backward}"),
    STOP("{fa-stop}"),
    SHUFFLE("{fa-random}");

    private final String mFontId;

    Control(String fontId) {
        mFontId = fontId;
    }

    public String getFontId() {
        return mFontId;
    }
}
