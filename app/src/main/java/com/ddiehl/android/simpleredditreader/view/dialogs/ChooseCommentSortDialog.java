package com.ddiehl.android.simpleredditreader.view.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.ddiehl.android.simpleredditreader.R;

public class ChooseCommentSortDialog extends DialogFragment {
    private static final String TAG = ChooseCommentSortDialog.class.getSimpleName();

    private static final String ARG_SETTING = "setting";
    public static final String EXTRA_SORT = "com.ddiehl.android.simpleredditreader.extra_sort";

    private String currentSetting;

    public ChooseCommentSortDialog() { }

    public static ChooseCommentSortDialog newInstance(String currentSetting) {
        Bundle args = new Bundle();
        args.putString(ARG_SETTING, currentSetting);
        ChooseCommentSortDialog dialog = new ChooseCommentSortDialog();
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int selectedItem = -1;

        // Get selected item if it was passed
        Bundle args = getArguments();
        currentSetting = args.getString(ARG_SETTING);
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
                .setSingleChoiceItems(R.array.comment_sort_options, selectedItem, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String selectedChoice = getResources().getStringArray(R.array.comment_sort_option_values)[which];
                        Intent data = new Intent();
                        data.putExtra(EXTRA_SORT, selectedChoice);
                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, data);
                        dismiss();
                    }
                });
        return builder.create();
    }
}
