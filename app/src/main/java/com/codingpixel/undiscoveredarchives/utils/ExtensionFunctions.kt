package com.codingpixel.undiscoveredarchives.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Patterns
import android.view.View
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.codingpixel.undiscoveredarchives.R
import com.google.android.material.textfield.TextInputLayout
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


/*Functions of toast*/
fun String.isValidEmail(): Boolean =
    this.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()


/*Functions Related to Views*/

fun View.viewVisible() {
    this.visibility = View.VISIBLE
}

fun View.viewGone() {
    this.visibility = View.GONE
}

fun View.viewInVisible() {
    this.visibility = View.INVISIBLE
}

fun View.viewVisibility(): Int {
    return this.visibility
}

fun View.isVisible(): Boolean {
    return this.visibility == View.VISIBLE
}

fun View.isGone(): Boolean {
    return this.visibility == View.GONE
}

fun View.isInVisible(): Boolean {
    return this.visibility == View.INVISIBLE
}


/*Functions Related to Validations*/

@SuppressLint("MissingPermission")
@Suppress("DEPRECATION")
fun isInternetAvailable(context: Context): Boolean {
    var result = false
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        cm?.run {
            cm.getNetworkCapabilities(cm.activeNetwork)?.run {
                result = when {
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    else -> false
                }
            }
        }
    } else {
        cm?.run {
            cm.activeNetworkInfo?.run {
                if (type == ConnectivityManager.TYPE_WIFI) {
                    result = true
                } else if (type == ConnectivityManager.TYPE_MOBILE) {
                    result = true
                }
            }
        }
    }
    return result
}

fun focusIn(et: EditText, src: Int, ctx: Context) {
    var drawable: Drawable? = ContextCompat.getDrawable(ctx, src)
    drawable?.colorFilter = PorterDuffColorFilter(
        ContextCompat.getColor(ctx, R.color.white), PorterDuff.Mode.SRC_IN
    )
    et.setCompoundDrawablesWithIntrinsicBounds(
        drawable,
        null,
        null,
        null
    )
}

fun focusInTil(et: TextInputLayout, src: Int, ctx: Context) {
    var drawable: Drawable? = ContextCompat.getDrawable(ctx, src)
    drawable?.colorFilter = PorterDuffColorFilter(
        ContextCompat.getColor(ctx, R.color.white), PorterDuff.Mode.SRC_IN
    )

    et.startIconDrawable = drawable
}

fun focusOut(et: EditText, src: Int, ctx: Context) {
    var drawable: Drawable? = ContextCompat.getDrawable(ctx, src)
    drawable = DrawableCompat.wrap(drawable!!)
    et.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
}

fun focusOutTil(et: TextInputLayout, src: Int, ctx: Context) {
    var drawable: Drawable? = ContextCompat.getDrawable(ctx, src)
    drawable = DrawableCompat.wrap(drawable!!)
    et.startIconDrawable = drawable
}

fun ConvertDateFormat(date_string: String): String {
    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    var myDate: Date? = null
    try {
        myDate = dateFormat.parse(date_string)

    } catch (e: ParseException) {
        e.printStackTrace()
    }

    val timeFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    return timeFormat.format(myDate ?: Date())
}




