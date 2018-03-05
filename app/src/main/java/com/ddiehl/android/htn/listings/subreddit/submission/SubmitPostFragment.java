package com.ddiehl.android.htn.listings.subreddit.submission;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.view.BaseFragment;
import com.hannesdorfmann.fragmentargs.FragmentArgs;
import com.hannesdorfmann.fragmentargs.annotation.Arg;
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rxreddit.model.SubmitPostResponse;

import static com.ddiehl.android.htn.utils.AndroidUtils.getTextInputLayout;

@FragmentWithArgs
public class SubmitPostFragment extends BaseFragment
        implements SubmitPostView {

    public static final String TAG = SubmitPostFragment.class.getSimpleName();

    public static final String EXTRA_SUBMIT_SUBREDDIT = "EXTRA_SUBMIT_SUBREDDIT";
    public static final String EXTRA_SUBMIT_ID = "EXTRA_SUBMIT_ID";
    public static final String EXTRA_SUBMIT_ERRORS = "EXTRA_SUBMIT_ERRORS";
    public static final int RESULT_SUBMIT_SUCCESS = Activity.RESULT_OK;
    public static final int RESULT_SUBMIT_ERROR = -10;

    @Arg String subreddit;

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.submission_type_tabs)
    TabLayout submissionTypeTabs;
    @BindView(R.id.submission_url)
    TextInputEditText urlEditText;
    @BindView(R.id.submission_title)
    TextInputEditText titleEditText;
    @BindView(R.id.submission_text)
    TextInputEditText textEditText;
    @BindView(R.id.send_replies_to_inbox)
    CheckBox sendRepliesToInboxCheckbox;
    @BindView(R.id.submission_submit)
    Button submitButton;

    SubmitPostPresenter presenter;

    @Override
    protected int getLayoutResId() {
        return R.layout.submission_fragment;
    }

    @NotNull @Override
    protected View getChromeView() {
        return coordinatorLayout;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HoldTheNarwhal.getApplicationComponent().inject(this);
        FragmentArgs.inject(this);
        presenter = new SubmitPostPresenter(this);
    }

    @NotNull
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        View view = super.onCreateView(inflater, container, state);
        ButterKnife.bind(this, view);

        submissionTypeTabs.addOnTabSelectedListener(getOnTabSelectedListener());
        TabLayout.Tab linkTab = addNewTab(submissionTypeTabs, R.string.submission_type_link, "link");
        TabLayout.Tab textTab = addNewTab(submissionTypeTabs, R.string.submission_type_text, "self");

        return view;
    }

    TabLayout.OnTabSelectedListener getOnTabSelectedListener() {
        return new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String tag = (String) tab.getTag();
                if ("link".equals(tag)) {
                    onLinkTabSelected();
                } else if ("self".equals(tag)) {
                    onTextTabSelected();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { /* no-op */ }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { /* no-op */ }
        };
    }

    TabLayout.Tab addNewTab(@NotNull TabLayout tabLayout, @StringRes int resId, @NotNull String tag) {
        TabLayout.Tab tab = tabLayout.newTab();
        tab.setText(resId);
        tab.setTag(tag);
        tabLayout.addTab(tab);
        return tab;
    }

    private void onLinkTabSelected() {
        getTextInputLayout(urlEditText).setVisibility(View.VISIBLE);
        getTextInputLayout(textEditText).setVisibility(View.GONE);
    }

    private void onTextTabSelected() {
        getTextInputLayout(urlEditText).setVisibility(View.GONE);
        getTextInputLayout(textEditText).setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.submission_submit)
    void onSubmitClicked() {
        int position = submissionTypeTabs.getSelectedTabPosition();
        TabLayout.Tab selected = submissionTypeTabs.getTabAt(position);

        if (selected == null) {
            throw new RuntimeException("Invalid selected tab position");
        }
        if ("link".equals(selected.getTag())) {
            presenter.submit("link");
        } else {
            presenter.submit("self");
        }
    }

    @Override
    public String getSubreddit() {
        return subreddit;
    }

    @Override
    public String getTitle() {
        return titleEditText.getText().toString();
    }

    @Override
    public String getUrl() {
        return urlEditText.getText().toString();
    }

    @Override
    public String getText() {
        return textEditText.getText().toString();
    }

    @Override
    public boolean getSendReplies() {
        return sendRepliesToInboxCheckbox.isChecked();
    }

    @Override
    public void onPostSubmitted(SubmitPostResponse result) {
        Intent intent = new Intent();
        // Success
        if (result.getErrors().size() == 0) {
            intent.putExtra(EXTRA_SUBMIT_SUBREDDIT, subreddit);
            intent.putExtra(EXTRA_SUBMIT_ID, result.getId());
            finish(RESULT_SUBMIT_SUCCESS, intent);
        }
        // Errors present
        else {
            for (String error : result.getErrors()) {
                Crashlytics.logException(new RuntimeException(error));
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onSubmitError(Throwable error) {
        Snackbar.make(getChromeView(), R.string.submission_submit_error, Snackbar.LENGTH_LONG);
    }

    @Override
    public void dismissAfterCancel() {
        getActivity().setResult(Activity.RESULT_CANCELED);
        getActivity().finish();
    }
}
