package com.sregg.android.tv.spotifyPlayer.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.sregg.android.tv.spotifyPlayer.R;

/*
 * PlaylistActivity for PlaylistDetailsFragment
 */
public class PlaylistActivity extends Activity {

    public static final String ARG_PLAYLIST_ID = "ARG_PLAYLIST_ID";
    public static final String ARG_PLAYLIST_NAME = "ARG_PLAYLIST_NAME";
    public static final String ARG_USER_ID = "ARG_USER_ID";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
    }

    public static void launch(Activity activity, String id, String name, String userId) {
        Intent intent = new Intent(activity, PlaylistActivity.class);
        intent.putExtra(ARG_PLAYLIST_ID, id);
        intent.putExtra(ARG_PLAYLIST_NAME, name);
        intent.putExtra(ARG_USER_ID, userId);
        activity.startActivity(intent);
    }

}
