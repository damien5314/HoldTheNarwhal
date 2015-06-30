/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.reddit;

public interface Savable {

    String getName();
    Boolean isSaved();
    void isSaved(boolean b);
}
