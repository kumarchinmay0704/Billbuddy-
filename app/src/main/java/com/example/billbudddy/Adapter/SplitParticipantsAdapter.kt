package com.example.billbudddy.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.billbudddy.Domain.User
import com.example.billbudddy.databinding.ItemSplitParticipantBinding

class SplitParticipantsAdapter(
    private val participants: List<User>,
    private val amounts: Map<String, Float>
) : RecyclerView.Adapter<SplitParticipantsAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemSplitParticipantBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(user: User) {
            binding.apply {
                participantName.text = user.name
                participantAmount.text = "$${amounts[user.uid] ?: 0f}"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemSplitParticipantBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(participants[position])
    }

    override fun getItemCount(): Int = participants.size
} 