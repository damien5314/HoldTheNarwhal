package com.mopub.network;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.mopub.common.event.BaseEvent;
import com.mopub.common.logging.MoPubLog;
import com.mopub.volley.DefaultRetryPolicy;
import com.mopub.volley.NetworkResponse;
import com.mopub.volley.Request;
import com.mopub.volley.RequestQueue;
import com.mopub.volley.Response;
import com.mopub.volley.VolleyError;
import com.mopub.volley.toolbox.HttpHeaderParser;

import java.util.Arrays;

public class TrackingRequest extends Request<Void> {

    public interface Listener extends Response.ErrorListener {
        public void onResponse(@NonNull String url);
    }

    @Nullable private final TrackingRequest.Listener mListener;

    private TrackingRequest(@NonNull final String url, @Nullable final Listener listener) {
        super(Method.GET, url, listener);
        mListener = listener;
        setShouldCache(false);
        setRetryPolicy(new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    @Override
    protected Response<Void> parseNetworkResponse(final NetworkResponse networkResponse) {
        if (networkResponse.statusCode != 200) {
            return Response.error(
                    new MoPubNetworkError("Failed to log tracking request. Response code: "
                            + networkResponse.statusCode + " for url: " + getUrl(),
                            MoPubNetworkError.Reason.TRACKING_FAILURE));
        }
        return Response.success(null, HttpHeaderParser.parseCacheHeaders(networkResponse));
    }

    @Override
    public void deliverResponse(final Void aVoid) {
        if (mListener != null) {
            mListener.onResponse(getUrl());
        }
    }

    ///////////////////////////////////////////////////////////////
    // Static helper methods that can be used as utilities:
    //////////////////////////////////////////////////////////////

    public static void makeTrackingHttpRequest(final Iterable<String> urls, final Context context) {
        makeTrackingHttpRequest(urls, context, null, null);
    }

    public static void makeTrackingHttpRequest(final Iterable<String> urls,
            final Context context,
            final BaseEvent.Name name) {
        makeTrackingHttpRequest(urls, context, null, name);
    }

    public static void makeTrackingHttpRequest(final Iterable<String> urls,
            final Context context,
            @Nullable final Listener listener,
            final BaseEvent.Name name) {
        if (urls == null || context == null) {
            return;
        }

        final RequestQueue requestQueue = Networking.getRequestQueue(context);
        for (final String url : urls) {
            if (TextUtils.isEmpty(url)) {
                continue;
            }

            final TrackingRequest.Listener internalListener = new TrackingRequest.Listener() {
                @Override
                public void onResponse(@NonNull String url) {
                    MoPubLog.d("Successfully hit tracking endpoint: " + url);
                    if (listener != null) {
                        listener.onResponse(url);
                    }
                }

                @Override
                public void onErrorResponse(final VolleyError volleyError) {
                    MoPubLog.d("Failed to hit tracking endpoint: " + url);
                    if (listener != null) {
                        listener.onErrorResponse(volleyError);
                    }
                }
            };
            final TrackingRequest trackingRequest = new TrackingRequest(url, internalListener);
            requestQueue.add(trackingRequest);
        }
    }

    public static void makeTrackingHttpRequest(final String url,
            final Context context) {
        makeTrackingHttpRequest(url, context, null, null);
    }

    public static void makeTrackingHttpRequest(final String url,
            final Context context, @Nullable Listener listener) {
        makeTrackingHttpRequest(url, context, listener, null);
    }

    public static void makeTrackingHttpRequest(final String url,
            final Context context, final BaseEvent.Name name) {
        makeTrackingHttpRequest(url, context, null, name);
    }

    public static void makeTrackingHttpRequest(final String url,
            final Context context,
            @Nullable Listener listener,
            final BaseEvent.Name name) {
        if (url != null) {
            makeTrackingHttpRequest(Arrays.asList(url), context, listener, name);
        }
    }
}
