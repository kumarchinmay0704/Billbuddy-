package com.example.billbudddy.Adapter

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.billbudddy.Domain.User
import com.example.billbudddy.databinding.ItemSplitDetailUserBinding

class SplitDetailsAdapter : RecyclerView.Adapter<SplitDetailsAdapter.ViewHolder>() {
    private var users = listOf<User>()
    private var amounts = mutableMapOf<User, Float>()
    private var customAmounts = mutableMapOf<User, Float>()
    private var isCustomMode = false
    private var totalBillAmount = 0f
    private var onAmountChangedListener: ((Float) -> Unit)? = null
    private val textWatchers = mutableMapOf<User, TextWatcher>()

    inner class ViewHolder(private val binding: ItemSplitDetailUserBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(user: User) {
            binding.apply {
                userName.text = user.name
                userEmail.text = user.email
                
                textWatchers[user]?.let { amountInput.removeTextChangedListener(it) }
                
                amountInput.isEnabled = isCustomMode
                
                val currentAmount = if (isCustomMode) {
                    customAmounts[user] ?: amounts[user] ?: 0f
                } else {
                    amounts[user] ?: 0f
                }
                
                if (amountInput.text.toString() != String.format("%.1f", currentAmount)) {
                    amountInput.setText(String.format("%.1f", currentAmount))
                }
                
                val textWatcher = object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                    override fun afterTextChanged(s: Editable?) {
                        if (isCustomMode && s != null) {
                            try {
                                val newAmount = s.toString().toFloatOrNull() ?: 0f
                                customAmounts[user] = newAmount
                                amounts[user] = newAmount
                                calculateTotalSplitAmount()
                            } catch (e: NumberFormatException) {
                                Log.e("SplitDetailsAdapter", "Error parsing amount", e)
                                customAmounts[user] = 0f
                                amounts[user] = 0f
                            }
                        }
                    }
                }
                
                textWatchers[user] = textWatcher
                amountInput.addTextChangedListener(textWatcher)
            }
        }
    }

    private fun calculateTotalSplitAmount() {
        val total = amounts.values.sum()
        onAmountChangedListener?.invoke(total)
    }

    fun setCustomMode(enabled: Boolean) {
        isCustomMode = enabled
        if (!enabled) {
            val equalShare = totalBillAmount / users.size
            users.forEach { user ->
                amounts[user] = equalShare
            }
        } else {
            users.forEach { user ->
                val customAmount = customAmounts[user]
                amounts[user] = customAmount ?: (totalBillAmount / users.size)
            }
        }
        notifyDataSetChanged()
    }

    fun setTotalBillAmount(amount: Float) {
        totalBillAmount = amount
        if (!isCustomMode && users.isNotEmpty()) {
            val equalShare = amount / users.size
            users.forEach { user ->
                amounts[user] = equalShare
            }
            notifyDataSetChanged()
        }
    }

    fun updateUsers(users: List<User>, equalShare: Float = 0f) {
        this.users = users
        if (!isCustomMode) {
            users.forEach { user ->
                amounts[user] = equalShare
            }
        }
        notifyDataSetChanged()
    }

    fun setInitialCustomAmounts(customAmounts: Map<String, Float>) {
        users.forEach { user ->
            val amount = customAmounts[user.uid] ?: 0f
            this.customAmounts[user] = amount
            if (isCustomMode) {
                amounts[user] = amount
            }
        }
        notifyDataSetChanged()
    }

    fun getIndividualAmounts(): Map<User, Float> = amounts.toMap()

    fun setOnAmountChangedListener(listener: (Float) -> Unit) {
        onAmountChangedListener = listener
    }

    fun recalculateShares() {
        if (isCustomMode) {
            val remainingAmount = totalBillAmount - amounts.values.sum()
            if (remainingAmount != 0f && users.isNotEmpty()) {
                val adjustment = remainingAmount / users.size
                users.forEach { user ->
                    amounts[user] = (amounts[user] ?: 0f) + adjustment
                }
                notifyDataSetChanged()
            }
        }
    }

    fun getCustomAmounts(): Map<User, Float> = customAmounts.toMap()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSplitDetailUserBinding.inflate(
            LayoutInflater.from(parent.context), 
            parent, 
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size
} 