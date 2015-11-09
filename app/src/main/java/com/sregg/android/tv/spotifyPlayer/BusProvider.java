package com.sregg.android.tv.spotifyPlayer;


import com.squareup.otto.Bus;

/**
 * Maintains a singleton instance for obtaining the bus. Ideally this would be replaced with a more efficient means
 * such as through injection directly into interested classes.
 */
public final class BusProvider {
    private static final Bus BUS = new Bus();
    private static final String TAG = BusProvider.class.toString();

    public static Bus getInstance() {
        return BUS;
    }

    private BusProvider() {
        // No instances.
    }

    public static void register(Object object) {
        BUS.register(object);
    }

    public static void unregister(Object object) {
        try {
            BUS.unregister(object);
        } catch (IllegalArgumentException e) {
            //Logger.e(TAG, "unregister error", e);
        }
    }

    public static void post(Object object) {
        BUS.post(object);
    }
}
