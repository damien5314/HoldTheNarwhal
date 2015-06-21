package com.mopub.mobileads;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.mopub.common.UrlHandler;
import com.mopub.common.UrlAction;
import com.mopub.common.logging.MoPubLog;
import com.mopub.common.util.Intents;
import com.mopub.exceptions.IntentNotResolvableException;

import static com.mopub.mobileads.MoPubErrorCode.UNSPECIFIED;

class HtmlWebViewClient extends WebViewClient {
    static final String MOPUB_FINISH_LOAD = "mopub://finishLoad";
    static final String MOPUB_FAIL_LOAD = "mopub://failLoad";

    private final Context mContext;
    private HtmlWebViewListener mHtmlWebViewListener;
    private BaseHtmlWebView mHtmlWebView;
    private final String mClickthroughUrl;
    private final String mRedirectUrl;

    HtmlWebViewClient(HtmlWebViewListener htmlWebViewListener, BaseHtmlWebView htmlWebView, String clickthrough, String redirect) {
        mHtmlWebViewListener = htmlWebViewListener;
        mHtmlWebView = htmlWebView;
        mClickthroughUrl = clickthrough;
        mRedirectUrl = redirect;
        mContext = htmlWebView.getContext();
    }

    @Override
    public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
        new UrlHandler.Builder()
                .withSupportedUrlActions(
                        UrlAction.HANDLE_MOPUB_SCHEME,
                        UrlAction.IGNORE_ABOUT_SCHEME,
                        UrlAction.HANDLE_PHONE_SCHEME,
                        UrlAction.OPEN_APP_MARKET,
                        UrlAction.OPEN_NATIVE_BROWSER,
                        UrlAction.OPEN_IN_APP_BROWSER,
                        UrlAction.HANDLE_SHARE_TWEET,
                        UrlAction.FOLLOW_DEEP_LINK)
                .withResultActions(new UrlHandler.ResultActions() {
                    @Override
                    public void urlHandlingSucceeded(@NonNull String url,
                            @NonNull UrlAction urlAction) {
                        if (mHtmlWebView.wasClicked()) {
                            mHtmlWebViewListener.onClicked();
                            mHtmlWebView.onResetUserClick();
                        }
                    }

                    @Override
                    public void urlHandlingFailed(@NonNull String url,
                            @NonNull UrlAction lastFailedUrlAction) {
                    }
                })
                .withMoPubSchemeListener(new UrlHandler.MoPubSchemeListener() {
                    @Override
                    public void onFinishLoad() {
                        mHtmlWebViewListener.onLoaded(mHtmlWebView);
                    }

                    @Override
                    public void onClose() {
                        mHtmlWebViewListener.onCollapsed();
                    }

                    @Override
                    public void onFailLoad() {
                        mHtmlWebViewListener.onFailed(UNSPECIFIED);
                    }
                })
                .build().handleUrl(mContext, url, mHtmlWebView.wasClicked());
        return true;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        // If the URL being loaded shares the redirectUrl prefix, open it in the browser.
        if (mRedirectUrl != null && url.startsWith(mRedirectUrl)) {
            view.stopLoading();
            if (mHtmlWebView.wasClicked()) {
                try {
                    Intents.showMoPubBrowserForUrl(mContext, Uri.parse(url));
                } catch (IntentNotResolvableException e) {
                    MoPubLog.d(e.getMessage());
                }
            } else {
                MoPubLog.d("Attempted to redirect without user interaction");
            }
        }
    }

}
