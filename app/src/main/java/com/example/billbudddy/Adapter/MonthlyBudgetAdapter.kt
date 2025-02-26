package com.example.billbudddy.Adapter

import android.graphics.Camera
import android.graphics.Matrix
import android.view.LayoutInflater
import android.view.ViewGroup
import android.animation.ValueAnimator
import androidx.recyclerview.widget.RecyclerView
import com.example.billbudddy.Domain.MonthlyBudgetData
import com.example.billbudddy.databinding.BudgetFlipcardBinding
import java.text.SimpleDateFormat
import java.util.*

class MonthlyBudgetAdapter(private var items: List<MonthlyBudgetData>) :
    RecyclerView.Adapter<MonthlyBudgetAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: BudgetFlipcardBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.cameraDistance = 8000f * binding.root.resources.displayMetrics.density
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = BudgetFlipcardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, item.year)
            set(Calendar.MONTH, item.month)
            set(Calendar.DAY_OF_MONTH, 1)  // Set to first day of month
        }
        
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        
        holder.binding.apply {
            root.setOnClickListener {
                // Flip animation on click
                root.animate()
                    .setDuration(400)
                    .rotationX(360f)
                    .withEndAction {
                        root.rotationX = 0f
                    }
                    .start()
            }

            // Set month and year
            monthYearText.text = monthFormat.format(calendar.time)
            
            // Calculate and set progress
            val progress = if (item.income > 0) 
                (item.expense / item.income * 100).toFloat() 
            else 0f
            monthlyProgressBar.progress = progress.coerceIn(0f, 100f)
            
            // Format currency values
            incomeText.text = "$${String.format("%.2f", item.income)}"
            expenseText.text = "$${String.format("%.2f", item.expense)}"
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<MonthlyBudgetData>) {
        items = newItems
        notifyDataSetChanged()
    }
} 