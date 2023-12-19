package com.codingpixel.undiscoveredarchives.home.add_new_listing

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.astritveliu.boom.Boom
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.base.AppController
import com.codingpixel.undiscoveredarchives.base.AppController.Companion.addEditUserProductData
import com.codingpixel.undiscoveredarchives.base.BaseActivity
import com.codingpixel.undiscoveredarchives.databinding.ActivityAddListing3Binding
import com.codingpixel.undiscoveredarchives.home.add_new_listing.AddListingActivity.Companion.fromEdit
import com.codingpixel.undiscoveredarchives.home.add_new_listing.AddListingActivity.Companion.productGradients
import com.codingpixel.undiscoveredarchives.home.favorites.models.ShipingZone
import com.codingpixel.undiscoveredarchives.home.add_new_listing.upload_product_photos.ProductPhotosActivity
import com.codingpixel.undiscoveredarchives.home.favorites.models.Pivot
import com.codingpixel.undiscoveredarchives.utils.viewVisible

class AddListingActivity3 : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityAddListing3Binding

    private lateinit var shippingZonesAdapter: ShippingZonesAdapter
    private var zonesList: MutableList<ShipingZone> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddListing3Binding.inflate(layoutInflater)
        setContentView(binding.root)

        blackStatusBarIcons()

        initViews()
        initListeners()
        initZonesAdapter()
    }

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    private fun initViews() {
        binding.topBar.ivBack.viewVisible()
        binding.topBar.tvScreenTitle.text = "Add new listing"
    }

    private fun initListeners() {
        binding.topBar.ivBack.setOnClickListener(this)
        binding.btnNext.setOnClickListener(this)
        Boom(binding.btnNext)
    }

    private fun initZonesAdapter() {
        zonesList.clear()

        if (fromEdit) {
            zonesList.addAll(productGradients.shiping_zones)
            for (i in productGradients.shiping_zones.indices) {
                for (j in addEditUserProductData?.zones?.indices!!) {
                    if (productGradients.shiping_zones[i].id == addEditUserProductData?.zones?.get(j)?.id) {
                        zonesList[i].isChecked = true
                        zonesList[i].pivot = Pivot(
                            addEditUserProductData?.zones?.get(j)?.pivot?.product_id ?: -1,
                            addEditUserProductData?.zones?.get(j)?.pivot?.shipping_charges ?: "",
                            addEditUserProductData?.zones?.get(j)?.pivot?.shipping_zone_id ?: -1,
                        )
                    }
                }
            }

        } else
            zonesList.addAll(productGradients.shiping_zones)

        shippingZonesAdapter = ShippingZonesAdapter(
            zonesList,
            object : ShippingZonesAdapter.ShippingZonesInterface {
                override fun onZoneClicked(position: Int) {
                    binding.shippingRecyclerView.post {
                        shippingZonesAdapter.notifyItemChanged(position)
                    }
                }
            })
        binding.shippingRecyclerView.adapter = shippingZonesAdapter
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivBack -> {
                finish()
            }
            R.id.btnNext -> {
                addEditUserProductData?.zones?.addAll(zonesList.filter { it.isChecked })
                startActivity(Intent(this, ProductPhotosActivity::class.java))
            }
        }
    }


}