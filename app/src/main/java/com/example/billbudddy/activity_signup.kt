package com.example.billbudddy

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.billbudddy.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ActivitySignup : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("users")
        auth = FirebaseAuth.getInstance()

        binding.imageView2.setOnClickListener {
            val email = binding.editTextTextPersonName.text.toString()
            val password = binding.editTextTextPersonName2.text.toString()
            val name = binding.editTextTextPersonName4.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty()) {
                signupUser(email, password, name)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
        binding.loginRedirect.setOnClickListener {
            startActivity(Intent(this@ActivitySignup, activity_login::class.java))
            finish()
        }
    }

    private fun signupUser(email: String, password: String, name: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val id = auth.currentUser?.uid
                if (id != null) {
                    val userData = UserData(id, name, email, password)
                    databaseReference.child(id).setValue(userData)
                        .addOnSuccessListener {
                            Toast.makeText(this@ActivitySignup, "Signup Successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@ActivitySignup, activity_login::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this@ActivitySignup, "Database Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@ActivitySignup, "Authentication Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

// Data class for user information
