package com.ddiehl.android.htn.navigation;

import android.net.Uri;
import android.os.Bundle;

import com.ddiehl.android.htn.listings.comments.LinkCommentsRouter;
import com.ddiehl.android.htn.routing.AppRouter;
import com.ddiehl.android.htn.view.BaseActivity;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

public class DeepLinkDispatcher extends BaseActivity {

    @Inject
    AppRouter appRouter;
    @Inject
    LinkCommentsRouter linkCommentsRouter;

    @Override
    protected boolean hasNavigationDrawer() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri deepLink = getIntent().getData();
        processDeepLink(deepLink);
    }

    // TODO Analytics
    private void processDeepLink(@NotNull Uri uri) {
        Timber.i("Deep link: %s", uri.toString());
        List<String> segments = uri.getPathSegments();
        if (segments.size() == 0) {
            // Front page
            appRouter.showSubreddit(null, null, null);
        } else if (isSubredditSort(segments.get(0))) {
            // Sorted front page
            appRouter.showSubreddit(null, segments.get(0), null);
        } else if (segments.get(0).equals("r")) {
            // Subreddit navigation
            String subreddit = segments.get(1);
            // Check for more metadata
            if (segments.size() > 2) {
                if (segments.get(2).equals("comments")) {
                    // Navigating to comment thread
                    if (segments.size() > 5) {
                        // Link to specific comment
                        linkCommentsRouter.showCommentsForLink(subreddit, segments.get(3), segments.get(5));
                    } else {
                        // Link to full thread
                        linkCommentsRouter.showCommentsForLink(subreddit, segments.get(3), null);
                    }
                } else if (isSubredditSort(segments.get(2))) {
                    // Subreddit sorted
                    appRouter.showSubreddit(subreddit, segments.get(2), null);
                }
            } else {
                // Subreddit default sort
                appRouter.showSubreddit(subreddit, null, null);
            }
        } else if (segments.get(0).equals("u") || segments.get(0).equals("user")) {
            // User profile navigation
            if (segments.size() > 2) {
                // Profile view specified
                if (segments.size() > 3) {
                    // Profile view with sort
                    // FIXME This actually should be read from a query string
                    appRouter.showUserProfile(segments.get(1), segments.get(2), segments.get(3));
                } else {
                    // Profile view default sort
                    appRouter.showUserProfile(segments.get(1), segments.get(2), null);
                }
            } else {
                appRouter.showUserProfile(segments.get(1), null, null);
            }
        } else {
            Timber.w("Deep link fell through without redirection: %s", uri.toString());
            appRouter.showFrontPage(null, null);
        }

        // Clear DeepLinkDispatcher from the activity task stack
        finish();
    }

    private boolean isSubredditSort(String s) {
        return s.equals("hot")
                || s.equals("new")
                || s.equals("rising")
                || s.equals("controversial")
                || s.equals("top")
                || s.equals("gilded");
    }
}
