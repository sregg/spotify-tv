package com.sregg.android.tv.spotify.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.format.DateFormat;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.sregg.android.tv.spotify.R;
import com.sregg.android.tv.spotify.SpotifyTvApplication;
import com.sregg.android.tv.spotify.activities.MainActivity;
import com.sregg.android.tv.spotify.utils.Utils;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.FeaturedPlaylists;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.RetrofitError;

public class RecommendationsService extends IntentService {
    private static final String TAG = "RecommendationsService";
    private static final int MAX_RECOMMENDATIONS = 3;

    private static final int CARD_WIDTH = 274;
    private static final int CARD_HEIGHT = 274;

    private final NotificationManager mNotificationManager;

    public RecommendationsService() {
        super(RecommendationsService.class.getSimpleName());

        mNotificationManager = (NotificationManager) SpotifyTvApplication.getInstance().getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!Utils.isRunningOnAndroidTV(getApplicationContext())) {
            return;
        }

        Log.d(TAG, "Updating recommendation");

        try {
            loadRecommendationsData();
        } catch (RetrofitError error) {
            error.printStackTrace();
        }
    }

    private void loadRecommendationsData() throws RetrofitError {
        SpotifyTvApplication app = SpotifyTvApplication.getInstance();
        SpotifyService spotifyService = app.getSpotifyService();
        UserPrivate user = spotifyService.getMe();

        if (user == null) {
            return;
        }

        Map<String, Object> options = new HashMap<>();
        options.put(SpotifyService.COUNTRY, user.country);
        options.put("timestamp", DateFormat.format("yyyy-MM-dd'T'hh:mm:ss", new Date()));
        FeaturedPlaylists featuredPlaylists = spotifyService.getFeaturedPlaylists(options);

        if (featuredPlaylists == null) {
            return;
        }

        int count = 0;

        for (PlaylistSimple playlistSimple : featuredPlaylists.playlists.items) {
            Playlist playlist = spotifyService.getPlaylist(playlistSimple.owner.id, playlistSimple.id);

            Log.d(TAG, "Recommendation - Featured Playlist - " + playlist.name);

            mNotificationManager.notify(playlist.id.hashCode(), buildNotification(playlist));

            if (++count >= MAX_RECOMMENDATIONS) {
                break;
            }
        }
    }

    private Notification buildNotification(Playlist playlist) {
        Bitmap image = null;
        if (playlist.images.size() > 0) {
            try {
                image = Picasso.with(getApplicationContext())
                        .load(playlist.images.get(0).url)
                        .resize(CARD_WIDTH, CARD_HEIGHT)
                        .centerCrop()
                        .get();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return new NotificationCompat.BigPictureStyle(
                new NotificationCompat.Builder(getApplicationContext())
                        .setContentTitle(playlist.name)
                        .setContentText(playlist.description)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setLocalOnly(true)
                        .setOngoing(true)
                        .setColor(getResources().getColor(R.color.fastlane_background))
                        .setCategory(Notification.CATEGORY_RECOMMENDATION)
                        .setLargeIcon(image)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentIntent(buildPendingIntent(playlist.id, playlist.uri))
                        .setExtras(null))
                .build();
    }

    private PendingIntent buildPendingIntent(String id, String itemUri) {
        Intent detailsIntent = new Intent(this, MainActivity.class);
        detailsIntent.setData(Uri.parse(itemUri));
        // Ensure a unique PendingIntents, otherwise all recommendations end up with the same PendingIntent
        detailsIntent.setAction(id);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(detailsIntent);

        PendingIntent intent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        return intent;
    }
}
