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

package com.sregg.android.tv.spotify.presenters;

import android.graphics.Color;
import android.support.v17.leanback.widget.ImageCardView;

import com.joanzapata.android.iconify.IconDrawable;
import com.joanzapata.android.iconify.Iconify;
import com.sregg.android.tv.spotify.Constants;
import com.sregg.android.tv.spotify.R;

import java.net.URI;

import kaaes.spotify.webapi.android.models.PlaylistSimple;

/*
 * A CardPresenter is used to generate Views and bind Objects to them on demand. 
 * It contains an Image CardView
 */
public class PlaylistSimpleCardPresenter extends AbsCardPresenter {

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, Object item) {
        super.onBindViewHolder(viewHolder, item);

        final CardViewHolder cardViewHolder = (CardViewHolder) viewHolder;
        ImageCardView imageCardView = cardViewHolder.getImageCardView();

        final PlaylistSimple playlist = (PlaylistSimple) item;

        // name
        imageCardView.setTitleText(playlist.name);

        // nb tracks
        int totalTracks = playlist.tracks.total;
        String totalTracksString;
        if (totalTracks > 0) {
            totalTracksString = imageCardView.getContext().getResources().getQuantityString(R.plurals.playlist_nb_tracks, totalTracks, totalTracks);
        } else {
            totalTracksString = null;
        }
        imageCardView.setContentText(totalTracksString);

        // playlist mosaic
        if (playlist.images != null && !playlist.images.isEmpty()) {
            cardViewHolder.updateCardViewImage(URI.create(playlist.images.get(0).url));
        }
    }
}
