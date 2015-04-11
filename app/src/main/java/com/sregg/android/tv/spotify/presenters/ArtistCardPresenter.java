package com.sregg.android.tv.spotify.presenters;

import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import com.sregg.android.tv.spotify.SpotifyTvApplication;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.net.URI;

/**
 * Created by simonreggiani on 15-01-31.
 */
public class ArtistCardPresenter extends AbsCardPresenter {
    @Override
    public void onBindViewHolder(final Presenter.ViewHolder viewHolder, Object item) {
        super.onBindViewHolder(viewHolder, item);

        final CardViewHolder cardViewHolder = (CardViewHolder) viewHolder;
        final ImageCardView imageCardView = cardViewHolder.getImageCardView();

        ArtistSimple artistSimple = (ArtistSimple) item;

        // name
        imageCardView.setTitleText(artistSimple.name);

        // load real artist (not simple) from API
        SpotifyTvApplication app = SpotifyTvApplication.getInstance();
        app.getSpotifyService().getArtist(artistSimple.id, new Callback<Artist>() {
            @Override
            public void success(final Artist artist, Response response) {
                // TODO nb albums
                //String totalAlbums = mContext.getResources().getQuantityString(R.plurals.playlist_nb_tracks, artist.a, playlist.tracks.total);
                //((ViewHolder) viewHolder).mImageCardView.setContentText(totalAlbums);

                // image
                if (artist.images != null && !artist.images.isEmpty()) {
                    imageCardView.post(new Runnable() {
                        @Override
                        public void run() {
                            cardViewHolder.updateCardViewImage(URI.create(artist.images.get(0).url));
                        }
                    });
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }
}
