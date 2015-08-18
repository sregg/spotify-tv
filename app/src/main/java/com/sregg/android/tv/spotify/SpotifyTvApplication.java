package com.sregg.android.tv.spotify;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.Spotify;
import com.sregg.android.tv.spotify.activities.AlbumActivity;
import com.sregg.android.tv.spotify.activities.ArtistsAlbumsActivity;
import com.sregg.android.tv.spotify.activities.CategoryActivity;
import com.sregg.android.tv.spotify.activities.PlaylistActivity;
import com.sregg.android.tv.spotify.controllers.SpotifyPlayerController;

import io.fabric.sdk.android.Fabric;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Category;
import kaaes.spotify.webapi.android.models.PlaylistBase;
import kaaes.spotify.webapi.android.models.User;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by simonreggiani on 15-01-18.
 */
public class SpotifyTvApplication extends Application {

    private static final String SHARED_PREFS_NAME = "SpotifyTvApplicationSharedPref";
    private static final String KEY_CURRENT_USER = "CurrentUser";
    private static final String PREMIUM = "premium";

    private static SpotifyTvApplication sInstance;
    private SpotifyPlayerController mSpotifyPlayerController;
    private SpotifyService mSpotifyService;
    private User mCurrentUser;

    private Gson gson;
    private SharedPreferences sharedPreferences;

    public SpotifyTvApplication() {
    }

    public static SpotifyTvApplication getInstance() {
        checkInstance();
        return sInstance;
    }

    private static void checkInstance() {
        if (sInstance == null)
            throw new IllegalStateException("Application not created yet!");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        sInstance = this;
    }

    public SpotifyPlayerController getSpotifyPlayerController() {
        return mSpotifyPlayerController;
    }

    public void startSpotifySession(final Activity activity, String accessToken, final Runnable onStarted, final Runnable onFailed) {
        // Spotify API
        SpotifyApi api = new SpotifyApi();
        api.setAccessToken(accessToken);
        mSpotifyService = api.getService();

        // Spotify Player Controller
        String clientId = getString(R.string.spotify_client_id);
        Config playerConfig = new Config(this, accessToken, clientId);
        Player player = Spotify.getPlayer(playerConfig, this, null);
        mSpotifyPlayerController = new SpotifyPlayerController(player, mSpotifyService);

        mSpotifyService.getMe(new Callback<User>() {
            @Override
            public void success(User user, Response response) {
                saveCurrentUser(user);
                activity.runOnUiThread(onStarted);
            }

            @Override
            public void failure(RetrofitError error) {
                activity.runOnUiThread(onFailed);
            }
        });
    }

    public SpotifyService getSpotifyService() {
        return mSpotifyService;
    }

    private SharedPreferences getSharedPreferences() {
        if (sharedPreferences == null) {
            sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        }

        return sharedPreferences;
    }

    private void saveCurrentUser(User currentUser) {
        mCurrentUser = currentUser;
        String currentUserJSON = getGson().toJson(currentUser);
        getSharedPreferences().edit().putString(KEY_CURRENT_USER, currentUserJSON).apply();
    }

    private Gson getGson() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }

    public User getCurrentUser() {
        if (mCurrentUser == null) {
            String currentUserJSON = getSharedPreferences().getString(KEY_CURRENT_USER, null);
            mCurrentUser = getGson().fromJson(currentUserJSON, User.class);
        }

        return mCurrentUser;
    }

    public static String getCurrentUserCountry() {
        return getInstance().getCurrentUser().country;
    }

    public static String getCurrentUserId() {
        return getInstance().getCurrentUser().id;
    }

    public static boolean isCurrentUserPremium() {
        return getInstance().getCurrentUser().product.equals(PREMIUM);
    }

    @Override
    public void onTerminate() {
        mSpotifyPlayerController.terminate();
        super.onTerminate();
    }

    public void launchDetailScreen(Activity activity, Object item) {
        if (item instanceof AlbumSimple) {
            AlbumSimple albumSimple = (AlbumSimple) item;
            AlbumActivity.launch(activity, albumSimple.id, albumSimple.name);
        } else if (item instanceof ArtistSimple) {
            ArtistSimple artistSimple = (ArtistSimple) item;
            ArtistsAlbumsActivity.launch(activity, artistSimple.id, artistSimple.name);
        } else if (item instanceof PlaylistBase) {
            PlaylistBase playlist = (PlaylistBase) item;
            PlaylistActivity.launch(activity, playlist.id, playlist.name, playlist.owner.id);
        } else if (item instanceof Category) {
            Category category = (Category) item;
            CategoryActivity.launch(activity, category.id, category.name);
        }
    }
}
