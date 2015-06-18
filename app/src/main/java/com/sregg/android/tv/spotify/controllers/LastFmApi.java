package com.sregg.android.tv.spotify.controllers;

import android.text.TextUtils;
import android.util.Log;

import com.sregg.android.tv.spotify.SpotifyTvApplication;
import com.sregg.android.tv.spotify.settings.UserPreferences;
import de.umass.lastfm.Authenticator;
import de.umass.lastfm.Caller;
import de.umass.lastfm.Session;
import de.umass.lastfm.cache.MemoryCache;
import de.umass.lastfm.scrobble.ScrobbleData;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by simonreggiani on 15-03-14.
 */
public class LastFmApi {
    public static final String TAG = "LastFmApi";
    private static final String API_KEY = "beab8868add5f873323d2d5012443c66";
    private static final String API_SECRET = "075b8c312c0ffa4ba28607d36fe0c038";

    private static LastFmApi INSTANCE;

    private Session mLastFmSession;

    public static LastFmApi getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LastFmApi();
        }

        return INSTANCE;
    }

    private LastFmApi() {
    }

    public void scrobbleSpotifyTrack(Track track) {
        // start session if not started
        if (mLastFmSession == null) {

            UserPreferences prefs = UserPreferences.getInstance(SpotifyTvApplication.getInstance().getApplicationContext());
            String lastFmUsername = prefs.getLastFmUsername();
            String lastFmPassword = prefs.getLastFmPassword();

            // if not set, don't do anything
            if (TextUtils.isEmpty(lastFmUsername) || TextUtils.isEmpty(lastFmPassword)) {
                return;
            }

            startLastFmSession(lastFmUsername, lastFmPassword);
        }

        if (mLastFmSession == null) {
            // if the session is still null, that means that we couldn't authenticate the user on last.fm
            // (e.g. username/password is incorrect)
            return;
        }

        int now = (int) (System.currentTimeMillis() / 1000);
        ScrobbleData scrobbleData = new ScrobbleData(track.artists.get(0).name, track.name, now);

        de.umass.lastfm.Track.updateNowPlaying(scrobbleData, mLastFmSession);
        de.umass.lastfm.Track.scrobble(scrobbleData, mLastFmSession);
    }

    public boolean startLastFmSession(String lastFmUsername, String lastFmPassword) {
        // Last fm API set up
        Caller lastFmCaller = Caller.getInstance();
        lastFmCaller.setUserAgent(System.getProperties().getProperty("http.agent"));
        lastFmCaller.setDebugMode(true);
        lastFmCaller.setCache(new MemoryCache());

        try {
            mLastFmSession = Authenticator.getMobileSession(lastFmUsername, lastFmPassword, API_KEY, API_SECRET);
        } catch (Exception e) {
            Log.e(TAG, "Error while getting last.fm session", e);
            return false;
        }

        return mLastFmSession != null;
    }
}
