package com.codingpixel.undiscoveredarchives.home.dialogs

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.astritveliu.boom.Boom
import com.codingpixel.undiscoveredarchives.databinding.DialogGuestUserBinding

class GuestDialog(private val guestDialogInterface: GuestDialogInterface) : DialogFragment() {

    private lateinit var binding: DialogGuestUserBinding
    private lateinit var mContext: Context

    interface GuestDialogInterface {
        fun onSignIn()
        fun onSignUp()
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
        binding = DialogGuestUserBinding.inflate(layoutInflater, container, false)
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
        Boom(binding.btnSignIn)
        Boom(binding.btnSignUp)

        binding.ivCross.setOnClickListener {
            dismiss()
        }
        binding.btnSignIn.setOnClickListener {
            dismiss()
            guestDialogInterface.onSignIn()
        }
        binding.btnSignUp.setOnClickListener {
            dismiss()
            guestDialogInterface.onSignUp()
        }
    }

}