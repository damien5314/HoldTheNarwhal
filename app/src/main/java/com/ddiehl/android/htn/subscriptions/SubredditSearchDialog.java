package com.ddiehl.android.htn.subscriptions;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.ddiehl.android.htn.R;


public class SubredditSearchDialog extends DialogFragment {

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getContext())
                .setTitle(R.string.subreddit_search_dialog_title)
                .setView(R.layout.subscriptions_subreddit_search_dialog)
                .setPositiveButton(
                        R.string.subreddit_search_dialog_confirm,
                        (dialog, which) -> {
                            onConfirm();
                })
                .create();
    }

    void onConfirm() {

    }
}
