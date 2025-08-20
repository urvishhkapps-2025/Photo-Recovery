package com.Blue.photorecovery.common

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

class UserDataManager {

    private var userPreferences: SharedPreferences? = null
    private var userPreferencesEditor: SharedPreferences.Editor? = null
    private var context: Context? = null
    private val PRIVATE_MODE = 0

    private var gson: Gson? = null

    @SuppressLint("UseKtx")
    fun initSharedPreferencesManager(context: Context) {
        this.context = context
        userPreferences =
            this.context!!.getSharedPreferences(context.applicationInfo.packageName, PRIVATE_MODE)
        userPreferencesEditor = userPreferences?.edit()
        userPreferencesEditor?.apply()
    }

    private val gsonInstance: Gson?
        get() = if (gson == null) {
            gson =
                GsonBuilder()
                    .serializeSpecialFloatingPointValues()
                    .create()
            gson
        } else {
            gson
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

    fun setRecoverImages(imagesList: ArrayList<String>?) {
        val json: String = gsonInstance!!.toJson(imagesList)
        userPreferencesEditor!!.putString(SAVE_IMAGES, json)
        userPreferencesEditor!!.commit()
    }

    fun getRecoverImages(): ArrayList<String> {
        val json = userPreferences!!.getString(SAVE_IMAGES, "")
        var recoverImages: ArrayList<String> =
            ArrayList<String>()
        if (!TextUtils.isEmpty(json) && !json!!.isEmpty() && json != "null") {
            recoverImages = gsonInstance!!.fromJson(
                json,
                object :
                    TypeToken<ArrayList<String>?>() {}.type
            )
        }
        return recoverImages
    }

    companion object {
        const val IS_FIRST = "is_first"
        const val SAVE_IMAGES = "save_image"

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