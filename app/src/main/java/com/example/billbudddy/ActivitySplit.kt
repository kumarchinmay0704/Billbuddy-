package com.example.billbudddy

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.billbudddy.Adapter.SplitUsersAdapter
import com.example.billbudddy.Adapter.SplitHistoryAdapter
import com.example.billbudddy.databinding.ActivitySplitBinding
import com.example.billbudddy.Domain.User
import com.example.billbudddy.Domain.SplitHistory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.HashMap
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ActivitySplit : AppCompatActivity() {
    private lateinit var binding: ActivitySplitBinding
    private lateinit var usersAdapter: SplitUsersAdapter
    private lateinit var splitHistoryAdapter: SplitHistoryAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private val PREFS_NAME = "SplitHistoryPrefs"
    private val SPLITS_KEY = "splits"

    companion object {
        private const val SPLIT_DETAILS_REQUEST = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        setUserDetails()
        setupRecyclerView()
        setupSplitHistoryRecyclerView()
        loadUsers()
        setupButtons()


        loadSplitsFromBothSources()
    }

    private fun setupRecyclerView() {
        usersAdapter = SplitUsersAdapter()
        binding.usersRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ActivitySplit, LinearLayoutManager.HORIZONTAL, false)
            adapter = usersAdapter
        }
    }

    private fun loadUsers() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val usersRef = database.getReference("users")
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val usersList = mutableListOf<User>()
                    for (userSnapshot in snapshot.children) {
                        val user = userSnapshot.getValue(User::class.java)
                        if (user != null && user.uid != currentUser.uid) {
                            usersList.add(user)
                        }
                    }
                    usersAdapter.setUsers(usersList)
                } catch (e: Exception) {
                    Log.e("ActivitySplit", "Error loading users", e)
                    Toast.makeText(this@ActivitySplit, "Error loading users", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ActivitySplit", "Database error: ${error.message}")
                Toast.makeText(this@ActivitySplit, "Failed to load users", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupButtons() {
        binding.button.setOnClickListener {
            navigateToSplitDetails()
        }

        binding.button2.setOnClickListener {
            navigateToSplitDetails()
        }
    }

    private fun navigateToSplitDetails() {
        val selectedUsers = usersAdapter.getSelectedUsers()
        if (selectedUsers.isEmpty()) {
            Toast.makeText(this, "Please select at least one user", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val intent = Intent(this, ActivitySplitDetails::class.java).apply {
                putParcelableArrayListExtra("selected_users", ArrayList(selectedUsers))
            }
            startActivityForResult(intent, SPLIT_DETAILS_REQUEST)
        } catch (e: Exception) {
            Log.e("ActivitySplit", "Error navigating to split details", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSplitHistoryRecyclerView() {
        splitHistoryAdapter = SplitHistoryAdapter()
        binding.list.apply {
            layoutManager = LinearLayoutManager(this@ActivitySplit, LinearLayoutManager.HORIZONTAL, false)
            adapter = splitHistoryAdapter
        }

        loadSplitHistory()
    }

    private fun loadSplitHistory() {
        val currentUser = auth.currentUser ?: return
        val splitsRef = database.getReference("splits/${currentUser.uid}")
        
        splitsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val splitsList = mutableListOf<SplitHistory>()
                    for (splitSnapshot in snapshot.children) {
                        val split = splitSnapshot.getValue(SplitHistory::class.java)
                        split?.let { splitsList.add(it) }
                    }

                    splitsList.sortByDescending { it.date }
                    splitHistoryAdapter.setSplits(splitsList)
                } catch (e: Exception) {
                    Log.e("ActivitySplit", "Error loading split history", e)
                    Toast.makeText(this@ActivitySplit, 
                        "Error loading split history: ${e.message}", 
                        Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ActivitySplit", "Database error: ${error.message}")
                Toast.makeText(this@ActivitySplit, 
                    "Failed to load split history", 
                    Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadSplitsFromBothSources() {

        val localSplits = loadSplitsLocally()
        splitHistoryAdapter.setSplits(localSplits)

        // Then try to load from Firebase and merge with local data
        val currentUser = auth.currentUser ?: return
        val splitsRef = database.getReference("splits/${currentUser.uid}")
        
        splitsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val firebaseSplits = mutableListOf<SplitHistory>()
                    for (splitSnapshot in snapshot.children) {
                        val split = splitSnapshot.getValue(SplitHistory::class.java)
                        split?.let { firebaseSplits.add(it) }
                    }
                    

                    val mergedSplits = mergeSplits(localSplits, firebaseSplits)
                    splitHistoryAdapter.setSplits(mergedSplits)
                    

                    saveSplitsLocally(mergedSplits)
                } catch (e: Exception) {
                    Log.e("ActivitySplit", "Error loading split history from Firebase", e)

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ActivitySplit", "Firebase error: ${error.message}")

            }
        })
    }

    private fun mergeSplits(localSplits: List<SplitHistory>, firebaseSplits: List<SplitHistory>): List<SplitHistory> {
        val allSplits = (localSplits + firebaseSplits).distinctBy { 

            Triple(it.title, it.date, it.amount)
        }
        return allSplits.sortedByDescending { it.date }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SPLIT_DETAILS_REQUEST && resultCode == RESULT_OK && data != null) {
            try {
                val splitHistory = data.getParcelableExtra<SplitHistory>("split_history")
                if (splitHistory != null) {

                    splitHistoryAdapter.addSplit(splitHistory)
                    

                    saveSplitsLocally(splitHistoryAdapter.getSplits())
                    saveSplitToFirebase(splitHistory)
                }
                usersAdapter.clearSelection()
            } catch (e: Exception) {
                Log.e("ActivitySplit", "Error processing split result", e)
               // Toast.makeText(this, "Error saving split: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveSplitsLocally(splits: List<SplitHistory>) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val json = gson.toJson(splits)
        prefs.edit().putString(SPLITS_KEY, json).apply()
    }

    private fun loadSplitsLocally(): List<SplitHistory> {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val json = prefs.getString(SPLITS_KEY, null)
        val type = object : TypeToken<List<SplitHistory>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    private fun setUserDetails() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->

            database.reference.child("users").child(user.uid)
                .get()
                .addOnSuccessListener { snapshot ->
                    val name = snapshot.child("name").value as? String ?: user.displayName ?: "User"
                    binding.textView3.text = name
                    binding.textView4.text = user.email
                }
                .addOnFailureListener {

                    binding.textView3.text = user.displayName ?: "User"
                    binding.textView4.text = user.email
                }
        }
    }


    override fun onPause() {
        super.onPause()
        saveSplitsLocally(splitHistoryAdapter.getSplits())
    }

    private fun saveSplitToFirebase(splitHistory: SplitHistory) {
        val currentUser = auth.currentUser ?: return
        val splitsRef = database.getReference("splits/${currentUser.uid}")
        val splitId = splitsRef.push().key ?: return
        

        val sanitizedAmounts = splitHistory.amounts.mapKeys { (key, _) ->
            key.replace(Regex("[^a-zA-Z0-9]"), "_")
        }
        
        val sanitizedSplitHistory = splitHistory.copy(amounts = sanitizedAmounts)
        
        splitsRef.child(splitId).setValue(sanitizedSplitHistory)
            .addOnSuccessListener {
                Log.d("ActivitySplit", "Split saved successfully")
                // After successful Firebase save, also save locally
                val currentSplits = loadSplitsLocally().toMutableList()
                currentSplits.add(0, splitHistory)
                saveSplitsLocally(currentSplits)
            }
            .addOnFailureListener { e ->
                Log.e("ActivitySplit", "Error saving split", e)
                Toast.makeText(this, "Error saving split to database", Toast.LENGTH_SHORT).show()
                // Still save locally even if Firebase fails
                val currentSplits = loadSplitsLocally().toMutableList()
                currentSplits.add(0, splitHistory)
                saveSplitsLocally(currentSplits)
            }
    }
} 