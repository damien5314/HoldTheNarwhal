package com.ddiehl.android.htn.view;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;

import com.ddiehl.android.htn.R;


public abstract class TransparentBaseActivity extends AppCompatActivity {

    ProgressDialog loadingOverlay;

    public void showSpinner() {
        if (loadingOverlay == null) {
            loadingOverlay = new ProgressDialog(this, R.style.ProgressDialog);
            loadingOverlay.setCancelable(false);
            loadingOverlay.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        loadingOverlay.show();
    }

    public void dismissSpinner() {
        if (loadingOverlay != null && loadingOverlay.isShowing()) {
            loadingOverlay.dismiss();
        }
    }
}
