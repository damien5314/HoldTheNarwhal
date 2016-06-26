package com.ddiehl.android.htn.view.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.ddiehl.android.htn.R;

public class AnalyticsDialog extends DialogFragment {

  private Callbacks mListener;

  public interface Callbacks {
    void onAnalyticsAccepted();
    void onAnalyticsDeclined();
  }

  @NonNull @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    return new AlertDialog.Builder(getActivity())
        .setTitle(R.string.dialog_analytics_title)
        .setMessage(R.string.dialog_analytics_message)
        .setNeutralButton(R.string.dialog_analytics_accept,
            (dialog, which) -> mListener.onAnalyticsAccepted())
        .setNegativeButton(R.string.dialog_analytics_decline,
            (dialog, which) -> mListener.onAnalyticsDeclined())
        .create();
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    try {
      mListener = (Callbacks) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(context.getClass().getSimpleName()
          + " must implement AnalyticsDialog.Callbacks");
    }
  }
}
