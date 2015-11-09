package com.sregg.android.tv.spotifyPlayer.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.sregg.android.tv.spotifyPlayer.R;
import com.sregg.android.tv.spotifyPlayer.SpotifyTvApplication;
import com.sregg.android.tv.spotifyPlayer.controllers.LastFmApi;

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
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null);

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        Button okBtn = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Context context = SpotifyTvApplication.getInstance().getApplicationContext();

                final String username = usernameET.getText().toString();
                final String password = passwordET.getText().toString();

                if (username.isEmpty()) {
                    usernameET.setError(context.getString(R.string.settings_lastfm_error_username_empty));
                    return;
                }

                if (password.isEmpty()) {
                    passwordET.setError(context.getString(R.string.settings_lastfm_error_password_empty));
                    return;
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean success = LastFmApi.getInstance().startLastFmSession(username, password);

                        if (!success) {
                            passwordET.post(new Runnable() {
                                @Override
                                public void run() {
                                    passwordET.setError(context.getString(R.string.settings_lastfm_error_invalid));
                                }
                            });
                            return;
                        }

                        // if login successful, save username/password in prefs
                        prefs.setLastFmUsername(username);
                        prefs.setLastFmPassword(password);

                        // and close dialog
                        alertDialog.dismiss();
                    }
                }).start();


            }
        });
    }
}
