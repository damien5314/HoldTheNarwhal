package com.ddiehl.android.htn.view

import android.app.Fragment
import com.ddiehl.android.htn.R

@Suppress("DEPRECATION") // no kidding
abstract class FragmentActivity : BaseDaggerActivity() {

    protected abstract val fragment: Fragment?

    protected abstract val fragmentTag: String?

    public override fun onStart() {
        super.onStart()
        if (fragmentManager.findFragmentByTag(fragmentTag) == null) {
            fragmentManager.beginTransaction()
                .add(R.id.fragment_container, fragment, fragmentTag)
                .commit()
        }
    }
}
