package com.codingpixel.undiscoveredarchives.welcome_slider

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.codingpixel.undiscoveredarchives.databinding.FragmentWelcomeScreenOneBinding
import com.codingpixel.undiscoveredarchives.databinding.FragmentWelcomeScreenTwoBinding

class WelcomeScreenThree  : Fragment() {

    private lateinit var binding: FragmentWelcomeScreenTwoBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWelcomeScreenTwoBinding.inflate(inflater, container, false)

        return binding.root
    }
}