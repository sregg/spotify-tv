package com.sregg.android.tv.spotifyPlayer.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.sregg.android.tv.spotifyPlayer.BusProvider;
import com.sregg.android.tv.spotifyPlayer.R;
import com.sregg.android.tv.spotifyPlayer.SpotifyTvApplication;

/**
 * Created by simonreggiani on 15-05-25.
 */
public class CustomizeUiSetting extends Setting {
    public CustomizeUiSetting() {
        super("{fa-bars}", SpotifyTvApplication.getInstance().getString(R.string.settings_customize));
    }

    @Override
    public void onClick(final Activity activity) {
        // TODO use DialogFragment (don't know why it wasn't working...)

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        final String[] sections = activity.getResources().getStringArray(R.array.settings_customize_dialog_choices);

        boolean[] isSectionSelected = new boolean[sections.length];

        UserPreferences userPreferences = UserPreferences.getInstance(activity);

        for (int i = 0; i < sections.length; i++) {
            isSectionSelected[i] = userPreferences.isSectionEnabled(sections[i]);
        }

        builder.setTitle(R.string.settings_customize_dialog)
                .setMultiChoiceItems(sections, isSectionSelected, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        // save pref
                        UserPreferences.getInstance(activity).setSectionEnabled(sections[i], b);
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // send OTTO event to tell the main fragment to refresh
                        BusProvider.post(new OnCustomizeUiSettingChanged());
                    }
                });

        builder.create().show();
    }

    public static class OnCustomizeUiSettingChanged {

    }
}
