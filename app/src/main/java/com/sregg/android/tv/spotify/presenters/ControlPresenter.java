package com.sregg.android.tv.spotify.presenters;

import android.content.Context;
import android.support.v17.leanback.widget.Presenter;
import android.view.ViewGroup;
import android.widget.IconTextView;
import android.widget.TextView;
import com.sregg.android.tv.spotify.R;
import com.sregg.android.tv.spotify.SpotifyTvApplication;
import com.sregg.android.tv.spotify.enums.Control;
import com.sregg.android.tv.spotify.views.ControlView;

public class ControlPresenter extends Presenter {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        Context context = parent.getContext();
        ControlView controlView = new ControlView(context);
        int size = context.getResources().getDimensionPixelSize(R.dimen.control_view_size);
        controlView.setLayoutParams(new ViewGroup.LayoutParams(size, size));
        controlView.setBackgroundColor(context.getResources().getColor(R.color.control_bg));
        controlView.setFocusable(true);
        controlView.setFocusableInTouchMode(true);
        return new ViewHolder(controlView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        ControlView controlView = (ControlView) viewHolder.view;
        Control control = (Control) item;

        controlView.setControl(control);
        IconTextView fontIconIV = controlView.getIconTextView();
        fontIconIV.setText(control.getFontId(), TextView.BufferType.NORMAL);

        boolean isOn;
        if (control == Control.SHUFFLE) {
            isOn = SpotifyTvApplication.getInstance().getSpotifyPlayerController().isShuffleOn();
        } else {
            isOn = false;
        }
        controlView.toggleControlColor(isOn);
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {

    }
}
