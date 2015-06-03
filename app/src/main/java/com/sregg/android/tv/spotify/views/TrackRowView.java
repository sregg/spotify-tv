package com.sregg.android.tv.spotify.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.squareup.otto.Subscribe;
import com.sregg.android.tv.spotify.BusProvider;
import com.sregg.android.tv.spotify.R;
import com.sregg.android.tv.spotify.events.AbsPlayingEvent;
import com.sregg.android.tv.spotify.events.OnPause;
import com.sregg.android.tv.spotify.events.OnPlay;
import com.sregg.android.tv.spotify.events.OnTrackChanged;

public class TrackRowView extends LinearLayout {

    private String mUri;
    private NowPlayingIndicatorView mNowPlayingView;

    private TextView mArtistTextView;
    private TextView mTrackTextView;
    private TextView mTrackLengthTextView;
    private TextView mTrackNumberTextView;

    public TrackRowView(Context context) {
        super(context);
        init();
    }

    public TrackRowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TrackRowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public TrackRowView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    private void init() {
        mArtistTextView = (TextView) findViewById(R.id.track_artist);
        mNowPlayingView = (NowPlayingIndicatorView) findViewById(R.id.track_now_playing);
        mTrackTextView = (TextView) findViewById(R.id.track_name);
        mTrackLengthTextView = (TextView) findViewById(R.id.track_length);
        mTrackNumberTextView = (TextView) findViewById(R.id.track_number);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        BusProvider.getInstance().register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        BusProvider.unregister(this);
        super.onDetachedFromWindow();
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onTrackStart(OnTrackChanged onTrackChanged) {
        initNowPlaying(isSelf(onTrackChanged));
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onPlay(OnPlay onPlay) {
        if (isSelf(onPlay)) {
            mNowPlayingView.startAnimation();
        }
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onPause(OnPause onPause) {
        if (isSelf(onPause)) {
            mNowPlayingView.stopAnimations();
        }
    }

    private boolean isSelf(AbsPlayingEvent playingEvent) {
        return playingEvent.getPlayingState().isCurrentObjectOrTrack(mUri);
    }

    public void initNowPlaying(boolean isSelf) {
        if (isSelf) {
            mNowPlayingView.startAnimation();
        } else {
            mNowPlayingView.stopAnimations();
        }
    }

    public NowPlayingIndicatorView getNowPlayingView() {
        return mNowPlayingView;
    }

    public String getUri() {
        return mUri;
    }

    public void setUri(String uri) {
        mUri = uri;
    }

    public TextView getArtistTextView() {
        return mArtistTextView;
    }

    public TextView getTrackTextView() {
        return mTrackTextView;
    }

    public TextView getTrackLengthTextView() {
        return mTrackLengthTextView;
    }

    public TextView getTrackNumberTextView() {
        return mTrackNumberTextView;
    }
}
