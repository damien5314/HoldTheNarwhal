package com.ddiehl.android.htn.subscriptions;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.widget.EditText;

import com.ddiehl.android.htn.R;

import butterknife.ButterKnife;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;


public class SubredditSearchDialog extends DialogFragment {

    public static final String TAG = SubredditSearchDialog.class.getSimpleName();

    public static final int REQUEST_SEARCH = 1001;
    public static final String RESULT_SEARCH = "RESULT_SEARCH";

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getContext())
                .setTitle(R.string.subreddit_search_dialog_title)
                .setView(R.layout.subscriptions_subreddit_search_dialog)
                .setPositiveButton(R.string.subreddit_search_dialog_confirm, (dialog, which) -> onConfirm())
                .create();
    }

    void onConfirm() {
        EditText editText = ButterKnife.findById(getDialog(), R.id.search_input_field);
        String input = editText.getText().toString();
        input = input.substring(3); // Trim the "/r/" portion of the input field

        if (TextUtils.isEmpty(input)) {
            onSearchCancelled();
        } else {
            onSearch(input);
        }
    }

    void onSearch(String input) {
        if (getTargetFragment() != null) {
            Intent result = new Intent();
            result.putExtra(RESULT_SEARCH, input);

            getTargetFragment().onActivityResult(getTargetRequestCode(), RESULT_OK, result);
        }
    }

    void onSearchCancelled() {
        if (getTargetFragment() != null) {
            getTargetFragment().onActivityResult(getTargetRequestCode(), RESULT_CANCELED, null);
        }
    }
}
