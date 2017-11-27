package com.ddiehl.android.htn.di;

import android.content.Context;

import com.ddiehl.android.htn.BuildConfig;
import com.ddiehl.android.htn.identity.IdentityManager;
import com.ddiehl.android.htn.identity.IdentityManagerImpl;
import com.ddiehl.android.htn.settings.SettingsManager;
import com.ddiehl.android.htn.settings.SettingsManagerImpl;
import com.ddiehl.android.htn.view.markdown.HtmlParser;
import com.ddiehl.android.htn.view.markdown.MarkdownParser;
import com.google.gson.Gson;

import org.jetbrains.annotations.Nullable;

import java.io.File;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import in.uncod.android.bypass.Bypass;
import rxreddit.android.AndroidAccessTokenManager;
import rxreddit.android.AndroidUtil;
import rxreddit.api.RedditService;
import rxreddit.util.RxRedditUtil;

@Module
public class ApplicationModule {

    @Singleton
    @Provides
    IdentityManager providesIdentityManager(Context context, SettingsManager settingsManager) {
        return new IdentityManagerImpl(context, settingsManager);
    }

    @Singleton
    @Provides
    SettingsManager providesSettingsManager(Context context, RedditService service) {
        return new SettingsManagerImpl(context, service);
    }

    @Singleton
    @Provides
    RedditService providesRedditService(Context context) {
//        return new RedditServiceMock();
        final int cacheSize = 10 * 1024 * 1024; // 10 MiB
        File path = new File(context.getCacheDir(), "htn-http-cache");
        final String userAgent = RxRedditUtil.getUserAgent(
                "android", "com.ddiehl.android.htn", BuildConfig.VERSION_NAME, "damien5314");
        return new RedditService.Builder()
                .appId(BuildConfig.REDDIT_APP_ID)
                .redirectUri(BuildConfig.REDDIT_REDIRECT_URI)
                .deviceId(AndroidUtil.getDeviceId(context))
                .userAgent(userAgent)
                .accessTokenManager(new AndroidAccessTokenManager(context))
                .cache(cacheSize, path)
                .loggingEnabled(BuildConfig.DEBUG)
                .build();
    }

    @Provides
    Gson providesGson(RedditService redditService) {
        return redditService.getGson();
    }

    @Provides @Nullable
    MarkdownParser providesMarkdownParser(@Nullable Bypass bypass) {
        return null; // Force logic to use HtmlParser
//        if (bypass == null) {
//            return null;
//        } else {
//            return new MarkdownParser(bypass);
//        }
    }

    @Provides
    HtmlParser providesHtmlProcessor(Context context) {
        return new HtmlParser(context);
    }
}
