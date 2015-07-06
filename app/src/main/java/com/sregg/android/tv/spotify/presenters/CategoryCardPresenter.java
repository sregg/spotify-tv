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

import android.support.v17.leanback.widget.ImageCardView;

import com.sregg.android.tv.spotify.utils.Utils;

import java.net.URI;

import kaaes.spotify.webapi.android.models.Category;
import kaaes.spotify.webapi.android.models.Track;

/*
 * A CardPresenter is used to generate Views and bind Objects to them on demand. 
 * It contains an Image CardView
 */
public class CategoryCardPresenter extends AbsCardPresenter {

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        super.onBindViewHolder(viewHolder, item);

        final CardViewHolder cardViewHolder = (CardViewHolder) viewHolder;
        final ImageCardView imageCardView = cardViewHolder.getImageCardView();

        Category category = (Category) item;

        imageCardView.setTitleText(category.name);

        if (category.icons.size() > 0) {
            cardViewHolder.updateCardViewImage(URI.create(category.icons.get(0).url));
        } else {
            imageCardView.setMainImage(null);
        }
    }
}
