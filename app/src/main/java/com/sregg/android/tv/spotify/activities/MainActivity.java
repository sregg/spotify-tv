/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.sregg.android.tv.spotify.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.sregg.android.tv.spotify.R;
import com.sregg.android.tv.spotify.SpotifyTvApplication;
import com.sregg.android.tv.spotify.controllers.SpotifyPlayerController;
import com.sregg.android.tv.spotify.services.RecommendationsService;
import com.sregg.android.tv.spotify.utils.SpotifyUriLoader;

import retrofit.RetrofitError;
import retrofit.client.Response;

/*
 * MainActivity class that loads MainFragment
 */
public class MainActivity extends Activity {


    public static final int REQUEST_CODE = 0;

    /**
     * Called when the activity is first created.
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        login();
    }

    private void init() {
        setContentView(R.layout.activity_main);

        startService(new Intent(this, RecommendationsService.class));

        if (getIntent() != null && getIntent().getData() != null) {
            SpotifyUriLoader.loadObjectFromUri(SpotifyTvApplication.getInstance().getSpotifyService(), getIntent().getData().toString(), new SpotifyUriLoader.SpotifyObjectLoaderCallback() {
                @Override
                public void success(Object object, Response response) {
                    SpotifyTvApplication.getInstance().launchDetailScreen(MainActivity.this, object);
                }

                @Override
                public void failure(RetrofitError error) {

                }
            });
        }
    }

    private void login() {
        String clientId = getString(R.string.spotify_client_id);
        String[] scopes = getResources().getStringArray(R.array.spotify_scopes);
        String redirectUri = getString(R.string.spotify_callback_uri_scheme)
                + "://" + getString(R.string.spotify_callback_uri_host);

        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(clientId,
                AuthenticationResponse.Type.TOKEN,
                redirectUri);
        builder.setScopes(scopes);
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                SpotifyTvApplication.getInstance().startSpotifySession(response.getAccessToken());
                init();
            }
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                SpotifyPlayerController spotifyPlayerController = SpotifyTvApplication.getInstance().getSpotifyPlayerController();

                if (spotifyPlayerController != null) {
                    spotifyPlayerController.togglePauseResume();
                }
                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    @Override
    public boolean onSearchRequested() {
        startActivity(new Intent(this, SearchActivity.class));
        return true;
    }
}
