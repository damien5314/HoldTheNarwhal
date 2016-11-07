package com.ddiehl.android.htn.view.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.ddiehl.android.htn.R;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class ChooseLinkSortDialog extends DialogFragment {

    public static final String TAG = ChooseLinkSortDialog.class.getSimpleName();

    public static final String EXTRA_SORT = "com.ddiehl.android.htn.EXTRA_SORT";

    private static final String ARG_SETTING = "ARG_SETTING";

    public static ChooseLinkSortDialog newInstance(String currentSetting) {
        Bundle args = new Bundle();
        args.putString(ARG_SETTING, currentSetting);
        ChooseLinkSortDialog dialog = new ChooseLinkSortDialog();
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
            String[] settings = getResources().getStringArray(R.array.link_sort_option_values);
            for (int i = 0; i < settings.length; i++) {
                if (settings[i].equals(currentSetting))
                    selectedItem = i;
            }
        }

        // Build dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.menu_sort_title)
                .setSingleChoiceItems(R.array.link_sort_options, selectedItem, (dialog, which) -> {
                    String sort = getResources().getStringArray(R.array.link_sort_option_values)[which];
                    finish(sort);
                });
        return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        finish(RESULT_CANCELED, null);
    }

    private void finish(@NonNull String sort) {
        Intent data = new Intent();
        data.putExtra(EXTRA_SORT, sort);
        finish(RESULT_OK, data);
    }

    private void finish(int resultCode, Intent data) {
        if (getTargetFragment() != null) {
            getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, data);
            dismiss();
        } else {
            getActivity().setResult(resultCode);
            getActivity().finish();
        }
    }
}
