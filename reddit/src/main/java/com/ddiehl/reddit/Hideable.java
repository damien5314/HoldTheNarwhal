/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.reddit;

public interface Hideable {

    String getName();
    Boolean isHidden();
    void isHidden(Boolean b);
}
