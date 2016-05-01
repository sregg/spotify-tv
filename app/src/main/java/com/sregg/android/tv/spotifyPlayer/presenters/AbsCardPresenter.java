/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.sregg.android.tv.spotifyPlayer.presenters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.Presenter;
import android.support.v7.graphics.Palette;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.sregg.android.tv.spotifyPlayer.R;
import com.sregg.android.tv.spotifyPlayer.SpotifyTvApplication;
import com.sregg.android.tv.spotifyPlayer.events.ContentState;
import com.sregg.android.tv.spotifyPlayer.utils.Utils;
import com.sregg.android.tv.spotifyPlayer.views.NewSpotifyCardView;

import java.net.URI;

import kaaes.spotify.webapi.android.models.TrackSimple;

/*
 * A CardPresenter is used to generate Views and bind Objects to them on demand. 
 * It contains an Image CardView
 */
public abstract class AbsCardPresenter extends Presenter {
    private static final String TAG = "CardPresenter";

    static class CardViewHolder extends Presenter.ViewHolder {

        protected final NewSpotifyCardView mNewSpotifyCardView;
        protected PicassoImageCardViewTarget mImageCardViewTarget;

        public CardViewHolder(NewSpotifyCardView newSpotifyCardView) {
            super(newSpotifyCardView);
            mNewSpotifyCardView = newSpotifyCardView;
            mImageCardViewTarget = new PicassoImageCardViewTarget(newSpotifyCardView);
        }

        public NewSpotifyCardView getImageCardView() {
            return mNewSpotifyCardView;
        }

        protected void updateCardViewImage(URI uri) {
            int imageSize = mNewSpotifyCardView.getImageSize();
            Picasso.with(mNewSpotifyCardView.getContext())
                    .load(uri.toString())
                    .resize(imageSize, imageSize)
                    .centerCrop()
                    .into(mImageCardViewTarget);
        }
    }

    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup parent) {
        Context context = parent.getContext();

        NewSpotifyCardView cardView = new NewSpotifyCardView(context);
        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        cardView.setBackgroundColor(context.getResources().getColor(R.color.card_default_bg));
        return new CardViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        CardViewHolder cardViewHolder = (CardViewHolder) viewHolder;

        NewSpotifyCardView newSpotifyCardView = cardViewHolder.mNewSpotifyCardView;


        // set item
        newSpotifyCardView.setItem(item);

        // init badge and now playing
        ContentState contentState = SpotifyTvApplication.getInstance()
                .getSpotifyPlayerController()
                .getPlayingState();
        String uri = Utils.getUriFromSpotiyObject(item);
        newSpotifyCardView.initNowPlaying(item instanceof TrackSimple ? contentState.isCurrentTrack(uri) : contentState.isCurrentObject(uri));
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
        CardViewHolder cardViewHolder = (CardViewHolder) viewHolder;

        NewSpotifyCardView newSpotifyCardView = cardViewHolder.mNewSpotifyCardView;

        // reset info selected color
//        newSpotifyCardView.setSelectedInfoAreaBackgroundColor(null);

        // reset image
//        newSpotifyCardView.setMainImage(null);
        newSpotifyCardView.setBackground(null);
//        newSpotifyCardView.setInfoAreaBackground(null);

        // reset badge and now playing
//        newSpotifyCardView.getBadgeView().setVisibility(View.GONE);
//        newSpotifyCardView.getNowPlayingView().setVisibility(View.GONE);
    }

    @Override
    public void onViewAttachedToWindow(Presenter.ViewHolder viewHolder) {
        // TO DO
    }

    public static class PicassoImageCardViewTarget implements Target {
        private NewSpotifyCardView mNewSpotifyCardView;

        public PicassoImageCardViewTarget(NewSpotifyCardView newSpotifyCardView) {
            mNewSpotifyCardView = newSpotifyCardView;
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
            // load the bitmap in the imageview
            Drawable bitmapDrawable = new BitmapDrawable(mNewSpotifyCardView.getContext().getResources(), bitmap);
            mNewSpotifyCardView.setMainImage(bitmapDrawable);

            // set background based on the color palette
            Palette.generateAsync(bitmap, new Palette.PaletteAsyncListener() {
                public void onGenerated(Palette palette) {
                    Palette.Swatch swatch = palette.getDarkVibrantSwatch();

                    if (swatch == null) {
                        swatch = palette.getDarkMutedSwatch();
                    }

                    if (swatch != null) {
//                        mNewSpotifyCardView.setSelectedInfoAreaBackgroundColor(swatch.getRgb());
                    }
                }
            });
        }

        @Override
        public void onBitmapFailed(Drawable drawable) {
            mNewSpotifyCardView.setMainImage(drawable);
        }

        @Override
        public void onPrepareLoad(Drawable drawable) {
            // Do nothing, default_background manager has its own transitions
        }
    }
}
