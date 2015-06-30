/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn;

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
