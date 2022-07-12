package com.ddiehl.android.htn.routing

import androidx.fragment.app.FragmentActivity
import com.ddiehl.android.htn.settings.SettingsActivity
import javax.inject.Inject

/**
 * Router that links to just about everything.
 *
 * TODO: Break this into fragment-specific routers so we can modularize later.
 */
class AppRouter @Inject constructor(
    private val activity: FragmentActivity
) {

    fun showSettings() {
        val intent = SettingsActivity.getIntent(activity)
        activity.startActivity(intent)
    }
}
