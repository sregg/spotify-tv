package com.sregg.android.tv.spotify.controllers;

import android.util.Log;
import com.sregg.android.tv.spotify.SpotifyTvApplication;
import com.sregg.android.tv.spotify.settings.UserPreferences;
import de.umass.lastfm.Authenticator;
import de.umass.lastfm.Caller;
import de.umass.lastfm.Session;
import de.umass.lastfm.cache.MemoryCache;
import de.umass.lastfm.scrobble.ScrobbleData;
import de.umass.lastfm.scrobble.ScrobbleResult;
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
        if (mLastFmSession == null) {

            UserPreferences prefs = UserPreferences.getInstance(SpotifyTvApplication.getInstance().getApplicationContext());
            String lastFmUsername = prefs.getLastFmUsername();
            String lastFmPassword = prefs.getLastFmPassword();

            // if not set, don't do anything
            if (lastFmUsername == null || lastFmPassword == null) {
                return;
            }

            // Last fm API set up
            Caller lastFmCaller = Caller.getInstance();
            lastFmCaller.setUserAgent(System.getProperties().getProperty("http.agent"));
            lastFmCaller.setDebugMode(true);
            lastFmCaller.setCache(new MemoryCache());

            // Start session
            mLastFmSession = Authenticator.getMobileSession(lastFmUsername, lastFmPassword, API_KEY, API_SECRET);
        }

        int now = (int) (System.currentTimeMillis() / 1000);
        ScrobbleData scrobbleData = new ScrobbleData(track.artists.get(0).name, track.name, now);

        de.umass.lastfm.Track.updateNowPlaying(scrobbleData, mLastFmSession);
        de.umass.lastfm.Track.scrobble(scrobbleData, mLastFmSession);
    }
}
