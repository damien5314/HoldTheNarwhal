/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.events.exceptions;

public class UserRequiredException extends RuntimeException {

    public UserRequiredException() {
        super("A signed in user is required to perform this action.");
    }

}
