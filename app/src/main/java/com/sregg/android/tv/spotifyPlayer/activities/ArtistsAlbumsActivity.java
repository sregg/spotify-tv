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

package com.sregg.android.tv.spotifyPlayer.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import com.sregg.android.tv.spotifyPlayer.R;

/*
 * VerticalGridActivity that loads VerticalGridFragment
 */
public class ArtistsAlbumsActivity extends Activity {

    public static final String ARG_ARTIST_ID = "ARG_ARTIST_ID";
    public static final String ARG_ARTIST_NAME = "ARG_ARTIST_NAME";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artists_albums);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public boolean onSearchRequested() {
        startActivity(new Intent(this, SearchActivity.class));
        return true;
    }

    public static void launch(Activity activity, String id, String name) {
        Intent intent = new Intent(activity, ArtistsAlbumsActivity.class);
        intent.putExtra(ARG_ARTIST_ID, id);
        intent.putExtra(ARG_ARTIST_NAME, name);
        activity.startActivity(intent);
    }
}
