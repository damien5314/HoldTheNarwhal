/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.reddit;

public interface Votable extends Archivable {

    String getId();
    void applyVote(int direction);

    Boolean isLiked();
    void isLiked(Boolean b);
}
