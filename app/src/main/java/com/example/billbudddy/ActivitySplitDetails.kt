package com.example.billbudddy

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.billbudddy.Domain.User
import com.example.billbudddy.Adapter.SplitDetailsAdapter
import com.example.billbudddy.Domain.SplitHistory
import com.example.billbudddy.databinding.ActivitySplitDetailsBinding
import kotlin.math.abs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ActivitySplitDetails : AppCompatActivity() {
    private lateinit var binding: ActivitySplitDetailsBinding
    private lateinit var adapter: SplitDetailsAdapter
    private var selectedUsers: List<User> = emptyList()
    private var totalAmount: Float = 0f
    private var amountsMap: HashMap<String, Float> = HashMap()
    private var splitType: String = "Equal"
    private val PREFS_NAME = "SplitDetailsPrefs"
    private var splitId: String = System.currentTimeMillis().toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplitDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        selectedUsers = intent.getParcelableArrayListExtra("selected_users") ?: emptyList()
        if (selectedUsers.isEmpty()) {
            Toast.makeText(this, "No users selected", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupRecyclerView()
        setupTotalAmountInput()
        setupSplitTypeRadioGroup()
        setupRecalculateButton()
        setupSendButton()
    }

    private fun calculateTotalAmount(): Float {
        return binding.totalAmountInput.text.toString().toFloatOrNull() ?: 0f
    }

    private fun setupSendButton() {
        binding.confirmsplitButton.setOnClickListener {
            val totalAmount = calculateTotalAmount()
            if (totalAmount <= 0) {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            splitType = when (binding.splitTypeGroup.checkedRadioButtonId) {
                R.id.equallyRadio -> "Equal"
                R.id.customRadio -> "Custom"
                else -> {
                    Toast.makeText(this, "Please select a split type", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }


            val sanitizedAmounts = HashMap<String, Float>()
            adapter.getIndividualAmounts().forEach { (user, amount) ->

                val sanitizedKey = user.uid.replace(Regex("[^a-zA-Z0-9]"), "_")
                sanitizedAmounts[sanitizedKey] = amount
            }


            if (splitType == "Custom") {
                saveCustomAmountsLocally()
            }

            val splitHistory = SplitHistory(
                title = binding.splitDescriptionInput.text.toString().trim().ifEmpty { "Split Bill" },
                type = splitType,
                date = System.currentTimeMillis(),
                amount = totalAmount,
                participants = selectedUsers,
                amounts = sanitizedAmounts,
                splitId = splitId
            )

            val resultIntent = Intent().apply {
                putExtra("split_history", splitHistory)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = SplitDetailsAdapter()
        binding.selectedUsersRecyclerView.adapter = adapter
        binding.selectedUsersRecyclerView.layoutManager = LinearLayoutManager(this)
        

        intent.getParcelableExtra<SplitHistory>("split_history")?.let { splitHistory ->
            adapter.setInitialCustomAmounts(splitHistory.amounts)
        }
        
        adapter.updateUsers(selectedUsers)
        adapter.setOnAmountChangedListener { total ->

        }
    }

    private fun setupTotalAmountInput() {
        binding.totalAmountInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                try {
                    totalAmount = s.toString().toFloatOrNull() ?: 0f
                    adapter.setTotalBillAmount(totalAmount)
                } catch (e: NumberFormatException) {
                    totalAmount = 0f
                }
            }
        })
    }

    private fun setupSplitTypeRadioGroup() {
        binding.splitTypeGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.equallyRadio -> {
                    splitType = "Equal"
                    adapter.setCustomMode(false)
                    binding.recalculateButton.visibility = View.GONE
                }
                R.id.customRadio -> {
                    splitType = "Custom"
                    adapter.setCustomMode(true)
                    binding.recalculateButton.visibility = View.VISIBLE

                    loadCustomAmountsLocally()
                }
            }
        }
    }

    private fun updateSplitAmounts() {
        if (totalAmount > 0 && selectedUsers.isNotEmpty()) {
            val equalShare = totalAmount / selectedUsers.size
            adapter.updateUsers(selectedUsers, equalShare)
        }
    }



    private fun setupRecalculateButton() {
        binding.recalculateButton.setOnClickListener {
            adapter.recalculateShares()
        }
        binding.recalculateButton.visibility = View.GONE
    }

    private fun saveCustomAmountsLocally() {
        val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val customAmounts = adapter.getCustomAmounts() // New method we'll add to adapter
        
        val amountsJson = customAmounts.map { (user, amount) ->
            "${user.uid}:$amount"
        }.joinToString(",")
        
        sharedPrefs.edit().putString("custom_amounts_$splitId", amountsJson).apply()
    }

    private fun loadCustomAmountsLocally() {
        val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val amountsJson = sharedPrefs.getString("custom_amounts_$splitId", "")
        
        if (!amountsJson.isNullOrEmpty()) {
            val customAmounts = mutableMapOf<String, Float>()
            amountsJson.split(",").forEach { entry ->
                val (uid, amount) = entry.split(":")
                customAmounts[uid] = amount.toFloat()
            }
            adapter.setInitialCustomAmounts(customAmounts)
        }
    }
}

