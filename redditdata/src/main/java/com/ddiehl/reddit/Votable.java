package com.ddiehl.reddit;

public interface Votable {

    String getId();
    void applyVote(int direction);
}
