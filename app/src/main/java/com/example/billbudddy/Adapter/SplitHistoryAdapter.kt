package com.example.billbudddy.Adapter

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.billbudddy.Domain.SplitHistory
import com.example.billbudddy.Domain.User
import com.example.billbudddy.R
import com.example.billbudddy.databinding.DialogSplitDetailsBinding
import com.example.billbudddy.databinding.ItemSplitHistoryBinding
import com.google.android.material.chip.Chip
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.billbudddy.Adapter.SplitParticipantsAdapter

class SplitHistoryAdapter(
    private val splits: MutableList<SplitHistory> = mutableListOf()
) : RecyclerView.Adapter<SplitHistoryAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemSplitHistoryBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(split: SplitHistory) {
            binding.apply {
                splitTitleText.text = split.title
                splitTypeText.text = split.type
                splitDateText.text = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    .format(Date(split.date))
                totalAmountText.text = "$${split.amount}"
                

                participantsGroup.removeAllViews()


                split.participants.take(3).forEach { user ->
                    val chip = Chip(itemView.context).apply {
                        text = user.name
                        isClickable = false
                        isCheckable = false
                    }
                    participantsGroup.addView(chip)
                }
                

                if (split.participants.size > 3) {
                    val remainingCount = split.participants.size - 3
                    val moreChip = Chip(itemView.context).apply {
                        text = "+$remainingCount more"
                        isClickable = false
                        isCheckable = false
                        setChipBackgroundColorResource(R.color.accent_color)
                        setTextColor(itemView.context.getColor(R.color.white))
                    }
                    participantsGroup.addView(moreChip)
                }


                root.setOnClickListener {
                    showSplitDetailsDialog(itemView.context, split)
                }
            }
        }
    }

    private fun showSplitDetailsDialog(context: Context, split: SplitHistory) {
        val dialog = Dialog(context, R.style.DialogTheme)
        val binding = DialogSplitDetailsBinding.inflate(LayoutInflater.from(context))
        
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(binding.root)
        

        dialog.window?.apply {
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        binding.apply {
            splitTitleText.text = split.title
            splitTypeText.text = split.type
            splitDateText.text = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                .format(Date(split.date))
            totalAmountText.text = "$${split.amount}"

            // Setup participants RecyclerView
            participantsRecyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = SplitParticipantsAdapter(split.participants, split.amounts)
            }

            closeButton.setOnClickListener {
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    fun addSplit(split: SplitHistory) {
        splits.add(0, split) // Add to beginning of list
        notifyItemInserted(0)
    }

    fun getSplits(): List<SplitHistory> = splits

    fun setSplits(splits: List<SplitHistory>) {
        this.splits.clear()
        this.splits.addAll(splits)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSplitHistoryBinding.inflate(
            LayoutInflater.from(parent.context), 
            parent, 
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(splits[position])
    }

    override fun getItemCount(): Int = splits.size
} 