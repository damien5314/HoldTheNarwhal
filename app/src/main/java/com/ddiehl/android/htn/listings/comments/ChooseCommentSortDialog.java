package com.ddiehl.android.htn.listings.comments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.ddiehl.android.htn.R;

public class ChooseCommentSortDialog extends DialogFragment {

    public static final String TAG = ChooseCommentSortDialog.class.getSimpleName();

    public static final String EXTRA_SORT = "com.ddiehl.android.htn.EXTRA_SORT";

    private static final String ARG_SETTING = "ARG_SETTING";

    public static ChooseCommentSortDialog newInstance(String currentSetting) {
        Bundle args = new Bundle();
        args.putString(ARG_SETTING, currentSetting);
        ChooseCommentSortDialog dialog = new ChooseCommentSortDialog();
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int selectedItem = -1;

        // Get selected item if it was passed
        Bundle args = getArguments();
        String currentSetting = args.getString(ARG_SETTING);
        if (currentSetting != null) {
            String[] settings = getResources().getStringArray(R.array.comment_sort_option_values);
            for (int i = 0; i < settings.length; i++) {
                if (settings[i].equals(currentSetting))
                    selectedItem = i;
            }
        }

        // Build dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.menu_sort_title)
                .setSingleChoiceItems(R.array.comment_sort_options, selectedItem, (dialog, which) -> {
                    String selectedChoice = getResources().getStringArray(R.array.comment_sort_option_values)[which];
                    Intent data = new Intent();
                    data.putExtra(EXTRA_SORT, selectedChoice);
                    getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, data);
                    dismiss();
                });
        return builder.create();
    }
}