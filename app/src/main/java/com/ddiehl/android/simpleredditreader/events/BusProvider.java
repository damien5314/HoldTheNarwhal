package com.ddiehl.android.simpleredditreader.events;

import com.squareup.otto.Bus;


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
