package com.codingpixel.undiscoveredarchives.loader

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.codingpixel.undiscoveredarchives.databinding.DialogLoaderBinding
import com.codingpixel.undiscoveredarchives.utils.AppUtils

class LoaderDialog(val dismissCallback: () -> Unit) : DialogFragment() {

    private lateinit var binding: DialogLoaderBinding
    private lateinit var mContext: Context

    override fun onResume() {
        dialog?.setCanceledOnTouchOutside(false)
        val window: Window? = dialog!!.window
//        window!!.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        window!!.setGravity(Gravity.CENTER)
        window.setBackgroundDrawableResource(android.R.color.transparent)
        window.setDimAmount(0f)
        // Call super onResume after sizing
        super.onResume()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DialogLoaderBinding.inflate(layoutInflater, container, false)
        mContext = requireContext()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = true
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismissCallback()
        AppUtils.touchScreenEnableDisable(Loader.mContext!!, true)
    }
}