package com.sregg.android.tv.spotify.testUtils;

import android.os.Bundle;
import android.support.test.runner.AndroidJUnitRunner;

import rx.plugins.RxJavaPlugins;

/**
 * This runner creates an Observable hook and registers it as
 * an Espresso idling resource so that your Espresso tests
 * will wait for async Rx subscriptions to finish before
 * progressing.
 * <p/>
 * taken from https://github.com/rosshambrick/RxEspresso
 */
public class RxAndroidJUnitRunner extends AndroidJUnitRunner {
    @Override
    public void onCreate(Bundle arguments) {
        RxJavaPlugins.getInstance().registerObservableExecutionHook(RxIdlingResource.get());

        super.onCreate(arguments);
    }
}
