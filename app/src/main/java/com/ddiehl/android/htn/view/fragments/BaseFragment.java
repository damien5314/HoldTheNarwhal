package com.ddiehl.android.htn.view.fragments;

import android.support.v4.app.Fragment;

import com.ddiehl.android.htn.view.dialogs.NsfwWarningDialog;

public abstract class BaseFragment extends Fragment {

  protected static final int REQUEST_CHOOSE_SORT = 1;
  protected static final int REQUEST_CHOOSE_TIMESPAN = 2;
  protected static final int REQUEST_NSFW_WARNING = 3;
  protected static final int REQUEST_ADD_COMMENT = 4;

  public void showNsfwWarningDialog() {
    NsfwWarningDialog dialog = new NsfwWarningDialog();
    dialog.setTargetFragment(this, REQUEST_NSFW_WARNING);
    dialog.show(getFragmentManager(), NsfwWarningDialog.TAG);
  }
}
