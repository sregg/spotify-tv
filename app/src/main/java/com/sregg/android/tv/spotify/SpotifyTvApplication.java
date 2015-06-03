package com.sregg.android.tv.spotify;

import android.app.Activity;
import android.app.Application;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.Spotify;
import com.sregg.android.tv.spotify.activities.AlbumActivity;
import com.sregg.android.tv.spotify.activities.ArtistsAlbumsActivity;
import com.sregg.android.tv.spotify.activities.PlaylistActivity;
import com.sregg.android.tv.spotify.controllers.SpotifyPlayerController;
import com.sregg.android.tv.spotify.enums.Control;
import com.sregg.android.tv.spotify.settings.Setting;
import com.sregg.android.tv.spotify.utils.Utils;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistBase;
import kaaes.spotify.webapi.android.models.User;

/**
 * Created by simonreggiani on 15-01-18.
 */
public class SpotifyTvApplication extends Application {
    private static SpotifyTvApplication sInstance;
    private SpotifyPlayerController mSpotifyPlayerController;
    private SpotifyService mSpotifyService;
    private User mCurrentUser;

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
        sInstance = this;
    }

    public SpotifyPlayerController getSpotifyPlayerController() {
        return mSpotifyPlayerController;
    }

    public void startSpotifySession(String accessToken) {
        // Spotify API
        SpotifyApi api = new SpotifyApi();
        api.setAccessToken(accessToken);
        mSpotifyService = api.getService();

        // Spotify Player Controller
        String clientId = getString(R.string.spotify_client_id);
        Config playerConfig = new Config(this, accessToken, clientId);
        Player player = Spotify.getPlayer(playerConfig, this, null);
        mSpotifyPlayerController = new SpotifyPlayerController(player, mSpotifyService);
    }

    public SpotifyService getSpotifyService() {
        return mSpotifyService;
    }

    public String getCurrentUserId() {
        return mCurrentUser.id;
    }

    public String getCurrentUserCountry() {
        return mCurrentUser.country;
    }

    public void setCurrentUser(User currentUser) {
        mCurrentUser = currentUser;
    }

    @Override
    public void onTerminate() {
        mSpotifyPlayerController.terminate();
        super.onTerminate();
    }

    public void onItemClick(Activity activity, Object item) {
        if (item instanceof Setting) {
            ((Setting) item).onClick(activity);
        } else if (item instanceof Control) {
            getSpotifyPlayerController().onControlClick(((Control) item));
        } else if (item instanceof AlbumSimple) {
            AlbumSimple albumSimple = (AlbumSimple) item;
            AlbumActivity.launch(activity, albumSimple.id, albumSimple.name);
        } else if (item instanceof ArtistSimple) {
            ArtistSimple artistSimple = (ArtistSimple) item;
            ArtistsAlbumsActivity.launch(activity, artistSimple.id, artistSimple.name);
        } else if (item instanceof PlaylistBase) {
            PlaylistBase playlist = (PlaylistBase) item;
            PlaylistActivity.launch(activity, playlist.id, playlist.name, playlist.owner.id);
        } else {
            String itemUri = Utils.getUriFromSpotiyObject(item);
            if (mSpotifyPlayerController.getPlayingState().isCurrentObjectOrTrack(itemUri)) {
                mSpotifyPlayerController.togglePauseResume();
            } else {
                mSpotifyPlayerController.play(item);
            }
        }
    }
}
