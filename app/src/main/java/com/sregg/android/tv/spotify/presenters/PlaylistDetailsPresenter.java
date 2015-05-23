package com.sregg.android.tv.spotify.presenters;

import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter;

import kaaes.spotify.webapi.android.models.Playlist;


public class PlaylistDetailsPresenter extends AbstractDetailsDescriptionPresenter {

    @Override
    protected void onBindDescription(ViewHolder viewHolder, Object item) {
        Playlist playlist = (Playlist) item;

        if (playlist != null) {
            viewHolder.getTitle().setText(playlist.name);
            viewHolder.getSubtitle().setText(playlist.description);
        }
    }
}
