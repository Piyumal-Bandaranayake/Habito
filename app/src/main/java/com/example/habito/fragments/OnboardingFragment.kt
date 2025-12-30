package com.example.habito.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.habito.R
import com.example.habito.data.DataManager
import com.example.habito.databinding.FragmentOnboardingBinding

/**
 * OnboardingFragment displays welcome screen and collects user name
 */
class OnboardingFragment : Fragment() {
    
    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var dataManager: DataManager
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize data manager
        dataManager = DataManager(requireContext())
        
        // Setup UI
        setupUI()
        setupButtonListeners()
    }
    
    private fun setupUI() {
        // Set welcome text
        binding.textViewWelcome.text = "Welcome to Habito"
        binding.textViewSubtitle.text = "Your personal wellness companion"
        binding.textViewDescription.text = "Let's start your journey to better habits and wellness. First, tell us your name."
        
        // Setup input field
        binding.editTextUserName.hint = "Enter your name"
    }
    
    private fun setupButtonListeners() {
        binding.buttonGetStarted.setOnClickListener {
            val userName = binding.editTextUserName.text.toString().trim()
            
            if (userName.isEmpty()) {
                Toast.makeText(context, "Please enter your name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (userName.length < 2) {
                Toast.makeText(context, "Name must be at least 2 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Save user name and mark onboarding as completed
            dataManager.saveUserName(userName)
            dataManager.setOnboardingCompleted(true)
            
            // Navigate to main app
            findNavController().navigate(R.id.action_onboarding_to_habits)
        }
        
        binding.buttonSkip.setOnClickListener {
            // Skip onboarding with default name
            dataManager.saveUserName("User")
            dataManager.setOnboardingCompleted(true)
            
            // Navigate to main app
            findNavController().navigate(R.id.action_onboarding_to_habits)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
