package com.ddiehl.android.simpleredditreader.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.ddiehl.android.simpleredditreader.R;

public class ChooseTimespanDialog extends DialogFragment {
    private static final String TAG = ChooseTimespanDialog.class.getSimpleName();

    public static final String EXTRA_TIMESPAN = "com.ddiehl.android.simpleredditreader.extra_timespan";

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.choose_timespan)
                .setItems(R.array.timespan_options, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String selectedChoice = getResources().getStringArray(R.array.timespan_options)[which];
                        Intent data = new Intent();
                        data.putExtra(EXTRA_TIMESPAN, selectedChoice);
                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, data);
                    }
                });
        return builder.create();
    }
}
