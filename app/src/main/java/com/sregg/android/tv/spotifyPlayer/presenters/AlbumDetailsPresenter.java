package com.sregg.android.tv.spotifyPlayer.presenters;

import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter;

import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.ArtistSimple;


public class AlbumDetailsPresenter extends AbstractDetailsDescriptionPresenter {

    @Override
    protected void onBindDescription(ViewHolder viewHolder, Object item) {
        Album album = (Album) item;

        if (album != null) {
            // artists
            StringBuilder artists = new StringBuilder();
            for (ArtistSimple artist : album.artists) {
                if (artists.length() > 0) {
                    artists.append(", ");
                }
                artists.append(artist.name);
            }

            viewHolder.getTitle().setText(album.name);
            viewHolder.getSubtitle().setText(artists);
            viewHolder.getBody().setText(album.release_date);
        }
    }
}
