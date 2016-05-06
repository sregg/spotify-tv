package com.sregg.android.tv.spotifyPlayer.presenters;

import android.support.v17.leanback.widget.Presenter;

import com.sregg.android.tv.spotifyPlayer.SpotifyTvApplication;
import com.sregg.android.tv.spotifyPlayer.views.SpotifyCardView;

import java.net.URI;

import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by simonreggiani on 15-01-31.
 */
public class AlbumCardPresenter extends AbsCardPresenter {
    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        super.onBindViewHolder(viewHolder, item);

        final CardViewHolder cardViewHolder = (CardViewHolder) viewHolder;
        final SpotifyCardView imageCardView = cardViewHolder.getImageCardView();

        AlbumSimple albumSimple = (AlbumSimple) item;

        // name
        imageCardView.setTitleText(albumSimple.name);

        if (albumSimple instanceof Album) {
            // if full album object, display info
            setupFullAlbumInfo(((Album) albumSimple), imageCardView, cardViewHolder);
        } else {
            // if not, reset the info views
            imageCardView.setContentText("");

            // and load the full object from API
            SpotifyTvApplication.getInstance().getSpotifyService().getAlbum(albumSimple.id, new Callback<Album>() {
                @Override
                public void success(final Album album, Response response) {
                    setupFullAlbumInfo(album, imageCardView, cardViewHolder);
                }

                @Override
                public void failure(RetrofitError error) {

                }
            });
        }

    }

    private void setupFullAlbumInfo(Album album, final SpotifyCardView imageCardView, final CardViewHolder cardViewHolder) {
        // artists
        final StringBuilder artists = new StringBuilder();
        for (ArtistSimple artist : album.artists) {
            if (artists.length() > 0) {
                artists.append(", ");
            }
            artists.append(artist.name);
        }

        // image
        final String imageUrl = album.images.get(0).url;

        // run on UI thread
        imageCardView.post(new Runnable() {
            @Override
            public void run() {
                imageCardView.setContentText(artists);

                cardViewHolder.updateCardViewImage(URI.create(imageUrl));
            }
        });
    }
}
