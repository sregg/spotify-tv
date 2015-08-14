package com.sregg.android.tv.spotify.presenters;

import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sregg.android.tv.spotify.R;
import com.sregg.android.tv.spotify.SpotifyTvApplication;
import com.sregg.android.tv.spotify.rows.TrackRow;
import com.sregg.android.tv.spotify.utils.Utils;
import com.sregg.android.tv.spotify.views.TrackRowView;

import kaaes.spotify.webapi.android.models.TrackSimple;

public abstract class AbsTrackRowPresenter extends RowPresenter {
    private static final String TAG = "TrackRowPresenter";

    private final OnItemViewClickedListener onTrackRowItemClicked;

    static class TrackRowViewHolder extends ViewHolder {

        protected final TrackRowView mTrackRowView;

        public TrackRowViewHolder(TrackRowView trackRowView) {
            super(trackRowView);
            mTrackRowView = trackRowView;
        }

        public TextView getArtistTextView() {
            return mTrackRowView.getArtistTextView();
        }

        public TextView getTrackTextView() {
            return mTrackRowView.getTrackTextView();
        }

        public TextView getTrackLengthTextView() {
            return mTrackRowView.getTrackLengthTextView();
        }

        public TextView getTrackNumberTextView() {
            return mTrackRowView.getTrackNumberTextView();
        }
    }

    public AbsTrackRowPresenter(OnItemViewClickedListener onTrackRowItemClicked) {
        super();
        this.onTrackRowItemClicked = onTrackRowItemClicked;
        setHeaderPresenter(null);
    }

    @Override
    public TrackRowViewHolder createRowViewHolder(ViewGroup parent) {
        TrackRowView view = (TrackRowView) LayoutInflater.from(parent.getContext()).inflate(R.layout.track_row_view, parent, false);
        return new TrackRowViewHolder(view);
    }

    @Override
    protected void onBindRowViewHolder(RowPresenter.ViewHolder viewHolder, final Object item) {
        super.onBindRowViewHolder(viewHolder, item);

        final TrackRowViewHolder trackViewHolder = (TrackRowViewHolder) viewHolder;
        final TrackRowView trackRowview = trackViewHolder.mTrackRowView;
        final TrackRow trackRow = (TrackRow) item;

        // get uri
        String uri = Utils.getUriFromSpotiyObject(trackRow.getTrack());
        trackRowview.setUri(uri);

        // init badge and now playing
        trackRowview.initNowPlaying(SpotifyTvApplication.getInstance()
                .getSpotifyPlayerController()
                .getPlayingState()
                .isCurrentTrack(uri));

        trackRowview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTrackRowItemClicked.onItemClicked(trackViewHolder, trackRow.getTrack(), getRowViewHolder(trackViewHolder), trackRow);
            }
        });
    }

    @Override
    protected void onUnbindRowViewHolder(RowPresenter.ViewHolder viewHolder) {
        super.onUnbindRowViewHolder(viewHolder);

        TrackRowViewHolder cardViewHolder = (TrackRowViewHolder) viewHolder;

        TrackRowView trackRowview = cardViewHolder.mTrackRowView;
    }

    @Override
    protected void onRowViewSelected(RowPresenter.ViewHolder holder, boolean selected) {
        super.onRowViewSelected(holder, selected);
        ViewHolder vh = (ViewHolder) holder;
    }

    @Override
    public final boolean isUsingDefaultSelectEffect() {
        return false;
    }
}
