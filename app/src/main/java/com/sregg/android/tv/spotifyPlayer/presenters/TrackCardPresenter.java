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

import android.support.v17.leanback.widget.Presenter;

import com.sregg.android.tv.spotifyPlayer.utils.Utils;
import com.sregg.android.tv.spotifyPlayer.views.NewSpotifyCardView;

import java.net.URI;

import kaaes.spotify.webapi.android.models.Track;

/*
 * A CardPresenter is used to generate Views and bind Objects to them on demand. 
 * It contains an Image CardView
 */
public class TrackCardPresenter extends AbsCardPresenter {

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        super.onBindViewHolder(viewHolder, item);

        final CardViewHolder cardViewHolder = (CardViewHolder) viewHolder;
        final NewSpotifyCardView imageCardView = cardViewHolder.getImageCardView();

        Track track = (Track) item;

        imageCardView.setTitleText(track.name);

        String artists = Utils.getTrackArtists(track);
        imageCardView.setContentText(artists);
        cardViewHolder.updateCardViewImage(URI.create(track.album.images.get(0).url));
    }
}
