package com.tools.easy.scanner.support

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.ContactsContract
import android.provider.MediaStore
import com.tools.easy.scanner.App

/**
 *  description :
 */
object GpSupport {
    fun skip2Market(pkgName: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            data = Uri.parse(
                "https://play.google.com/store/apps/details?id=$pkgName"
            )
            if (hasGooglePlay()) {
                setPackage("com.android.vending")
            }
        }
        try {
            App.ins.startActivity(intent)
        } catch (e: Exception) {
            openUrlByBrowser("https://play.google.com/store/apps/details?id=$pkgName")
        }
    }

    private fun hasGooglePlay(): Boolean {
        return try {
            App.ins.packageManager.getApplicationInfo("com.android.vending", 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun openUrlByBrowser(url: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            App.ins.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun share(activity: Activity, title: String, content: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, content)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        activity.startActivity(
            Intent.createChooser(intent, title)
        )
    }

    fun shareBitmap(activity: Activity, title: String, content: String, bitmap: Bitmap) {
        val bitmapUri = Uri.parse(
            MediaStore.Images.Media.insertImage(
                activity.contentResolver,
                bitmap,
                null,
                null
            )
        )
        val intent = Intent(Intent(Intent.ACTION_SEND))
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_TEXT, content)
        intent.putExtra(Intent.EXTRA_STREAM, bitmapUri)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        activity.startActivity(
            Intent.createChooser(intent, title)
        )
    }

    fun sendEmail(context: Context, destEmail: String, subject: String, content: String) {
        val intent = Intent(Intent.ACTION_SENDTO);
        intent.data = Uri.parse("mailto:$destEmail")
        intent.putExtra(Intent.EXTRA_EMAIL, destEmail)
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_TEXT, content)
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }

    fun addContact(
        context: Activity,
        name: String,
        title: String,
        phone: String,
        email: String,
        company: String
    ) {
        val intent = Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI)
        intent.putExtra(ContactsContract.Intents.Insert.NAME, name)
        intent.putExtra(ContactsContract.Intents.Insert.JOB_TITLE, title)
        intent.putExtra(ContactsContract.Intents.Insert.PHONE, phone)
        intent.putExtra(ContactsContract.Intents.Insert.EMAIL, email)
        intent.putExtra(ContactsContract.Intents.Insert.COMPANY, company)

        context.startActivity(intent)
    }

    fun callPhone(activity: Activity, number: String) {
        val intent = Intent(Intent.ACTION_DIAL)
        val data = Uri.parse("tle:$number")
        intent.data = data
        activity.startActivity(intent)
    }
}