package com.example.billbudddy

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.billbudddy.Domain.User
import com.example.billbudddy.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import android.view.animation.AnimationUtils
import android.animation.Animator
import com.airbnb.lottie.LottieDrawable

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        
        setupAnimations()
        setupFirebase()
        setupClickListeners()
        fetchUserData()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupAnimations() {
        try {

            setupLottieAnimation()
            

            binding.imageView7.apply {
                setAnimation(R.raw.animation3)
                repeatCount = LottieDrawable.INFINITE
                playAnimation()
            }


        } catch (e: Exception) {
            Log.e("MainActivity", "Error setting up animations", e)

            binding.imageView7.setImageResource(R.drawable.banner)

        }


        try {



            binding.imageView16.apply {
                setAnimation(R.raw.animation6)
                repeatCount = LottieDrawable.INFINITE
                playAnimation()
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error setting up animations", e)
            binding.imageView16.setImageResource(R.drawable.btn_2) // Fallback image
        }
    }

    private fun setupLottieAnimation() {
        try {
            binding.avatarAnimation.apply {
                setAnimation(R.raw.avatar_animation2)
                repeatCount = LottieDrawable.INFINITE
                playAnimation()
                
                addAnimatorListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {
                        Log.d("MainActivity", "Animation started")
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        Log.d("MainActivity", "Animation ended")
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        Log.e("MainActivity", "Animation cancelled")
                        setImageResource(R.drawable.default_profile)
                    }

                    override fun onAnimationRepeat(animation: Animator) {
                        Log.d("MainActivity", "Animation repeated")
                    }
                })
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error setting up Lottie", e)
            binding.avatarAnimation.setImageResource(R.drawable.default_profile)
        }
    }

    private fun setupFirebase() {
        try {
            auth = FirebaseAuth.getInstance()
            databaseReference = FirebaseDatabase.getInstance().reference.child("users")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error initializing Firebase", e)
            Toast.makeText(this, "Error connecting to database", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClickListeners() {
        binding.apply {

            trackexpense.setOnClickListener {
                startActivity(Intent(this@MainActivity, ActivityExpense::class.java))
            }


            chatButtonLayout.setOnClickListener {
                navigateToChat()
            }
            
            chatButtonIcon.setOnClickListener {
                navigateToChat()
            }

            chating.setOnClickListener {
                navigateToChat()
            }


            profileButton.setOnClickListener {
                try {
                    startActivity(Intent(this@MainActivity, ActivityProfile::class.java))
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error navigating to profile", e)
                    Toast.makeText(
                        this@MainActivity,
                        "Error opening profile: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            supportButton.setOnClickListener {
                try {
                    startActivity(Intent(this@MainActivity, ActivityDevelopers::class.java))
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error navigating to developers", e)
                    Toast.makeText(
                        this@MainActivity,
                        "Error opening support: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }


            loggoutbutton.setOnClickListener {
                logout()
            }

            splitbutton.setOnClickListener {
                try {
                    startActivity(Intent(this@MainActivity, ActivitySplit::class.java))
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error starting ActivitySplit", e)
                    Toast.makeText(
                        this@MainActivity,
                        "Error opening split bill: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }


            settingsButton.setOnClickListener {
                try {
                    startActivity(Intent(this@MainActivity, ActivitySettings::class.java))
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error navigating to settings", e)
                    Toast.makeText(
                        this@MainActivity,
                        "Error opening settings: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun navigateToChat() {
        try {
            startActivity(Intent(this@MainActivity, UserListActivity::class.java))
        } catch (e: Exception) {
            Log.e("MainActivity", "Error navigating to chat", e)
            Toast.makeText(this, "Error opening chat: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchUserData() {
        val currentUser = auth.currentUser ?: return
        databaseReference.child(currentUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val user = snapshot.getValue(User::class.java)
                        user?.let {
                            binding.apply {
                                textView9.apply {
                                    text = it.name
                                    startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_slide_up))
                                }
                                
                                textView10.apply {
                                    text = it.email
                                    startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_slide_up_delayed))
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error parsing user data", e)
                        Toast.makeText(baseContext, "Error loading user data", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MainActivity", "Database error: ${error.message}")
                    Toast.makeText(
                        this@MainActivity,
                        "Failed to load user data: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }






    private fun logout() {
        try {
            auth.signOut()
            startActivity(Intent(this, activity_login::class.java))
            finish()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error during logout", e)
            Toast.makeText(this, "Error logging out: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}