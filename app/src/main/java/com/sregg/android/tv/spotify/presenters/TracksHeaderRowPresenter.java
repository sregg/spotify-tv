package com.sregg.android.tv.spotify.presenters;

import android.support.v17.leanback.widget.RowPresenter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sregg.android.tv.spotify.R;
import com.sregg.android.tv.spotify.views.TrackRowView;

public class TracksHeaderRowPresenter extends RowPresenter {
    private static final String TAG = "TracksHeaderRowPresenter";

    static class TracksHeaderRowViewHolder extends ViewHolder {

        public TracksHeaderRowViewHolder(View view) {
            super(view);
        }
    }

    public TracksHeaderRowPresenter() {
        super();

        setHeaderPresenter(null);
    }

    @Override
    public TracksHeaderRowViewHolder createRowViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tracks_header_row_view, parent, false);
        return new TracksHeaderRowViewHolder(view);
    }

    @Override
    public final boolean isUsingDefaultSelectEffect() {
        return false;
    }
}
