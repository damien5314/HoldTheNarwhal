package com.ddiehl.android.simpleredditreader.events;

import com.squareup.otto.Bus;

/**
 * Created by Damien on 1/19/2015.
 */
public class BusProvider {
    private static Bus mBus = new Bus();

    private BusProvider() { }

    public static Bus getInstance() {
        if (mBus == null) {
            mBus = new Bus();
        }

        return mBus;
    }
}
