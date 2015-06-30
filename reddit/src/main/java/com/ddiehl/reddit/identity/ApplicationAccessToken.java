/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.reddit.identity;


public class ApplicationAccessToken extends AccessToken {

    @Override
    public boolean isUserAccessToken() {
        return false;
    }

}
