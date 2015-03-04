package com.ddiehl.android.simpleredditreader.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.ZoomButtonsController;

import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.RedditAuthorization;
import com.ddiehl.android.simpleredditreader.events.AuthorizeUserEvent;
import com.ddiehl.android.simpleredditreader.events.BusProvider;

/**
 * Created by Damien on 2/6/2015.
 */
public class WebViewFragment extends Fragment {
    public static final String TAG = WebViewFragment.class.getSimpleName();

    private String mUrl;
    private WebView mWebView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mUrl = getActivity().getIntent().getData().toString();
        getActivity().setTitle(R.string.app_name);
    }

    @Override @SuppressWarnings("SetJavaScriptEnabled")
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.web_view_fragment, container, false);

        final ProgressBar progressBar = (ProgressBar) v.findViewById(R.id.progress_bar);
        progressBar.setMax(100);

        mWebView = (WebView) v.findViewById(R.id.web_view);

        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setBuiltInZoomControls(true);
        disableWebViewZoomControls(mWebView);

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains(RedditAuthorization.REDIRECT_URI)) {
                    RedditAuthorization.getInstance(getActivity()).saveAuthorizationCode(url);
                    BusProvider.getInstance().post(new AuthorizeUserEvent());
                    getActivity().finish();
                }
                return false;
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

            @Override
            public void onReceivedTitle(WebView view, String title) {
                getActivity().setTitle(title);
            }
        });

        mWebView.loadUrl(mUrl);
        mWebView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // Check if the key event was the Back button and if there's history
                if (event.getAction() == KeyEvent.ACTION_UP
                        && (keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
                    mWebView.goBack();
                    return true;
                }
                return false;
            }
        });

        return v;
    }

    @Override
    public void onDestroyView() {
        mWebView.destroy();
        super.onDestroyView();
    }

    /**
     * Disable zoom buttons for WebView.
     * http://twigstechtips.blogspot.com/2013/09/android-disable-webview-zoom-controls.html
     */
    public void disableWebViewZoomControls(final WebView webView) {
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);

        // Use the API 11+ calls to disable the controls
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            new Runnable() {
                @SuppressLint("NewApi")
                public void run() {
                    webView.getSettings().setDisplayZoomControls(false);
                }
            }.run();
        } else {
            try {
                ZoomButtonsController zoom_control;
                zoom_control = ((ZoomButtonsController) webView.getClass()
                        .getMethod("getZoomButtonsController").invoke(webView, (Object[]) null));
                zoom_control.getContainer().setVisibility(View.GONE);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
