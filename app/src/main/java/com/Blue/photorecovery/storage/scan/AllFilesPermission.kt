package com.Blue.photorecovery.storage.scan

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

/**
 * How "all files" is interpreted by API level:
 * - API 30+ (Android 11+): True only if MANAGE_EXTERNAL_STORAGE toggle is ON.
 * - API 29 (Android 10): No real "all files". You CANNOT get full access (scoped storage).
 *                        If your app targets 29 with requestLegacyExternalStorage=true,
 *                        you'll get broader legacy behavior. We return false here to be strict.
 * - API 24–28 (Android 7–9): True if READ + WRITE external storage are granted.
 */
object AllFilesPermission {

    fun hasAllFilesAccess(activity: Activity): Boolean = when {
        Build.VERSION.SDK_INT >= 30 -> Environment.isExternalStorageManager()
        Build.VERSION.SDK_INT in 24..28 -> has(
            activity,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) &&
                has(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        Build.VERSION.SDK_INT == 29 -> false // scoped storage; no true "all files"
        else -> false
    }

    /**
     * Request "all files" access:
     * - API 30+: opens the system settings switch for MANAGE_EXTERNAL_STORAGE.
     * - API 24–28: requests READ/WRITE runtime permissions (legacy wide access).
     * - API 29: there's no true "all files" — consider SAF; we request READ as best-effort.
     *
     * Pass in a launcher created with createStoragePermsLauncher(...) for runtime perms.
     */
    fun requestAllFilesAccess(
        activity: Activity,
        runtimePermsLauncher: ActivityResultLauncher<Array<String>>
    ) {
        when {
            Build.VERSION.SDK_INT >= 30 -> {
                if (!Environment.isExternalStorageManager()) {
                    try {
                        activity.startActivity(
                            Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION).apply {
                                data = Uri.parse("package:${activity.packageName}")
                            }
                        )
                    } catch (_: Exception) {
                        activity.startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
                    }
                }
            }

            Build.VERSION.SDK_INT in 24..28 -> {
                runtimePermsLauncher.launch(
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                )
            }

            Build.VERSION.SDK_INT == 29 -> {
                // No true "all files". Ask for READ so you can access media; use SAF for folders.
                runtimePermsLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
            }
        }
    }

    /** Create once (in Activity/Fragment) and reuse. */
    fun createStoragePermsLauncher(
        caller: ActivityResultCaller,
        onResult: (Map<String, Boolean>) -> Unit
    ): ActivityResultLauncher<Array<String>> =
        caller.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
            onResult
        )

    private fun has(activity: Activity, perm: String): Boolean =
        ContextCompat.checkSelfPermission(activity, perm) == PackageManager.PERMISSION_GRANTED
}
