package com.ddiehl.android.htn.events.exceptions;

public class UserRequiredException extends RuntimeException {

    public UserRequiredException() {
        super("A signed in user is required to perform this action.");
    }

}
