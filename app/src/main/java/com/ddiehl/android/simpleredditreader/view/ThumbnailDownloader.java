package com.ddiehl.android.simpleredditreader.view;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.ddiehl.android.simpleredditreader.redditapi.RedditFetcher;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ThumbnailDownloader<Token> extends HandlerThread {
    private static final String TAG = ThumbnailDownloader.class.getSimpleName();
    private static final int MSG_DOWNLOAD = 0;

    private Handler mHandler;
    private Map<Token, String> mRequestMap = Collections.synchronizedMap(new HashMap<Token, String>());
    private Handler mResponseHandler;
    Listener<Token> mListener;

    public interface Listener<Token> {
        void onThumbnailDownloaded(Token token, String url, Bitmap thumbnail);
    }

    public void setListener(Listener<Token> listener) {
        mListener = listener;
    }

    public ThumbnailDownloader(Handler responseHandler) {
        super(TAG);
        mResponseHandler = responseHandler;
    }

    @SuppressLint("HandlerLeak") @Override
    protected void onLooperPrepared() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MSG_DOWNLOAD) {
                    @SuppressWarnings("unchecked")
                    Token token = (Token) msg.obj;
                    handleRequest(token);
                }
            }
        };
    }

    public void queueThumbnail(Token token, String url) {
        mRequestMap.put(token, url);
        mHandler.obtainMessage(MSG_DOWNLOAD, token)
                .sendToTarget();
    }

    private void handleRequest(final Token token) {
        try {
            final String url = mRequestMap.get(token);
            if (url == null) return;

            byte[] bitmapBytes = new RedditFetcher().getUrlBytes(url);
            final Bitmap bitmap = BitmapFactory
                    .decodeByteArray(bitmapBytes, 0, bitmapBytes.length);

            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mRequestMap.get(token) != url)
                        return;

                    mRequestMap.remove(token);
                    mListener.onThumbnailDownloaded(token, url, bitmap);
                }
            });
        } catch (IOException e) {
            Log.e(TAG, "Error downloading image", e);
        }
    }

    public void clearQueue() {
        mHandler.removeMessages(MSG_DOWNLOAD);
        mRequestMap.clear();
    }
}
