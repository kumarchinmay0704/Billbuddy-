package com.example.billbudddy

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.billbudddy.Adapter.UserAdapter
import com.example.billbudddy.Domain.User
import com.example.billbudddy.databinding.ActivityUserListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class UserListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserListBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var userAdapter: UserAdapter
    private val users = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        
        setupRecyclerView()
        loadUserChats()
        
        binding.swipeRefresh.setOnRefreshListener {
            loadUserChats()
        }
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter { selectedUser ->
            startChat(selectedUser)
        }
        
        binding.userRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@UserListActivity)
            adapter = userAdapter
        }
    }

    private fun startChat(user: User) {
        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("receiverId", user.uid)
            putExtra("receiverName", user.name)
        }
        startActivity(intent)
    }

    private fun loadUserChats() {
        if (auth.currentUser == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        val currentUser = auth.currentUser ?: return
        
        val usersRef = database.reference.child("users")

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    users.clear()
                    
                    for (userSnapshot in snapshot.children) {
                        val userId = userSnapshot.key
                        if (userId == currentUser.uid) continue
                        
                        val user = userSnapshot.getValue(User::class.java)
                        if (user != null) {
                            user.uid = userId ?: continue
                            users.add(user)
                        }
                    }

                    if (users.isEmpty()) {
                        binding.noChatsText.visibility = View.VISIBLE
                    } else {
                        binding.noChatsText.visibility = View.GONE
                    }

                    users.sortBy { it.name }
                    userAdapter.updateUsers(users)
                    
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                    
                } catch (e: Exception) {
                    Log.e("UserListActivity", "Error loading users", e)
                    Toast.makeText(
                        this@UserListActivity,
                        "Error loading users: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("UserListActivity", "Database error: ${error.message}", error.toException())
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
                Toast.makeText(
                    this@UserListActivity,
                    "Error loading users: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }


    private fun isActive(): Boolean {
        return !isFinishing && !isDestroyed
    }
} 