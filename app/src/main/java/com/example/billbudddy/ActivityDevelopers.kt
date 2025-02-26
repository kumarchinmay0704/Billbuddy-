package com.example.billbudddy

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.example.billbudddy.databinding.ActivityDevelopersBinding

class ActivityDevelopers : AppCompatActivity() {
    private lateinit var binding: ActivityDevelopersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDevelopersBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        
        binding.titleText.startAnimation(fadeIn)
        binding.devCard1.startAnimation(slideUp)
        binding.devCard2.startAnimation(slideUp)
        binding.devCard3.startAnimation(slideUp)


        binding.backButton.setOnClickListener {
            finish()
        }


        setupEmailClicks()
    }

    private fun setupEmailClicks() {
        binding.dev1Email.setOnClickListener { 
            openEmail("kumar.cd22@bmsce.ac.in")
        }
        binding.dev2Email.setOnClickListener {
            openEmail("piyush.ei22@bmsce.ac.in")
        }
        binding.dev3Email.setOnClickListener {
            openEmail("harshdeep.ei22@bmsce.ac.in")
        }
    }

    private fun openEmail(email: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
            putExtra(Intent.EXTRA_SUBJECT, "Regarding BillBuddy App")
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {

            e.printStackTrace()
        }
    }
} 