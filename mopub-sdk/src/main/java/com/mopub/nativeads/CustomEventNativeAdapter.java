package com.mopub.nativeads;

import android.content.Context;
import android.support.annotation.NonNull;

import com.mopub.common.DataKeys;
import com.mopub.common.logging.MoPubLog;
import com.mopub.nativeads.factories.CustomEventNativeFactory;
import com.mopub.network.AdResponse;

import java.util.Map;

final class CustomEventNativeAdapter {
    private CustomEventNativeAdapter() {}

    public static void loadNativeAd(@NonNull final Context context,
            @NonNull final Map<String, Object> localExtras,
            @NonNull final AdResponse adResponse,
            @NonNull final CustomEventNative.CustomEventNativeListener customEventNativeListener) {

        final CustomEventNative customEventNative;
        String customEventNativeClassName = adResponse.getCustomEventClassName();
        try {
            customEventNative = CustomEventNativeFactory.create(customEventNativeClassName);
        } catch (Exception e) {
            MoPubLog.w("Failed to load Custom Event Native class: " + customEventNativeClassName);
            customEventNativeListener.onNativeAdFailed(NativeErrorCode.NATIVE_ADAPTER_NOT_FOUND);
            return;
        }

        if (adResponse.hasJson()) {
            localExtras.put(DataKeys.JSON_BODY_KEY, adResponse.getJsonBody());
        }

        customEventNative.loadNativeAd(
                context,
                customEventNativeListener,
                localExtras,
                adResponse.getServerExtras()
        );
    }
}
