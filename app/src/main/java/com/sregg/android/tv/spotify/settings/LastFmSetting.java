package com.sregg.android.tv.spotify.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.spotify.sdk.android.player.PlaybackBitrate;
import com.sregg.android.tv.spotify.R;
import com.sregg.android.tv.spotify.SpotifyTvApplication;
import com.sregg.android.tv.spotify.controllers.LastFmApi;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by simonreggiani on 15-05-25.
 */
public class LastFmSetting extends Setting {
    public LastFmSetting() {
        super("{fa-lastfm}", SpotifyTvApplication.getInstance().getString(R.string.settings_lastfm));
    }

    @Override
    public void onClick(final Activity activity) {


        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        View inputView = LayoutInflater.from(activity).inflate(R.layout.setting_dialog_lastfm, null, false);

        final EditText usernameET = (EditText) inputView.findViewById(R.id.lastfm_username);
        final EditText passwordET = (EditText) inputView.findViewById(R.id.lastfm_password);

        final UserPreferences prefs = UserPreferences.getInstance(SpotifyTvApplication.getInstance().getApplicationContext());

        usernameET.setText(prefs.getLastFmUsername());
        passwordET.setText(prefs.getLastFmPassword());

        builder.setTitle(R.string.settings_lastfm_dialog)
                .setView(inputView)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        prefs.setLastFmUsername(usernameET.getText().toString());
                        prefs.setLastFmPassword(passwordET.getText().toString());
                    }
                })
        .setNegativeButton(android.R.string.cancel, null);

        builder.create().show();
    }
}
