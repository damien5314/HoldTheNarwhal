package com.ddiehl.android.simpleredditreader.view;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

/**
 * Created by Damien on 2/4/2015.
 */
public class ThumbnailCache {

    private static ThumbnailCache _instance;
    private static final int CACHE_SIZE = 4 * 1024 * 1024; // 4 Mb

    private LruCache<String, Bitmap> mCache;

    private ThumbnailCache() {
        mCache = new LruCache<>(CACHE_SIZE);
    }

    public static ThumbnailCache getInstance() {
        if (_instance == null) {
            synchronized (ThumbnailCache.class) {
                if (_instance == null) {
                    _instance = new ThumbnailCache();
                }
            }
        }
        return _instance;
    }

    public Bitmap addThumbnail(String key, Bitmap bitmap) {
        return mCache.put(key, bitmap);
    }

    public Bitmap getThumbnail(String key) {
        if (key == null) return null;
        return mCache.get(key);
    }
}
