package com.ddiehl.android.htn.managers

import android.content.Context
import android.net.ConnectivityManager

class NetworkConnectivityManager(context: Context) {

    private val appContext = context.applicationContext

    fun isConnectedToNetwork(): Boolean {
        val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = cm.activeNetworkInfo
        return info != null && info.isConnectedOrConnecting
    }
}
