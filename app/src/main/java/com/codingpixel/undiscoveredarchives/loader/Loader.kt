package com.codingpixel.undiscoveredarchives.loader

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.codingpixel.undiscoveredarchives.utils.AppUtils
import java.lang.Exception

class Loader {


    companion object {
        var mContext: Context? = null
        var loader: LoaderDialog? = null

        fun showLoader(activity: AppCompatActivity, onDismiss: () -> Unit) {
            try {

                if (loader != null && loader!!.isAdded) {
                    return
                }
                mContext = activity
                if (activity.isFinishing)
                    return
                try {
                    if (!activity.isFinishing) {
                        AppUtils.touchScreenEnableDisable(mContext!!, false)
                        loader = LoaderDialog(onDismiss)
                        loader?.show(activity.supportFragmentManager, "Loader")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    AppUtils.touchScreenEnableDisable(mContext!!, true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                AppUtils.touchScreenEnableDisable(mContext!!, true)
            }

        }

        fun hideLoader() {
            try {
                if (loader != null && loader!!.isAdded && mContext != null) {
                    AppUtils.touchScreenEnableDisable(mContext!!, true)
                    loader!!.dismiss()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                AppUtils.touchScreenEnableDisable(mContext!!, true)
            }
        }
    }


}