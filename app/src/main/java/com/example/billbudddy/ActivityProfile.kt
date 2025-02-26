package com.example.billbudddy

import android.animation.Animator
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.example.billbudddy.Domain.User
import com.example.billbudddy.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.airbnb.lottie.LottieDrawable

class ActivityProfile : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        
        setupUI()
        applyAnimations()
        loadUserData()
    }

    private fun setupUI() {
        binding.apply {
            backButton.setOnClickListener {
                finish()
            }

            // Setup Lottie animation
            profileImage.apply {
                setAnimation(R.raw.avatar_animation2)
                repeatCount = LottieDrawable.INFINITE
                playAnimation()
                
                addAnimatorListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {
                        Log.d("ActivityProfile", "Animation started")
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        Log.d("ActivityProfile", "Animation ended")
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        Log.e("ActivityProfile", "Animation cancelled")
                    }

                    override fun onAnimationRepeat(animation: Animator) {
                        Log.d("ActivityProfile", "Animation repeated")
                    }
                })
            }
        }
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Set email immediately from Auth
            binding.userEmailText.text = currentUser.email

            // Fetch user data from Realtime Database
            val userRef = database.reference.child("users").child(currentUser.uid)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    user?.let {
                        binding.userNameText.text = it.name
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                    binding.userNameText.text = "User"
                }
            })
        }
    }

    private fun applyAnimations() {
        // Load animations
        val slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down)
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up)

        // Apply animations
        binding.backButton.startAnimation(fadeIn)
        binding.profileCard.startAnimation(slideDown)
        binding.profileImage.startAnimation(scaleUp)
    }
} 