/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.reddit.listings;

import com.ddiehl.reddit.identity.UserIdentity;

public class UserIdentityListing extends Listing<UserIdentity> {

    public UserIdentity getUser() {
        return data;
    }

    @Override
    public String toString() {
        return getId() + " - " + getName() + " - Gold: " + data.isGold();
    }

}
