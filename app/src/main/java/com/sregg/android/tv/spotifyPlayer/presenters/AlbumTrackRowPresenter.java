package com.sregg.android.tv.spotifyPlayer.presenters;

import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.RowPresenter;
import android.text.format.DateUtils;
import com.sregg.android.tv.spotifyPlayer.rows.TrackRow;
import com.sregg.android.tv.spotifyPlayer.utils.Utils;
import kaaes.spotify.webapi.android.models.TrackSimple;

public class AlbumTrackRowPresenter extends AbsTrackRowPresenter {

    public AlbumTrackRowPresenter(OnItemViewClickedListener onTrackRowItemClicked) {
        super(onTrackRowItemClicked);
    }

    @Override
    protected void onBindRowViewHolder(RowPresenter.ViewHolder viewHolder, Object item) {
        super.onBindRowViewHolder(viewHolder, item);

        TrackRowViewHolder trackViewHolder = (TrackRowViewHolder) viewHolder;

        TrackRow row = (TrackRow) item;
        TrackSimple track = row.getTrack();

        trackViewHolder.getArtistTextView().setText(Utils.getTrackArtists(track));
        trackViewHolder.getTrackTextView().setText(track.name);
        trackViewHolder.getTrackLengthTextView().setText(DateUtils.formatElapsedTime(track.duration_ms / 1000));
        trackViewHolder.getTrackNumberTextView().setText(String.valueOf(track.track_number));
    }
}
