package com.ddiehl.reddit;

public interface Votable extends Archivable {
  String getId();
  String getKind();
  void applyVote(int direction);
  Boolean isLiked();
  void isLiked(Boolean b);
}
