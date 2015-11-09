package com.sregg.android.tv.spotifyPlayer.presenters;

import android.content.Context;
import android.support.v17.leanback.widget.Presenter;
import android.view.ViewGroup;

import com.sregg.android.tv.spotifyPlayer.settings.Setting;
import com.sregg.android.tv.spotifyPlayer.views.SettingView;

public class SettingPresenter extends Presenter {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        Context context = parent.getContext();
        SettingView settingView = new SettingView(context);
        return new ViewHolder(settingView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        SettingView settingView = (SettingView) viewHolder.view;
        Setting setting = (Setting) item;
        settingView.setSetting(setting);
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {

    }
}
