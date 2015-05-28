package com.ddiehl.reddit;

public interface Votable extends Archivable {

    String getId();
    void applyVote(int direction);
}
