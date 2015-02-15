package com.ddiehl.android.simpleredditreader.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.ddiehl.android.simpleredditreader.R;

public class ListingsActivity extends NavigationDrawerActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null) {
            Fragment defaultFragment = ListingsFragment.newInstance(null);
            displayFragment(defaultFragment);
        }
    }
}
