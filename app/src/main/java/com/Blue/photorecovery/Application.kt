package com.Blue.photorecovery

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.multidex.MultiDexApplication

class Application : MultiDexApplication(), DefaultLifecycleObserver {

    override fun onCreate() {
        super<MultiDexApplication>.onCreate()

    }

    companion object {
        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

}