package com.codingpixel.undiscoveredarchives.home.dialogs

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.activity.result.ActivityResult
import androidx.fragment.app.DialogFragment
import com.astritveliu.boom.Boom
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.auth.models.SearchableSpinnerModel
import com.codingpixel.undiscoveredarchives.base.BaseActivityResult
import com.codingpixel.undiscoveredarchives.databinding.DialogAddNewVariantBinding
import com.codingpixel.undiscoveredarchives.home.add_new_listing.Attribute
import com.codingpixel.undiscoveredarchives.home.searchable_spinner.SearchableSpinnerActivity
import com.codingpixel.undiscoveredarchives.utils.viewGone
import com.codingpixel.undiscoveredarchives.utils.viewVisible

class AddVariantDialog(
    private val commissionedPrice: String,
    private val sizesList: List<Attribute>,
    private val addVariantInterface: AddVariantInterface
) : DialogFragment() {

    private val activityLauncher: BaseActivityResult<Intent, ActivityResult> =
        BaseActivityResult.registerActivityForResult(this)
    private lateinit var binding: DialogAddNewVariantBinding
    private lateinit var mContext: Context

    private var searchableList: ArrayList<SearchableSpinnerModel> = ArrayList()

    private var selectedSizeId = -1

    interface AddVariantInterface {
        fun onAddVariant(
            attributePosition: Int, noOfItems: String, pricePerItem: String, compareAtPrice: String,
            commissionedPrice: String, sellerEarnings: String
        )
    }

    override fun onResume() {
        val window: Window? = dialog!!.window
        val size = Point()
        // Store dimensions of the screen in `size`
        // Store dimensions of the screen in `size`
        val display: Display = window!!.windowManager.defaultDisplay
        display.getSize(size)
        // Set the width of the dialog proportional to 75% of the screen width
        // Set the width of the dialog proportional to 75% of the screen width
        window.setLayout((size.x * 0.90).toInt(), (size.y * 0.8).toInt())
        window.setGravity(Gravity.CENTER)
        // Call super onResume after sizing
        // Call super onResume after sizing
        super.onResume()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DialogAddNewVariantBinding.inflate(layoutInflater, container, false)
        mContext = requireContext()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        isCancelable = false

        initListeners()
        setEditTexts()
    }

    @SuppressLint("SetTextI18n")
    private fun initListeners() {
        binding.tvCommissionedPrice.text = commissionedPrice
        binding.tvCommission.text =
            "$commissionedPrice% of your price will be deducted as commission"

        Boom(binding.sizeSpinner)
        Boom(binding.btnAddVariant)

        binding.ivCross.setOnClickListener {
            dismiss()
        }

        binding.sizeSpinner.setOnClickListener {
            searchableList.clear()
            sizesList.forEachIndexed { _, sizeData ->
                searchableList.add(
                    SearchableSpinnerModel(
                        sizeData.id, sizeData.value, sizeData.id == selectedSizeId
                    )
                )
            }

            val intent = Intent(mContext, SearchableSpinnerActivity::class.java)
            intent.putParcelableArrayListExtra("listData", searchableList)
            intent.putExtra("title", "Select Size")
            activityLauncher.launch(
                intent,
                object : BaseActivityResult.OnActivityResult<ActivityResult> {
                    override fun onActivityResult(result: ActivityResult) {
                        if (result.resultCode == Activity.RESULT_OK) {
                            selectedSizeId = result.data?.getIntExtra("selectedId", -1) ?: -1
                            binding.sizeSpinner.text =
                                result.data?.getStringExtra("selectedValue")
                        }
                    }
                })
        }

        binding.btnAddVariant.setOnClickListener {
            if (validate()) {
                dismiss()
                outer@ for (i in sizesList.indices)
                    if (sizesList[i].id == selectedSizeId) {
                        addVariantInterface.onAddVariant(
                            i,
                            binding.etQuantity.text.toString(),
                            binding.etPrice.text.toString(),
                            binding.etCompareAtPrice.text.toString(),
                            binding.tvCommissionedPrice.text.toString(),
                            binding.tvSellersEarnings.text.toString(),
                        )
                        break@outer
                    }
            }
        }
    }

    private fun setEditTexts() {
        binding.sizeSpinner.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.sizeSpinner.setBackgroundResource(R.drawable.auth_edittext_drawable)
                binding.tilSize.viewGone()
                binding.tilSize.isErrorEnabled = false
            }
        })
        binding.etQuantity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.etQuantity.setBackgroundResource(R.drawable.auth_edittext_drawable)
                binding.tilQuantity.viewGone()
                binding.tilQuantity.isErrorEnabled = false
            }
        })
        binding.etPrice.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.llPrice.setBackgroundResource(R.drawable.auth_edittext_drawable)
                binding.tilPrice.viewGone()
                binding.tilPrice.isErrorEnabled = false

                if (s.toString().isNotEmpty())
                    if (s.toString().toDouble() > 0.0) {
                        val price = s.toString().toDouble()
                        binding.tvSellersEarnings.text =
                            (price - (price * (commissionedPrice.toDouble() / 100))).toString()
                    }
            }
        })
        binding.etCompareAtPrice.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.llComparePrice.setBackgroundResource(R.drawable.auth_edittext_drawable)
                binding.tilComparePrice.viewGone()
                binding.tilComparePrice.isErrorEnabled = false
            }
        })

    }

    private fun validate(): Boolean {
        if (binding.sizeSpinner.text.toString().isBlank()) {
            binding.sizeSpinner.setBackgroundResource(R.drawable.auth_edit_text_error)
            binding.tilSize.viewVisible()
            binding.tilSize.error = "Select Size!"
            binding.sizeSpinner.requestFocus()
            return false
        }
        if (binding.etQuantity.text.toString().isBlank()) {
            binding.etQuantity.setBackgroundResource(R.drawable.auth_edit_text_error)
            binding.tilQuantity.viewVisible()
            binding.tilQuantity.error = "Add Quantity!"
            binding.etQuantity.requestFocus()
            return false
        }
        if (binding.etPrice.text.toString().isBlank()) {
            binding.llPrice.setBackgroundResource(R.drawable.auth_edit_text_error)
            binding.tilPrice.viewVisible()
            binding.tilPrice.error = "Enter Price!"
            binding.etPrice.requestFocus()
            return false
        }

        if (binding.etCompareAtPrice.text.toString().isNotBlank())
            if (binding.etCompareAtPrice.text.toString()
                    .toDouble() <= binding.etPrice.text.toString().toDouble()
            ) {
                binding.llComparePrice.setBackgroundResource(R.drawable.auth_edit_text_error)
                binding.tilComparePrice.viewVisible()
                binding.tilComparePrice.error =
                    "Compare at Price should be greator than Actual Price!"
                binding.etCompareAtPrice.requestFocus()
                return false
            }

        return true
    }
}