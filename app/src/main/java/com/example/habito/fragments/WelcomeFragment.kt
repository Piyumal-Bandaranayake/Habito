package com.example.habito.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.habito.R
import com.example.habito.databinding.FragmentWelcomeBinding

/**
 * WelcomeFragment is the first screen of onboarding
 */
class WelcomeFragment : Fragment() {
    
    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.buttonNext.setOnClickListener {
            // Navigate to name input onboarding screen
            findNavController().navigate(R.id.action_welcome_to_onboarding)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
