package com.ddiehl.android.htn.routing

import android.content.Intent
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.ddiehl.android.htn.view.BaseActivity
import rxreddit.android.SignInActivity
import rxreddit.api.RedditService
import javax.inject.Inject

class AuthRouter @Inject constructor(
    private val activity: FragmentActivity,
    private val redditService: RedditService,
) {

    fun showLoginView() {
        val intent = Intent(activity, SignInActivity::class.java)
        intent.putExtra(SignInActivity.EXTRA_AUTH_URL, redditService.authorizationUrl)
        ActivityCompat.startActivityForResult(activity, intent, BaseActivity.REQUEST_SIGN_IN, null)
    }
}
