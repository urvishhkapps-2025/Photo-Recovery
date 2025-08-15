package com.Blue.photorecovery.common

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import com.Blue.photorecovery.R
import java.util.Locale

object AppUtils {
    const val RATE_APP_LINK = "https://play.google.com/store/apps/details?id="

    fun shareText(
        activity: Activity,
        subject: String?,
        chooserTitle: String?,
    ) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject)

            shareIntent.putExtra(
                Intent.EXTRA_TEXT,
                ((String.format(
                    "I'm using *%1\$s!* app. Install Now",
                    activity.getString(R.string.app_name)
                ) + "\n\n"
                        + " " + RATE_APP_LINK).toString() + activity.packageName)
            )

            try {
                activity.startActivity(Intent.createChooser(shareIntent, chooserTitle))
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    activity,
                    "No application found to perform this action.",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

}