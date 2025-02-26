package com.example.billbudddy.Adapter

import android.content.Context
import android.icu.text.DecimalFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.billbudddy.Domain.BudgetDomain
import com.example.billbudddy.R
import com.example.billbudddy.databinding.ViewholderBudgetBinding

class ReportListAdapter(private val items: MutableList<BudgetDomain>) :
    RecyclerView.Adapter<ReportListAdapter.Viewholder>() {
    class Viewholder(val binding: ViewholderBudgetBinding) : RecyclerView.ViewHolder(binding.root)

    private lateinit var context: Context
    var formatter: DecimalFormat? = null
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ReportListAdapter.Viewholder {
        context = parent.context
        formatter = DecimalFormat("###,###,###,###")
        val binding = ViewholderBudgetBinding.inflate(LayoutInflater.from(context), parent, false)
        return Viewholder(binding)
    }

    override fun onBindViewHolder(holder: ReportListAdapter.Viewholder, position: Int) {
        val item = items[position]

        holder.binding.titleTxt.text = item.title
        holder.binding.percentTxt.text = String.format("%.1f%%", item.percent)
        holder.binding.priceTxt.text = "$${formatter?.format(item.price)} /Month"

        val progressColor = when {
            item.percent >= 90 -> context.resources.getColor(R.color.red, null)
            item.percent >= 75 -> context.resources.getColor(R.color.orange, null)
            else -> if (position % 2 == 1) {
                context.resources.getColor(R.color.blue, null)
            } else {
                context.resources.getColor(R.color.pink, null)
            }
        }

        holder.binding.circularProgressBar.apply {
            progress = item.percent.toFloat()
            progressBarColor = progressColor
            holder.binding.percentTxt.setTextColor(progressColor)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<BudgetDomain>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}