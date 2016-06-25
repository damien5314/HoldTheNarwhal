package com.ddiehl.android.htn.view.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.ddiehl.android.htn.R;

public class ChooseTimespanDialog extends DialogFragment {
  private static final String ARG_SETTING = "arg_setting";
  public static final String EXTRA_TIMESPAN = "com.ddiehl.android.htn.extra_timespan";

  public ChooseTimespanDialog() { }

  public static ChooseTimespanDialog newInstance(String currentSetting) {
    Bundle args = new Bundle();
    args.putString(ARG_SETTING, currentSetting);
    ChooseTimespanDialog dialog = new ChooseTimespanDialog();
    dialog.setArguments(args);
    return dialog;
  }

  @NonNull @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    int selectedItem = -1;

    // Get selected item if it was passed
    Bundle args = getArguments();
    String currentSetting = args.getString(ARG_SETTING);
    if (currentSetting != null) {
      String[] settings = getResources().getStringArray(R.array.timespan_option_values);
      for (int i = 0; i < settings.length; i++) {
        if (settings[i].equals(currentSetting))
          selectedItem = i;
      }
    }

    // Build dialog
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setTitle(R.string.menu_timespan_title)
        .setSingleChoiceItems(R.array.timespan_options, selectedItem, (dialog, which) -> {
          String selectedChoice = getResources().getStringArray(R.array.timespan_option_values)[which];
          Intent data = new Intent();
          data.putExtra(EXTRA_TIMESPAN, selectedChoice);
          getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, data);
          dismiss();
        });
    return builder.create();
  }

  @Override
  public void onCancel(DialogInterface dialog) {
    getTargetFragment().onActivityResult(
        getTargetRequestCode(), Activity.RESULT_CANCELED, new Intent());
  }
}
