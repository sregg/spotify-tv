package com.sregg.android.tv.spotifyPlayer.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.v17.leanback.widget.BaseCardView;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.sregg.android.tv.spotifyPlayer.BusProvider;
import com.sregg.android.tv.spotifyPlayer.R;
import com.sregg.android.tv.spotifyPlayer.events.AbsPlayingEvent;
import com.sregg.android.tv.spotifyPlayer.events.ContentState;
import com.sregg.android.tv.spotifyPlayer.events.OnPause;
import com.sregg.android.tv.spotifyPlayer.events.OnPlay;
import com.sregg.android.tv.spotifyPlayer.events.OnTrackChanged;
import com.sregg.android.tv.spotifyPlayer.utils.Utils;

import kaaes.spotify.webapi.android.models.TrackSimple;

public class SpotifyCardView extends BaseCardView {

    private FrameLayout mMainContainer;
    private ImageView mImageView;
    private NowPlayingIndicatorView mNowPlayingView;
    private FrameLayout mNowPlayingContainer;

    private Integer mSelectedInfoAreaBackgroundColor;
    private int mSelectedInfoAreaBacgroundDefaultColor;
    private int mInfoAreaBacgroundDefaultColor;

    private String mUri;
    private Object mItem;

    private TextView mTitleView;
    private TextView mContentView;
    private boolean mAttachedToWindow;
    private LinearLayout mInfoArea;

    public SpotifyCardView(Context context) {
        this(context, null);
    }

    public SpotifyCardView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.imageCardViewStyle);
    }

    public SpotifyCardView(Context context, int styleResId) {
        super(new ContextThemeWrapper(context, styleResId), null, 0);
        buildImageCardView(styleResId);
    }

    public SpotifyCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(getStyledContext(context, attrs, defStyleAttr), attrs, defStyleAttr);
        buildImageCardView(getImageCardViewStyle(context, attrs, defStyleAttr));
    }

    private void buildImageCardView(int styleResId) {
        setFocusable(true);
        setFocusableInTouchMode(true);

        Context context = getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.spotify_card_view, this);

        mInfoArea = (LinearLayout) view.findViewById(R.id.info_field);
        mNowPlayingContainer = (FrameLayout) view.findViewById(R.id.now_playing_container);
        mMainContainer = (FrameLayout) view.findViewById(R.id.main_container);
        mImageView = (ImageView) view.findViewById(R.id.image_view_main);
        mNowPlayingView = (NowPlayingIndicatorView) view.findViewById(R.id.now_playing_indicator_view);
        mTitleView = (TextView) findViewById(R.id.title_text);
        mContentView = (TextView) findViewById(R.id.content_text);

        TypedArray cardAttrs =
                getContext().obtainStyledAttributes(styleResId, R.styleable.SpotifyCardView);

        int cardBackgroundColor =
                cardAttrs.getInt(R.styleable.SpotifyCardView_spotify_card_background_color,
                        ContextCompat.getColor(context, R.color.lb_details_description_color));
        int textColor =
                cardAttrs.getInt(R.styleable.SpotifyCardView_spotify_card_text_color,
                        ContextCompat.getColor(context, android.R.color.white));

        setTextColor(textColor);
        setCardBackgroundColor(cardBackgroundColor);

        cardAttrs.recycle();

        // init image
        setMainContainerDimensions(getImageSize(), getImageSize());
    }


    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mAttachedToWindow = true;
        if (mImageView.getAlpha() == 0) fadeIn();
        BusProvider.getInstance().register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        mAttachedToWindow = false;
        mImageView.animate().cancel();
        mImageView.setAlpha(1f);
        BusProvider.unregister(this);
        super.onDetachedFromWindow();
    }

    public void setItem(Object item) {
        mItem = item;
        mUri = Utils.getUriFromSpotiyObject(item);
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onTrackChanged(OnTrackChanged onTrackChanged) {
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
        ContentState contentState = playingEvent.getPlayingState();
        return mItem instanceof TrackSimple ? contentState.isCurrentTrack(mUri) : contentState.isCurrentObject(mUri);
    }

    public void initNowPlaying(boolean isSelf) {
        if (isSelf) {
            mNowPlayingContainer.setVisibility(VISIBLE);
            mNowPlayingView.startAnimation();
        } else {
            mNowPlayingContainer.setVisibility(GONE);
            mNowPlayingView.stopAnimations();
        }
    }

    public void setInfoAreaBackgroundColor(@ColorInt int color) {
        if (mInfoArea != null) {
            mInfoArea.setBackgroundColor(color);
        }
    }

    public void setInfoAreaBackground(Drawable drawable) {
        if (mInfoArea != null) {
            mInfoArea.setBackground(drawable);
        }
    }

    public void setMainContainerDimensions(int width, int height) {
        ViewGroup.LayoutParams lp = mMainContainer.getLayoutParams();
        lp.width = width;
        lp.height = height;
        mMainContainer.setLayoutParams(lp);
    }

    /**
     * Sets the image drawable with fade-in animation.
     */
    public void setMainImage(Drawable drawable) {
        setMainImage(drawable, true);
    }

    /**
     * Sets the image drawable with optional fade-in animation.
     */
    public void setMainImage(Drawable drawable, boolean fade) {
        if (mImageView == null) {
            return;
        }

        mImageView.setImageDrawable(drawable);
        if (drawable == null) {
            mImageView.animate().cancel();
            mImageView.setAlpha(1f);
            mImageView.setVisibility(View.INVISIBLE);
        } else {
            mImageView.setVisibility(View.VISIBLE);
            if (fade) {
                fadeIn();
            } else {
                mImageView.animate().cancel();
                mImageView.setAlpha(1f);
            }
        }
    }


    public int getImageSize() {
        return getResources().getDimensionPixelSize(R.dimen.card_image_view_size);
    }

    public final ImageView getMainImageView() {
        return mImageView;
    }

    public void setCardBackgroundColor(@ColorInt int color) {
        setBackgroundColor(color);
    }

    public void setTitleText(CharSequence text) {
        mTitleView.setText(text);
    }

    public void setContentText(CharSequence text) {
        mContentView.setText(text);
    }

    public void setTextColor(@ColorInt int color) {
        mTitleView.setTextColor(color);
        mContentView.setTextColor(color);
    }

    private static Context getStyledContext(Context context, AttributeSet attrs, int defStyleAttr) {
        int style = getImageCardViewStyle(context, attrs, defStyleAttr);
        return new ContextThemeWrapper(context, style);
    }

    private static int getImageCardViewStyle(Context context, AttributeSet attrs, int defStyleAttr) {
        int style = null == attrs ? 0 : attrs.getStyleAttribute();
        if (0 == style) {
            TypedArray styledAttrs = context.obtainStyledAttributes(R.styleable.SpotifyCardView);
            style = styledAttrs.getResourceId(R.styleable.SpotifyCardView_spotify_card_theme, 0);
            styledAttrs.recycle();
        }
        return style;
    }

    private void fadeIn() {
        mImageView.setAlpha(0f);
        if (mAttachedToWindow) {
            int duration =
                    mImageView.getResources().getInteger(android.R.integer.config_shortAnimTime);
            mImageView.animate().alpha(1f).setDuration(duration);
        }
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);

        // change info area bg color based on palette color from image
        Integer color;
        if (selected) {
            color = mSelectedInfoAreaBackgroundColor;
            if (color == null) {
                color = mSelectedInfoAreaBacgroundDefaultColor;
            }
        } else {
            color = mInfoAreaBacgroundDefaultColor;
        }

        setInfoAreaBackgroundColor(color);
    }

    public void setSelectedInfoAreaBackgroundColor(Integer selectedInfoAreaBackgroundColor) {
        mSelectedInfoAreaBackgroundColor = selectedInfoAreaBackgroundColor;
    }

    public View getNowPlayingView() {
        return mNowPlayingContainer;
    }
}