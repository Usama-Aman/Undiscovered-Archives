package com.codingpixel.undiscoveredarchives.home.add_new_listing.upload_product_photos

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.databinding.ItemUploadProductPhotoBinding
import com.codingpixel.undiscoveredarchives.utils.viewGone
import com.codingpixel.undiscoveredarchives.utils.viewVisible

class ProductPhotoAdapter(
    val productPhotoList: MutableList<ProductPhotoModel>,
    val productPhotoInterface: ProductPhotoInterface
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mContext: Context

    interface ProductPhotoInterface {
        fun addNewPhoto()
        fun editPreviousPhoto(position: Int)
        fun deleteProductPhoto(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        mContext = parent.context
        return Item(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                (R.layout.item_upload_product_photo),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is Item -> holder.bind(position)
        }
    }

    override fun getItemCount(): Int = productPhotoList.size

    inner class Item(val binding: ItemUploadProductPhotoBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            //To hide the view if deleted or edit
            binding.llEditPhoto.viewGone()
            binding.viewSelected.viewGone()

            if (!productPhotoList[position].canAddPhoto) {
                binding.ivProductPhoto.viewVisible()
                binding.ivCamera.viewGone()

                Glide
                    .with(mContext)
                    .load(productPhotoList[position].path)
                    .into(binding.ivProductPhoto)
            } else {
                binding.ivCamera.viewVisible()
                binding.ivProductPhoto.viewGone()
            }

            binding.constraintAddPhoto.setOnClickListener {
                if (productPhotoList[position].canAddPhoto) {
                    binding.viewSelected.viewGone()
                    binding.llEditPhoto.viewGone()
                    productPhotoInterface.addNewPhoto()
                } else {

                    if (binding.viewSelected.visibility == View.VISIBLE) {
                        binding.viewSelected.viewGone()
                        binding.llEditPhoto.viewGone()
                    } else {
                        binding.viewSelected.viewVisible()
                        binding.llEditPhoto.viewVisible()
                    }
                }
            }

            binding.ivEditPhoto.setOnClickListener {
                productPhotoInterface.editPreviousPhoto(position)
            }

            binding.ivDeletePhoto.setOnClickListener {
                productPhotoInterface.deleteProductPhoto(position)
            }

        }

    }

}