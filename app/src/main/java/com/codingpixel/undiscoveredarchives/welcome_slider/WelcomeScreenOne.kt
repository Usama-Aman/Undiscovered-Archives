package com.codingpixel.undiscoveredarchives.welcome_slider

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.codingpixel.undiscoveredarchives.databinding.FragmentWelcomeScreenOneBinding

class WelcomeScreenOne : Fragment() {

    private lateinit var binding: FragmentWelcomeScreenOneBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWelcomeScreenOneBinding.inflate(inflater, container, false)

        return binding.root
    }
}