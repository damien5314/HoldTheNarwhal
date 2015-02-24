package com.ddiehl.android.simpleredditreader.model.listings;

public abstract class AbsRedditComment extends Listing {
    
    private int depth;
    private boolean isVisible = true;
    private boolean isCollapsed = false;

    public int getDepth() {
        return this.depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public boolean isVisible() {
        return this.isVisible;
    }

    public void setVisible(boolean b) {
        this.isVisible = b;
    }

    public boolean isCollapsed() {
        return this.isCollapsed;
    }

    public void setCollapsed(boolean b) {
        this.isCollapsed = b;
    }
    
}
