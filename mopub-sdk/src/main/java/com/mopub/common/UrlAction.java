package com.mopub.common;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.mopub.common.logging.MoPubLog;
import com.mopub.common.util.Intents;
import com.mopub.exceptions.IntentNotResolvableException;
import com.mopub.exceptions.UrlParseException;

import static com.mopub.common.Constants.HTTP;
import static com.mopub.common.Constants.HTTPS;
import static com.mopub.common.UrlHandler.MoPubSchemeListener;

/**
 * {@code UrlAction} describes the different kinds of actions for URLs that {@link UrlHandler} can
 * potentially perform and how to match against each URL.
 */
public enum UrlAction {
    /**
     * NOTE: The order in which these are defined determines the priority when matching URLs!
     * If a URL matches multiple Url Actions, it will be handled by the one that appears first in
     * this enum (see {@link UrlHandler#handleUrl(Context, String)}).
     *
     * Each UrlAction includes its ordinal in a comment as a reminder of this fact.
     */

    /* 0 */ HANDLE_MOPUB_SCHEME(false) {
        @Override
        public boolean shouldTryHandlingUrl(@NonNull final Uri uri) {
            return "mopub".equals(uri.getScheme());
        }

        @Override
        protected void performAction(@NonNull final Context context, @NonNull final Uri uri,
                final boolean skipShowMoPubBrowser,
                @Nullable final MoPubSchemeListener moPubSchemeListener)
                throws IntentNotResolvableException {
            final String host = uri.getHost();

            if ("finishLoad".equals(host)) {
                moPubSchemeListener.onFinishLoad();
            } else if ("close".equals(host)) {
                moPubSchemeListener.onClose();
            } else if ("failLoad".equals(host)) {
                moPubSchemeListener.onFailLoad();
            } else {
                throw new IntentNotResolvableException("Could not handle MoPub Scheme url: " + uri);
            }
        }
    },

    /* 1 */ IGNORE_ABOUT_SCHEME(false) {
        @Override
        public boolean shouldTryHandlingUrl(@NonNull final Uri uri) {
            return "about".equals(uri.getScheme());
        }

        @Override
        protected void performAction(@NonNull final Context context, @NonNull final Uri uri,
                final boolean skipShowMoPubBrowser,
                @Nullable final MoPubSchemeListener moPubSchemeListener)
                throws IntentNotResolvableException {
            MoPubLog.d("Link to about page ignored.");
        }
    },

    /* 2 */ HANDLE_PHONE_SCHEME(true) {
        @Override
        public boolean shouldTryHandlingUrl(@NonNull final Uri uri) {
            final String scheme = uri.getScheme();
            return "tel".equals(scheme) || "voicemail".equals(scheme)
                    || "sms".equals(scheme) || "mailto".equals(scheme)
                    || "geo".equals(scheme)
                    || "google.streetview".equals(scheme);
        }

        @Override
        protected void performAction(@NonNull final Context context, @NonNull final Uri uri,
                final boolean skipShowMoPubBrowser,
                @Nullable final MoPubSchemeListener moPubSchemeListener)
                throws IntentNotResolvableException {
            final String errorMessage = "Could not handle intent with URI: " + uri + "\n\tIs " +
                    "this intent supported on your phone?";
            Intents.launchActionViewIntent(context, uri, errorMessage);
        }
    },

    /* 3 */ OPEN_NATIVE_BROWSER(true) {
        @Override
        public boolean shouldTryHandlingUrl(@NonNull final Uri uri) {
            return "mopubnativebrowser".equals(uri.getScheme());
        }

        @Override
        protected void performAction(@NonNull final Context context, @NonNull final Uri uri,
                final boolean skipShowMoPubBrowser,
                @Nullable final MoPubSchemeListener moPubSchemeListener)
                throws IntentNotResolvableException {
            final String errorMessage = "Unable to load mopub native browser url: " + uri;
            try {
                final Intent intent = Intents.intentForNativeBrowserScheme(uri);
                Intents.launchIntentForUserClick(context, intent, errorMessage);
            } catch (UrlParseException e) {
                throw new IntentNotResolvableException(errorMessage + "\n\t" + e.getMessage());
            }
        }
    },

    /* 4 */ OPEN_APP_MARKET(true) {
        @Override
        public boolean shouldTryHandlingUrl(@NonNull final Uri uri) {
            final String scheme = uri.getScheme();
            final String host = uri.getHost();

            return "play.google.com".equals(host) || "market.android.com".equals(host)
                    || "market".equals(scheme)
                    || uri.toString().startsWith("play.google.com/")
                    || uri.toString().startsWith("market.android.com/");
        }

        @Override
        protected void performAction(@NonNull final Context context, @NonNull final Uri uri,
                final boolean skipShowMoPubBrowser,
                @Nullable final MoPubSchemeListener moPubSchemeListener)
                throws IntentNotResolvableException {
            Intents.launchApplicationUrl(context, uri);
        }
    },

    /* 5 */ OPEN_IN_APP_BROWSER(true) {
        @Override
        public boolean shouldTryHandlingUrl(@NonNull final Uri uri) {
            final String scheme = uri.getScheme();
            return (HTTP.equals(scheme) || HTTPS.equals(scheme));
        }

        @Override
        protected void performAction(@NonNull final Context context, @NonNull final Uri uri,
                final boolean skipShowMoPubBrowser,
                @Nullable final MoPubSchemeListener moPubSchemeListener)
                throws IntentNotResolvableException {
            if (!skipShowMoPubBrowser) {
                Intents.showMoPubBrowserForUrl(context, uri);
            }
        }
    },

    /**
     * This handles tweet sharing via the chooser dialog.
     * See {@link Intents#intentForShareTweet(Uri)} for more details.
     */
    /* 6 */ HANDLE_SHARE_TWEET(true) {
        @Override
        public boolean shouldTryHandlingUrl(@NonNull final Uri uri) {
            Preconditions.checkNotNull(uri);
            return "mopubshare".equals(uri.getScheme()) && "tweet".equals(uri.getHost());
        }

        @Override
        protected void performAction(@NonNull final Context context, @NonNull final Uri uri,
                final boolean skipShowMoPubBrowser,
                @Nullable final MoPubSchemeListener moPubSchemeListener)
                throws IntentNotResolvableException {
            Preconditions.checkNotNull(context);
            Preconditions.checkNotNull(uri);

            final String chooserText = "Share via";
            final String errorMessage = "Could not handle share tweet intent with URI " + uri;
            try {
                final Intent shareTweetIntent = Intents.intentForShareTweet(uri);
                final Intent chooserIntent = Intent.createChooser(shareTweetIntent, chooserText);
                Intents.launchIntentForUserClick(context, chooserIntent, errorMessage);
            } catch (UrlParseException e) {
                throw new IntentNotResolvableException(errorMessage + "\n\t" + e.getMessage());
            }
        }
    },

    /* 7 */ FOLLOW_DEEP_LINK(true) {
        @Override
        public boolean shouldTryHandlingUrl(@NonNull final Uri uri) {
            final String scheme = uri.getScheme();
            final String host = uri.getHost();
            return !TextUtils.isEmpty(scheme) && !TextUtils.isEmpty(host);
        }

        @Override
        protected void performAction(@NonNull final Context context, @NonNull final Uri uri,
                final boolean skipShowMoPubBrowser,
                @Nullable final MoPubSchemeListener moPubSchemeListener)
                throws IntentNotResolvableException {
            Intents.launchApplicationUrl(context, uri);
        }
    },

    /* This is essentially an "unspecified" value for UrlAction. */
    NOOP(false) {
        @Override
        public boolean shouldTryHandlingUrl(@NonNull final Uri uri) {
            return false;
        }

        @Override
        protected void performAction(@NonNull final Context context, @NonNull final Uri uri,
                final boolean skipShowMoPubBrowser,
                @Nullable final MoPubSchemeListener moPubSchemeListener)
                throws IntentNotResolvableException { }
    };

    public void handleUrl(
            @NonNull final Context context,
            @NonNull final Uri destinationUri,
            final boolean fromUserInteraction,
            final boolean skipShowMoPubBrowser,
            @Nullable final MoPubSchemeListener moPubSchemeListener)
            throws IntentNotResolvableException {
        MoPubLog.d("Ad event URL: " + destinationUri);
        if (mRequiresUserInteraction && !fromUserInteraction) {
            throw new IntentNotResolvableException("Attempted to handle action without user " +
                    "interaction.");
        } else {
            performAction(context, destinationUri, skipShowMoPubBrowser,
                    moPubSchemeListener);
        }
    }

    private final boolean mRequiresUserInteraction;

    UrlAction(boolean requiresUserInteraction) {
        mRequiresUserInteraction = requiresUserInteraction;
    }

    public abstract boolean shouldTryHandlingUrl(@NonNull final Uri uri);

    protected abstract void performAction(
            @NonNull final Context context,
            @NonNull final Uri uri,
            final boolean skipShowMoPubBrowser,
            @Nullable final MoPubSchemeListener moPubSchemeListener)
            throws IntentNotResolvableException;
}
