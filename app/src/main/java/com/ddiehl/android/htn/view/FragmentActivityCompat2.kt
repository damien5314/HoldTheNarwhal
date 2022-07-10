package com.ddiehl.android.htn.view

import androidx.fragment.app.Fragment
import com.ddiehl.android.htn.R

abstract class FragmentActivityCompat2 : BaseDaggerActivity() {

    protected abstract val fragment: Fragment

    protected abstract val fragmentTag: String

    public override fun onStart() {
        super.onStart()
        if (supportFragmentManager.findFragmentByTag(fragmentTag) == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, fragment, fragmentTag)
                .commit()
        }
    }
}
