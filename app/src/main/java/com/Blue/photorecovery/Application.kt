package com.Blue.photorecovery

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.multidex.MultiDexApplication
import com.Blue.photorecovery.common.UserDataManager

class Application : MultiDexApplication(), DefaultLifecycleObserver {

    override fun onCreate() {
        super<MultiDexApplication>.onCreate()

        UserDataManager.instance!!.initSharedPreferencesManager(applicationContext)

    }

    companion object {
        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

}