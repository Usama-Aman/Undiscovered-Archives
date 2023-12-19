package com.codingpixel.undiscoveredarchives.utils

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.base.BaseActivity
import io.ak1.pix.helpers.PixEventCallback
import io.ak1.pix.helpers.addPixToActivity
import io.ak1.pix.models.Flash
import io.ak1.pix.models.Mode
import io.ak1.pix.models.Options
import io.ak1.pix.models.Ratio

class FetchImagesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fetch_images)

        val options = Options().apply {
            ratio = Ratio.RATIO_AUTO                        //Image/video capture ratio
            count = 2                                       //Number of images to restrict selection count
            spanCount = 4                                   //Number for columns in grid
            path = "Pix/Camera"                             //Custom Path For media Storage
            isFrontFacing = false                           //Front Facing camera on start
//            videoDurationLimitInSeconds = 10                  //Duration for video recording
            mode = Mode.Picture                             //Option to select only pictures or videos or both
            flash = Flash.Auto                              //Option to select flash type
//            preSelectedUrls = ArrayList<Uri>()                //Pre selected Image Urls
        }

        addPixToActivity(R.id.container, options) {
            when (it.status) {
                PixEventCallback.Status.SUCCESS -> {
                    Toast.makeText(this@FetchImagesActivity, "Success", Toast.LENGTH_SHORT).show()
                }
                PixEventCallback.Status.BACK_PRESSED -> {
                    Toast.makeText(this@FetchImagesActivity, "Back", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}