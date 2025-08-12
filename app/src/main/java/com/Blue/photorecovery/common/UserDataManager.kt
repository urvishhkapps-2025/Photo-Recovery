package com.Blue.photorecovery.common

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

class UserDataManager {

    private var userPreferences: SharedPreferences? = null
    private var userPreferencesEditor: SharedPreferences.Editor? = null
    private var context: Context? = null
    private val PRIVATE_MODE = 0

    @SuppressLint("UseKtx")
    fun initSharedPreferencesManager(context: Context) {
        this.context = context
        userPreferences =
            this.context!!.getSharedPreferences(context.applicationInfo.packageName, PRIVATE_MODE)
        userPreferencesEditor = userPreferences?.edit()
        userPreferencesEditor?.apply()
    }

    fun setUserFirstTime(isFirstTime: Boolean) {
        userPreferencesEditor!!.putBoolean(
            IS_FIRST,
            isFirstTime
        )
        userPreferencesEditor!!.apply()
    }

    fun getUserFirstTime(): Boolean {
        return userPreferences!!.getBoolean(
            IS_FIRST,
            true
        )
    }

    companion object {
        const val IS_FIRST = "is_first"

        @SuppressLint("StaticFieldLeak")
        private var userDataManager: UserDataManager? = null

        @JvmStatic
        val instance: UserDataManager?
            get() {
                if (userDataManager == null) {
                    userDataManager = UserDataManager()
                }
                return userDataManager
            }

    }

}