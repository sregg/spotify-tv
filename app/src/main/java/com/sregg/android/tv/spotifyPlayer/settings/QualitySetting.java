package com.sregg.android.tv.spotifyPlayer.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.ArrayAdapter;
import com.spotify.sdk.android.player.PlaybackBitrate;
import com.sregg.android.tv.spotifyPlayer.R;
import com.sregg.android.tv.spotifyPlayer.SpotifyTvApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by simonreggiani on 15-05-25.
 */
public class QualitySetting extends Setting {
    public QualitySetting() {
        super("{fa-cog}", SpotifyTvApplication.getInstance().getString(R.string.settings_quality));
    }

    @Override
    public void onClick(final Activity activity) {
        // TODO use DialogFragment (don't know why it wasn't working...)

        final List<BitrateItem> itemsList = new ArrayList<>(3);
        itemsList.add(new BitrateItem(activity.getString(R.string.settings_quality_low), PlaybackBitrate.BITRATE_LOW));
        itemsList.add(new BitrateItem(activity.getString(R.string.settings_quality_normal), PlaybackBitrate.BITRATE_NORMAL));
        itemsList.add(new BitrateItem(activity.getString(R.string.settings_quality_high), PlaybackBitrate.BITRATE_HIGH));

        ArrayAdapter<BitrateItem> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_single_choice, itemsList);

        PlaybackBitrate bitrate = UserPreferences.getInstance(activity).getBitrate();

        int selectedBitrateIndex = itemsList.indexOf(new BitrateItem(null, bitrate));

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setTitle(R.string.settings_quality_dialog)
                .setSingleChoiceItems(adapter, selectedBitrateIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PlaybackBitrate selectedBitrate = itemsList.get(which).getBitrate();

                        // save in user pref
                        UserPreferences.getInstance(activity).setBitrate(selectedBitrate);

                        // set it in Spotify SDK player
                        SpotifyTvApplication.getInstance().getSpotifyPlayerController().setPlayerBitrate(selectedBitrate);

                        // close the dialog
                        dialog.dismiss();
                    }
                });

        builder.create().show();
    }

    private class BitrateItem {
        private String mLabel;
        private PlaybackBitrate mBitrate;

        public BitrateItem(String label, PlaybackBitrate bitrate) {
            mLabel = label;
            mBitrate = bitrate;
        }

        public PlaybackBitrate getBitrate() {
            return mBitrate;
        }

        @Override
        public String toString() {
            return mLabel;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            BitrateItem that = (BitrateItem) o;

            return mBitrate == that.mBitrate;

        }

        @Override
        public int hashCode() {
            return mBitrate != null ? mBitrate.hashCode() : 0;
        }
    }
}
