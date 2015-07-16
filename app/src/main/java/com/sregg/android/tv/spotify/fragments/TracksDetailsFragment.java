package com.sregg.android.tv.spotify.fragments;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.DetailsFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.DetailsOverviewRow;
import android.support.v17.leanback.widget.DetailsOverviewRowPresenter;
import android.support.v17.leanback.widget.OnActionClickedListener;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v7.graphics.Palette;
import android.util.DisplayMetrics;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.sregg.android.tv.spotify.SpotifyTvApplication;
import com.sregg.android.tv.spotify.presenters.AlbumTrackRowPresenter;
import com.sregg.android.tv.spotify.presenters.TracksHeaderRowPresenter;
import com.sregg.android.tv.spotify.rows.TrackRow;
import com.sregg.android.tv.spotify.rows.TracksHeaderRow;
import com.sregg.android.tv.spotify.utils.BlurTransformation;
import com.sregg.android.tv.spotify.utils.Utils;

import java.io.IOException;
import java.util.List;

import kaaes.spotify.webapi.android.models.TrackSimple;

public abstract class TracksDetailsFragment extends DetailsFragment {
    private static final String TAG = TracksDetailsFragment.class.getSimpleName();

    private static final int DETAIL_THUMB_WIDTH = 274;
    private static final int DETAIL_THUMB_HEIGHT = 274;

    private BackgroundManager mBackgroundManager;
    private DisplayMetrics mMetrics;
    private Handler mHandler;
    private ArrayObjectAdapter mRowsAdapter;
    private SpotifyTvApplication mApp;

    private DetailsOverviewRowPresenter mDetailsPresenter;
    private DetailsOverviewRow mDetailsRow;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        mHandler = new Handler();

        mApp = SpotifyTvApplication.getInstance();

        setupFragment();
        setupBackground();
    }

    protected abstract Presenter getDetailsPresenter();

    protected abstract Presenter getTrackRowPresenter();

    private void setupFragment() {
        mDetailsPresenter = new DetailsOverviewRowPresenter(getDetailsPresenter());
        mDetailsPresenter.setStyleLarge(false);

        ClassPresenterSelector ps = new ClassPresenterSelector();
        ps.addClassPresenter(DetailsOverviewRow.class, mDetailsPresenter);
        ps.addClassPresenter(TracksHeaderRow.class, new TracksHeaderRowPresenter());
        ps.addClassPresenter(TrackRow.class, getTrackRowPresenter());

        setOnItemViewClickedListener(new OnItemViewClickedListener() {
            @Override
            public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
                if (item instanceof TrackSimple) {
                    playFromTrack(((TrackSimple) item));
                }
            }
        });

        mRowsAdapter = new ArrayObjectAdapter(ps);
        setAdapter(mRowsAdapter);
    }

    private void playFromTrack(TrackSimple item) {
        List<String> trackUris = getTrackUris();
        List<String> subList = trackUris.subList(trackUris.indexOf(item.uri), trackUris.size());
        mApp.getSpotifyPlayerController().play(getObjectUri(), subList);
    }

    protected abstract List<String> getTrackUris();

    protected abstract String getObjectUri();

    private void setupBackground() {
        mBackgroundManager = BackgroundManager.getInstance(getActivity());
        mBackgroundManager.attach(getActivity().getWindow());
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    protected void setOnActionClickedListener(OnActionClickedListener listener) {
        mDetailsPresenter.setOnActionClickedListener(listener);
    }

    protected void setDetailsRow(DetailsOverviewRow row) {
        mDetailsRow = row;
        mRowsAdapter.add(0, mDetailsRow);
    }

    protected void setupTracksRows(List<TrackSimple> tracks) {
        mRowsAdapter.add(new TracksHeaderRow());
        for (TrackSimple track : tracks) {
            mRowsAdapter.add(new TrackRow(track));
        }
    }

    protected void loadDetailsRowImage(String imageUrl) {
        new ImageLoader().execute(imageUrl);
    }

    private class ImageLoader extends AsyncTask<String, Void, Void> {

        private Bitmap mBackground;

        @Override
        protected Void doInBackground(String... params) {
            if (mDetailsRow == null || mDetailsPresenter == null) {
                return null;
            }

            Bitmap cover = null;
            Bitmap background = null;
            try {
                cover = Picasso.with(getActivity())
                        .load(params[0])
                        .resize(
                                Utils.dpToPx(DETAIL_THUMB_WIDTH, getActivity()),
                                Utils.dpToPx(DETAIL_THUMB_HEIGHT, getActivity())
                        )
                        .centerCrop()
                        .get();

                mDetailsRow.setImageBitmap(getActivity(), cover);

                Palette palette = Palette.generate(cover);
                Palette.Swatch swatch = palette.getDarkVibrantSwatch();

                if (swatch == null) {
                    swatch = palette.getDarkMutedSwatch();
                }

                if (swatch != null) {
                    mDetailsPresenter.setBackgroundColor(swatch.getRgb());
                }

                mBackground = Picasso.with(getActivity())
                        .load(params[0])
                        .transform(new BlurTransformation(getActivity()))
                        .resize(mMetrics.widthPixels, mMetrics.heightPixels)
                        .centerCrop()
                        .get();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mBackgroundManager.setBitmap(mBackground);
            setAdapter(mRowsAdapter);
        }
    }
}
