package com.sregg.android.tv.spotify.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.sregg.android.tv.spotify.R;

import kaaes.spotify.webapi.android.models.AlbumSimple;

/*
 * AlbumActivity for AlbumDetailsFragment
 */
public class AlbumActivity extends Activity {

    public static final String ARG_ALBUM_ID = "ARG_ALBUM_ID";
    public static final String ARG_ALBUM_NAME = "ARG_ALBUM_NAME";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
    }

    public static void launch(Activity activity, String id, String name) {
        Intent intent = new Intent(activity, AlbumActivity.class);
        intent.putExtra(ARG_ALBUM_ID, id);
        intent.putExtra(ARG_ALBUM_NAME, name);
        activity.startActivity(intent);
    }

}
