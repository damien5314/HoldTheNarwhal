package com.ddiehl.android.htn.navigation;

import android.net.MailTo;
import android.os.Bundle;
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

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.view.BaseFragment;
import com.ddiehl.android.htn.view.MainView;
import com.hannesdorfmann.fragmentargs.FragmentArgs;
import com.hannesdorfmann.fragmentargs.annotation.Arg;
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import timber.log.Timber;

@FragmentWithArgs
public class WebViewFragment extends BaseFragment {

    public static final String TAG = WebViewFragment.class.getSimpleName();
    public static final String EXTRA_CALLBACK_URL = "EXTRA_CALLBACK_URL";

    @Arg
    String mUrl;

    private WebView webView;
    private ProgressBar progressBar;

    @Override
    protected int getLayoutResId() {
        return R.layout.web_view_fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HoldTheNarwhal.getApplicationComponent().inject(this);
        FragmentArgs.inject(this);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @NotNull
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        View view = super.onCreateView(inflater, container, state);

        webView = view.findViewById(R.id.web_view);
        progressBar = view.findViewById(R.id.progress_bar);

        // Configure settings
        WebSettings settings = webView.getSettings();
        configure(settings);

        webView.setWebViewClient(new Client(this));

        // Configure progress bar
        progressBar.setMax(100);
        webView.setWebChromeClient(getProgressBarChromeClient(progressBar));

        // Handle back key taps
        webView.setOnKeyListener(getBackKeyListener());

        // Load url
        webView.loadUrl(mUrl);
        Timber.i("Showing WebView for URL: %s", mUrl);

        return view;
    }

    @SuppressWarnings("SetJavaScriptEnabled")
    protected void configure(WebSettings settings) {
        settings.setJavaScriptEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setDomStorageEnabled(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
    }

    protected static class Client extends WebViewClient {

        private final MainView mainView;

        public Client(@NotNull MainView mainView) {
            this.mainView = mainView;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Timber.d("Loading URL: %s", url);

            if (url.startsWith("mailto:")) {
                MailTo mt = MailTo.parse(url);
                mainView.doSendEmail(mt);
                return true;
            }

            return false;
        }
    }

    protected WebChromeClient getProgressBarChromeClient(final ProgressBar progressBar) {
        return new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                if (progress == 100) {
                    progressBar.setVisibility(View.INVISIBLE);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(progress);
                }
            }
        };
    }

    protected View.OnKeyListener getBackKeyListener() {
        return (v1, keyCode, event) -> {
            // Check if the key event was the Back button and if there's history
            if (event.getAction() == KeyEvent.ACTION_UP
                    && (keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
                webView.goBack();
                return true;
            }
            return false;
        };
    }

    @Override
    public void onDestroyView() {
        if (webView != null) {
            ((ViewGroup) webView.getParent()).removeView(webView);
            webView.removeAllViews();
            webView.destroy();
        }
        super.onDestroyView();
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
                webView.reload();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @NotNull
    @Override
    protected View getChromeView() {
        return webView;
    }
}
