package com.codingpixel.undiscoveredarchives.home.dialogs

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.codingpixel.undiscoveredarchives.databinding.DialogAccountPendingBinding
import com.codingpixel.undiscoveredarchives.databinding.DialogShippmentPlacedBinding

class AccountPending(private val accountPendingInterface: AccountPendingInterface) : DialogFragment() {

    private lateinit var binding: DialogAccountPendingBinding
    private lateinit var mContext: Context

    interface AccountPendingInterface {
        fun onDone()
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
        window.setLayout((size.x * 0.90).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
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
        binding = DialogAccountPendingBinding.inflate(layoutInflater, container, false)
        mContext = requireContext()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        isCancelable = false
        initListeners()
    }

    private fun initListeners() {
        binding.ivCross.setOnClickListener {
            dismiss()
            accountPendingInterface.onDone()
        }

        binding.btnOK.setOnClickListener {
            dismiss()
            accountPendingInterface.onDone()
        }
    }
}