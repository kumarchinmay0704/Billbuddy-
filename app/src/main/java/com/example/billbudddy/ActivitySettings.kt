package com.example.billbudddy

import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.billbudddy.databinding.ActivitySettingsBinding

class ActivitySettings : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        applyAnimations()
    }

    private fun setupUI() {
        binding.apply {

            backButton.setOnClickListener {
                finish()
            }


            darkModeSwitch.apply {
                isChecked = isDarkModeEnabled()
                setOnCheckedChangeListener { _, isChecked ->
                    toggleDarkMode(isChecked)
                }
            }
        }
    }

    private fun applyAnimations() {
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        binding.settingsTitle.startAnimation(fadeIn)
    }

    private fun isDarkModeEnabled(): Boolean {
        return AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
    }

    private fun toggleDarkMode(enabled: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (enabled) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
} 