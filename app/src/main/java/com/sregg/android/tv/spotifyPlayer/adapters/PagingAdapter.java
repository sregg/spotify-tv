package com.sregg.android.tv.spotifyPlayer.adapters;

import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.PresenterSelector;

import com.sregg.android.tv.spotifyPlayer.Constants;

import java.util.Collection;

public abstract class PagingAdapter extends ArrayObjectAdapter {

    public static final int LOAD_THRESHOLD = Constants.PAGE_LIMIT;

    public PagingAdapter(PresenterSelector presenterSelector) {
        super(presenterSelector);
    }

    public PagingAdapter(Presenter presenter) {
        super(presenter);
    }

    public PagingAdapter() {
    }

    private int total;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    protected abstract void onLoadMore(int offset);

    private boolean canLoadMore() {
        return total > size();
    }

    public void addAll(int total, int index, Collection items) {
        super.addAll(index, items);
        this.total = total;
    }

    public void onItemSelected(Object item) {
        if (!canLoadMore()) {
            return;
        }

        int position = indexOf(item);
        if (position > (size() - LOAD_THRESHOLD)) {
            onLoadMore(size());
        }
    }
}
