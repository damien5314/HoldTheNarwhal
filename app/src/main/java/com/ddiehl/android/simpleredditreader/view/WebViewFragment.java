package com.ddiehl.android.simpleredditreader.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ddiehl.android.simpleredditreader.R;

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
        View v = inflater.inflate(R.layout.fragment_web_view, container, false);

        final ProgressBar progressBar = (ProgressBar) v.findViewById(R.id.progress_bar);
        progressBar.setMax(100);
        final TextView titleTextView = (TextView) v.findViewById(R.id.title_text_view);

        mWebView = (WebView) v.findViewById(R.id.web_view);

        mWebView.getSettings().setJavaScriptEnabled(true);

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
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
                titleTextView.setText(title);
            }
        });

        mWebView.loadUrl(mUrl);

        return v;
    }
}
