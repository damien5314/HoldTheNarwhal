package com.ddiehl.android.htn.subscriptions;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.navigation.SubredditEditText;

import org.jetbrains.annotations.NotNull;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;


public class SubredditSearchDialog extends DialogFragment {

    public static final String TAG = SubredditSearchDialog.class.getSimpleName();

    public static final String RESULT_SEARCH = "RESULT_SEARCH";

    @NotNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getContext())
                .setTitle(R.string.subreddit_search_dialog_title)
                .setView(R.layout.subscriptions_subreddit_search_dialog)
                .setPositiveButton(R.string.subreddit_search_dialog_confirm, (dialog, which) -> onConfirm())
                .create();
    }

    void onConfirm() {
        SubredditEditText editText = getDialog().findViewById(R.id.search_input_field);
        String input = editText.getInput();

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
