package com.sregg.android.tv.spotify.controllers;

import android.util.Log;

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

    private static LastFmApi INSTANCE;

    private Session mLastFmSession;

    public static LastFmApi getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LastFmApi();
        }

        return INSTANCE;
    }

    private LastFmApi() {
        // Last fm API set up
        Caller lastFmCaller = Caller.getInstance();
        lastFmCaller.setUserAgent(System.getProperties().getProperty("http.agent"));
        lastFmCaller.setDebugMode(true);
        lastFmCaller.setCache(new MemoryCache());
    }

    public void scrobbleSpotifyTrack(Track track) {
        if (mLastFmSession == null) {
            mLastFmSession = Authenticator.getMobileSession("sreggiani", "", "beab8868add5f873323d2d5012443c66", "075b8c312c0ffa4ba28607d36fe0c038");
        }

        int now = (int) System.currentTimeMillis() / 1000;
        ScrobbleData scrobbleData = new ScrobbleData(track.artists.get(0).name, track.name, now);

        ScrobbleResult resultNP = de.umass.lastfm.Track.updateNowPlaying(scrobbleData, mLastFmSession);
        Log.d(TAG, "updateNowPlaying ok: " + (resultNP.isSuccessful() && !resultNP.isIgnored()));

        ScrobbleResult result = de.umass.lastfm.Track.scrobble(scrobbleData, mLastFmSession);
        Log.d(TAG, "scrobble ok: " + (result.isSuccessful() && !result.isIgnored()));
    }
}
