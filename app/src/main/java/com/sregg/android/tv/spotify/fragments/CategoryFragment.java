package com.sregg.android.tv.spotify.fragments;

import android.os.Bundle;
import android.support.v17.leanback.app.VerticalGridFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.VerticalGridPresenter;
import android.util.Log;

import com.sregg.android.tv.spotify.BusProvider;
import com.sregg.android.tv.spotify.SpotifyTvApplication;
import com.sregg.android.tv.spotify.activities.CategoryActivity;
import com.sregg.android.tv.spotify.presenters.PlaylistSimpleCardPresenter;

import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.PlaylistsPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class CategoryFragment extends VerticalGridFragment {

    private static final String TAG = CategoryFragment.class.getSimpleName();

    private static final int NUM_COLUMNS = 5;

    private ArrayObjectAdapter mAdapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        setTitle(getActivity().getIntent().getStringExtra(CategoryActivity.ARG_CATEGORY_NAME));

        setupFragment();

        loadCategory(getActivity().getIntent().getStringExtra(CategoryActivity.ARG_CATEGORY_ID));
    }

    private void setupFragment() {
        mAdapter = new ArrayObjectAdapter(new PlaylistSimpleCardPresenter());

        setAdapter(mAdapter);

        setOnItemViewClickedListener(new OnItemViewClickedListener() {
            @Override
            public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
                SpotifyTvApplication.getInstance().launchDetailScreen(getActivity(), item);
            }
        });
    }

    private void loadCategory(String categoryId) {
        Map<String, Object> options = new HashMap<>();
        options.put(SpotifyService.COUNTRY, SpotifyTvApplication.getInstance().getCurrentUserCountry());
        SpotifyTvApplication.getInstance().getSpotifyService().getPlaylistsForCategory(categoryId, options, new Callback<PlaylistsPager>() {
            @Override
            public void success(PlaylistsPager playlistsPager, Response response) {
                mAdapter.addAll(mAdapter.size(), playlistsPager.playlists.items);
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        VerticalGridPresenter gridPresenter = new VerticalGridPresenter();
        gridPresenter.setNumberOfColumns(NUM_COLUMNS);
        setGridPresenter(gridPresenter);

        BusProvider.getInstance().register(this);
    }

    @Override
    public void onDestroy() {
        BusProvider.getInstance().unregister(this);

        super.onDestroy();
    }

}
