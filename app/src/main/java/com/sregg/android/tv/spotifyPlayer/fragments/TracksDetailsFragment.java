package com.sregg.android.tv.spotifyPlayer.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.DetailsFragment;
import android.support.v17.leanback.widget.Action;
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

import com.spotify.sdk.android.player.PlayerState;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;
import com.sregg.android.tv.spotifyPlayer.BusProvider;
import com.sregg.android.tv.spotifyPlayer.R;
import com.sregg.android.tv.spotifyPlayer.SpotifyTvApplication;
import com.sregg.android.tv.spotifyPlayer.controllers.SpotifyPlayerController;
import com.sregg.android.tv.spotifyPlayer.events.ContentState;
import com.sregg.android.tv.spotifyPlayer.events.PlayerStateChanged;
import com.sregg.android.tv.spotifyPlayer.presenters.TracksHeaderRowPresenter;
import com.sregg.android.tv.spotifyPlayer.rows.TrackRow;
import com.sregg.android.tv.spotifyPlayer.rows.TracksHeaderRow;
import com.sregg.android.tv.spotifyPlayer.utils.BlurTransformation;
import com.sregg.android.tv.spotifyPlayer.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.TrackSimple;

public abstract class TracksDetailsFragment extends DetailsFragment {
    protected static final long ACTION_PLAY_PAUSE_PLAYLIST = 1;
    private static final String TAG = TracksDetailsFragment.class.getSimpleName();

    private static final int DETAIL_THUMB_WIDTH = 274;
    private static final int DETAIL_THUMB_HEIGHT = 274;
    protected SpotifyPlayerController playerController;
    protected ArrayObjectAdapter actionsAdapter;

    private BackgroundManager mBackgroundManager;
    private DisplayMetrics mMetrics;
    private ArrayObjectAdapter mRowsAdapter;

    private DetailsOverviewRowPresenter mDetailsPresenter;
    private DetailsOverviewRow mDetailsRow;
    private Object busEventListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        busEventListener = new Object() {
            @Subscribe
            public void onPlayerStateChanged(final PlayerStateChanged event) {
                TracksDetailsFragment.this.onPlayerStateChanged(event);
            }
        };
        BusProvider.getInstance().register(busEventListener);

        playerController = SpotifyTvApplication.getInstance().getSpotifyPlayerController();

        setupFragment();
        setupBackground();
    }

    public SpotifyPlayerController getPlayerController() {
        return playerController;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BusProvider.getInstance().unregister(busEventListener);
    }

    protected abstract Presenter getDetailsPresenter();

    protected abstract Presenter getTrackRowPresenter(OnItemViewClickedListener onTrackRowItemClicked);

    private void setupFragment() {
        mDetailsPresenter = new DetailsOverviewRowPresenter(getDetailsPresenter());
        mDetailsPresenter.setStyleLarge(false);

        ClassPresenterSelector ps = new ClassPresenterSelector();
        ps.addClassPresenter(DetailsOverviewRow.class, mDetailsPresenter);
        ps.addClassPresenter(TracksHeaderRow.class, new TracksHeaderRowPresenter());

        OnItemViewClickedListener onTrackRowItemClicked = new OnItemViewClickedListener() {
            @Override
            public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
                if (item instanceof TrackSimple) {
                    playFromTrack(((TrackSimple) item));
                }
            }
        };

        ps.addClassPresenter(TrackRow.class, getTrackRowPresenter(onTrackRowItemClicked));

        mRowsAdapter = new ArrayObjectAdapter(ps);
        setAdapter(mRowsAdapter);

        setOnActionClickedListener(new OnActionClickedListener() {
            @Override
            public void onActionClicked(Action action) {
                TracksDetailsFragment.this.onActionClicked(action);
            }
        });
    }

    /**
     * Called when an Action has been clicked
     *
     * @param action
     * @return true if the action click has been consumed
     */
    protected boolean onActionClicked(Action action) {
        if (action.getId() == ACTION_PLAY_PAUSE_PLAYLIST) {
            if (isContentPlaying()) {
                playerController.togglePauseResume();
            } else {
                playerController.play(getObjectUri(), getTrackUris(), getTracks());
            }
            return true;
        }
        return false;
    }

    protected void onContentLoaded(Object item) {
        if (!isAdded()){
            return;
        }

        DetailsOverviewRow detailsRow = new DetailsOverviewRow(item);

        actionsAdapter = new ArrayObjectAdapter();
        populateActionsAdapter();
        detailsRow.setActionsAdapter(actionsAdapter);

        setDetailsRow(detailsRow);

        setupTracksRows(getTracks());
    }

    private void playFromTrack(TrackSimple item) {
        List<TrackSimple> tracks = getTracks();
        List<String> trackUris = getTrackUris();
        int index = trackUris.indexOf(item.uri);
        List<TrackSimple> tracksSubList = tracks.subList(index, tracks.size());
        List<String> uriSubList = trackUris.subList(index, trackUris.size());
        List<String> subList = trackUris.subList(trackUris.indexOf(item.uri), trackUris.size());
        SpotifyTvApplication.getInstance().getSpotifyPlayerController().play(getObjectUri(), uriSubList, tracksSubList);
    }

    @Nullable
    protected abstract List<TrackSimple> getTracks();

    @Nullable
    protected abstract List<String> getTrackUris();

    @Nullable
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
        if (mDetailsRow != null) {
            mRowsAdapter.remove(mDetailsRow);
        }
        mDetailsRow = row;
        mRowsAdapter.add(0, mDetailsRow);
    }

    protected void setupTracksRows(List<TrackSimple> tracks) {
        if (mRowsAdapter.size() < 2) {
            mRowsAdapter.add(new TracksHeaderRow());
            List<TrackRow> trackRows = new ArrayList<>(tracks.size());
            for (TrackSimple track : tracks) {
                trackRows.add(new TrackRow(track));
            }
            mRowsAdapter.addAll(mRowsAdapter.size(), trackRows);
        }
    }

    protected void loadDetailsRowImage(String imageUrl) {
        if (imageUrl == null) {
            mDetailsRow.setImageBitmap(getActivity(), null);
            mBackgroundManager.setBitmap(null);
        } else {
            new ImageLoader(getActivity()).execute(imageUrl);
        }
    }

    private boolean isContentPlaying() {
        ContentState currentPlayState = playerController.getPlayingState();
        return currentPlayState != null && currentPlayState.isCurrentObject(getObjectUri());
    }

    private void populateActionsAdapter() {
        boolean playlistPlaying = isContentPlaying();
        Action playAction = new Action(ACTION_PLAY_PAUSE_PLAYLIST, playlistPlaying ? getResources().getString(R.string.lb_playback_controls_pause) : getResources().getString(R.string.lb_playback_controls_play), null);

        actionsAdapter.add(playAction);
        List<Action> actions = getDetailActions();
        if (actions != null) {
            for (Action action : getDetailActions()) {
                actions.add(action);
            }
        }
    }

    public void onPlayerStateChanged(PlayerStateChanged playerStateChanged) {
        PlayerState playerState = playerStateChanged.getPlayerState();
        boolean playlistPlaying = isContentPlaying() && playerState.playing;
        Action action = (Action) actionsAdapter.get(0);
        action.setLabel1(playlistPlaying ? getResources().getString(R.string.lb_playback_controls_pause) : getResources().getString(R.string.lb_playback_controls_play));
        actionsAdapter.notifyArrayItemRangeChanged(0, 1);
    }

    abstract List<Action> getDetailActions();

    private class ImageLoader extends AsyncTask<String, Void, Void> {

        private Context context;

        public ImageLoader(Context context) {
            this.context = context.getApplicationContext();
        }

        private Bitmap mBackground;

        @Override
        protected Void doInBackground(String... params) {
            if (mDetailsRow == null || mDetailsPresenter == null || !isAdded()) {
                return null;
            }

            Bitmap cover;
            try {
                cover = Picasso.with(this.context)
                        .load(params[0])
                        .resize(
                                Utils.dpToPx(DETAIL_THUMB_WIDTH, this.context),
                                Utils.dpToPx(DETAIL_THUMB_HEIGHT, this.context)
                        )
                        .centerCrop()
                        .get();

                mDetailsRow.setImageBitmap(this.context, cover);

                Palette palette = Palette.generate(cover);
                Palette.Swatch swatch = palette.getDarkVibrantSwatch();

                if (swatch == null) {
                    swatch = palette.getDarkMutedSwatch();
                }

                if (swatch != null) {
                    mDetailsPresenter.setBackgroundColor(swatch.getRgb());
                }

                mBackground = Picasso.with(this.context)
                        .load(params[0])
                        .transform(new BlurTransformation(this.context))
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
            if (!isAdded()) {
                return;
            }
            mBackgroundManager.setBitmap(mBackground);
            setAdapter(mRowsAdapter);
        }
    }
}
