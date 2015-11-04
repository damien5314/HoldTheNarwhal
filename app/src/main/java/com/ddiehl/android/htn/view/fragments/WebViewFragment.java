package com.ddiehl.android.htn.view.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.MailTo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.ZoomButtonsController;

import com.ddiehl.android.htn.BuildConfig;
import com.ddiehl.android.htn.BusProvider;
import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.events.responses.UserAuthCodeReceivedEvent;
import com.ddiehl.android.htn.io.RedditServiceAuth;
import com.ddiehl.android.htn.logging.Logger;
import com.ddiehl.android.htn.utils.AuthUtils;
import com.ddiehl.android.htn.utils.BaseUtils;
import com.squareup.otto.Bus;

import butterknife.Bind;
import butterknife.ButterKnife;


public class WebViewFragment extends Fragment {
    private static final String ARG_URL = "url";

    private Logger mLogger = HoldTheNarwhal.getLogger();
    private Bus mBus = BusProvider.getInstance();
    private String mUrl;
    @Bind(R.id.web_view) WebView mWebView;

    static {
        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
    }

    public WebViewFragment() { }

    public static Fragment newInstance(String url) {
        Bundle args = new Bundle();
        args.putString(ARG_URL, url);
        Fragment fragment = new WebViewFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        Bundle args = getArguments();

        mUrl = args.getString(ARG_URL);
        getActivity().setTitle(R.string.app_name);
    }

    @Override @SuppressWarnings("SetJavaScriptEnabled")
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.web_view_fragment, container, false);

        final ProgressBar progressBar = ButterKnife.findById(v, R.id.progress_bar);
        progressBar.setMax(100);

        ButterKnife.bind(this, v);

        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setDomStorageEnabled(true);
        disableWebViewZoomControls(mWebView);

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                mLogger.d("Loading URL: " + url);

                if (url.contains(RedditServiceAuth.REDIRECT_URI)
                        && !url.equals(RedditServiceAuth.AUTHORIZATION_URL)) {
                    // Pass auth code back to the Activity, which will pop this fragment
                    String authCode = AuthUtils.getUserAuthCodeFromRedirectUri(url);
                    mBus.post(new UserAuthCodeReceivedEvent(authCode));
                    getFragmentManager().popBackStack();
                    return true; // Can we do this to prevent the page from loading at all?
                }

                if (url.startsWith("mailto:")) {
                    MailTo mt = MailTo.parse(url);
                    Intent i = BaseUtils.getNewEmailIntent(
                            mt.getTo(), mt.getSubject(), mt.getBody(), mt.getCc());
                    getActivity().startActivity(i);
                    return true;
                }

                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

                // 11 = Build.VERSION_CODES.HONEYCOMB (Android 3.0)
                // http://stackoverflow.com/questions/15518317/shouldoverrideurlloading-is-not-working
                if (Build.VERSION.SDK_INT < 11) {
                    if (shouldOverrideUrlLoading(view, url)) {
                        view.stopLoading();
                    }
                }
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                if (progress == 100) {
                    progressBar.setVisibility(View.INVISIBLE);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(progress);
                }
            }
        });

        mWebView.loadUrl(mUrl);
        mWebView.setOnKeyListener((v1, keyCode, event) -> {
            // Check if the key event was the Back button and if there's history
            if (event.getAction() == KeyEvent.ACTION_UP
                    && (keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
                mWebView.goBack();
                return true;
            }
            return false;
        });

        return v;
    }

    @Override
    public void onDestroyView() {
        if (mWebView != null) {
            ((ViewGroup) mWebView.getParent()).removeView(mWebView);
            mWebView.removeAllViews();
            mWebView.destroy();
        }
        super.onDestroyView();
    }

    /**
     * Disable zoom buttons for WebView.
     * http://stackoverflow.com/a/14751673/3238938
     * http://twigstechtips.blogspot.com/2013/09/android-disable-webview-zoom-controls.html
     */
    private static void disableWebViewZoomControls(final WebView webView) {
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);

        // Use the API 11+ calls to disable the controls
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            webView.getSettings().setDisplayZoomControls(false);
        } else {
            try {
                ZoomButtonsController zoom_control;
                zoom_control = ((ZoomButtonsController) webView.getClass()
                        .getMethod("getZoomButtonsController").invoke(webView, (Object[]) null));
                zoom_control.getContainer().setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.web_view, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                mWebView.reload();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
