package com.sregg.android.tv.spotifyPlayer.testUtils;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.util.Log;

import java.util.concurrent.atomic.AtomicInteger;

import rx.Observable;
import rx.Subscriber;
import rx.plugins.RxJavaObservableExecutionHook;


/**
 * Provides the hooks for both RxJava and Espresso so that Espresso knows when to wait
 * until RxJava subscriptions have completed.
 * <p/>
 * taken from https://github.com/rosshambrick/RxEspresso
 */

public final class RxIdlingResource extends RxJavaObservableExecutionHook implements IdlingResource {
    public static final String TAG = "RxIdlingResource";

    private final AtomicInteger subscriptions = new AtomicInteger(0);

    private static RxIdlingResource INSTANCE;

    private ResourceCallback resourceCallback;

    // set to true to enable logging
    private static boolean logEnabled = false;

    private RxIdlingResource() {
        //private
    }

    public static RxIdlingResource get() {
        if (INSTANCE == null) {
            INSTANCE = new RxIdlingResource();
            Espresso.registerIdlingResources(INSTANCE);
        }
        return INSTANCE;
    }

    /* ======================== */
    /* IdlingResource Overrides */
    /* ======================== */

    @Override
    public String getName() {
        return TAG;
    }

    @Override
    public boolean isIdleNow() {
        int activeSubscriptionCount = subscriptions.get();
        boolean isIdle = activeSubscriptionCount == 0;

        if (logEnabled) {
            Log.d(TAG, "activeSubscriptionCount: " + activeSubscriptionCount);
            Log.d(TAG, "isIdleNow: " + isIdle);
        }

        return isIdle;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
        Log.d(TAG, "registerIdleTransitionCallback");
        this.resourceCallback = resourceCallback;
    }

    /* ======================================= */
    /* RxJavaObservableExecutionHook Overrides */
    /* ======================================= */

    @Override
    public <T> Observable.OnSubscribe<T> onSubscribeStart(Observable<? extends T> observableInstance,
                                                          final Observable.OnSubscribe<T> onSubscribe) {
        int activeSubscriptionCount = subscriptions.incrementAndGet();

        if (logEnabled) {
            Log.d(TAG, onSubscribe + " - onSubscribeStart: " + activeSubscriptionCount, new Throwable());
        }

        onSubscribe.call(new Subscriber<T>() {
            @Override
            public void onCompleted() {
                onFinally(onSubscribe, "onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                onFinally(onSubscribe, "onError");
            }

            @Override
            public void onNext(T t) {
                //nothing
            }
        });

        return onSubscribe;
    }

    private <T> void onFinally(Observable.OnSubscribe<T> onSubscribe, final String finalizeCaller) {
        int activeSubscriptionCount = subscriptions.decrementAndGet();
        if (logEnabled) {
            Log.d(TAG, onSubscribe + " - " + finalizeCaller + ": " + activeSubscriptionCount);
        }
        if (activeSubscriptionCount == 0) {
            if (logEnabled) {
                Log.d(TAG, "onTransitionToIdle");
            }
            resourceCallback.onTransitionToIdle();
        }
    }
}
