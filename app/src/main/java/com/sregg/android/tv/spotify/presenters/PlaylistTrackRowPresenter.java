package com.sregg.android.tv.spotify.presenters;

import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.text.format.DateUtils;

import com.sregg.android.tv.spotify.rows.TrackRow;
import com.sregg.android.tv.spotify.utils.Utils;

import kaaes.spotify.webapi.android.models.Track;

public class PlaylistTrackRowPresenter extends AbsTrackRowPresenter {

    public PlaylistTrackRowPresenter(OnItemViewClickedListener onTrackRowItemClicked) {
        super(onTrackRowItemClicked);
    }

    @Override
    protected void onBindRowViewHolder(ViewHolder viewHolder, Object item) {
        super.onBindRowViewHolder(viewHolder, item);

        TrackRowViewHolder trackViewHolder = (TrackRowViewHolder) viewHolder;

        TrackRow row = (TrackRow) item;
        Track track = (Track) row.getTrack();

        trackViewHolder.getArtistTextView().setText(Utils.getTrackArtists(track));
        trackViewHolder.getTrackTextView().setText(track.name);
        trackViewHolder.getTrackLengthTextView().setText(DateUtils.formatElapsedTime(track.duration_ms / 1000));
        trackViewHolder.getTrackNumberTextView().setText(null);
    }
}
