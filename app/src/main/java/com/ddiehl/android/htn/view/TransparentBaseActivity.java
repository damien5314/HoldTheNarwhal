package com.ddiehl.android.htn.view;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;

import com.ddiehl.android.htn.R;


public abstract class TransparentBaseActivity extends AppCompatActivity {

    ProgressDialog mLoadingOverlay;

    public void showSpinner() {
        if (mLoadingOverlay == null) {
            mLoadingOverlay = new ProgressDialog(this, R.style.ProgressDialog);
            mLoadingOverlay.setCancelable(false);
            mLoadingOverlay.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        mLoadingOverlay.show();
    }

    public void dismissSpinner() {
        if (mLoadingOverlay != null && mLoadingOverlay.isShowing()) {
            mLoadingOverlay.dismiss();
        }
    }
}
