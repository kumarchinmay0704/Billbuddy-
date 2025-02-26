package com.example.billbudddy.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.billbudddy.Domain.ExpenseDomain
import com.example.billbudddy.R
import com.example.billbudddy.databinding.ViewholderExpenseBinding
import kotlin.math.abs

class ExpenseListAdapter(private var items: List<ExpenseDomain>) : 
    RecyclerView.Adapter<ExpenseListAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ViewholderExpenseBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: ExpenseDomain) {
            binding.titleTxt.text = item.title
            binding.priceTxt.text = "$${String.format("%.2f", abs(item.price))}"
            binding.timeTxt.text = item.time

            // Set icon and colors based on transaction type
            when {
                item.title.equals("Salary", ignoreCase = true)  -> {
                    binding.pic.setImageResource(R.drawable.income)
                    binding.priceTxt.setTextColor(ContextCompat.getColor(binding.root.context, R.color.green))
                }

                item.title.equals("Freelance", ignoreCase = true)  -> {
                    binding.pic.setImageResource(R.drawable.trade)
                    binding.priceTxt.setTextColor(ContextCompat.getColor(binding.root.context, R.color.green))
                }

                item.title.equals("investment", ignoreCase = true)  -> {
                    binding.pic.setImageResource(R.drawable.futures)
                    binding.priceTxt.setTextColor(ContextCompat.getColor(binding.root.context, R.color.green))
                }
                item.title.equals("Business", ignoreCase = true)  -> {
                    binding.pic.setImageResource(R.drawable.trade)
                    binding.priceTxt.setTextColor(ContextCompat.getColor(binding.root.context, R.color.green))
                }
                item.title.equals("Rental income", ignoreCase = true)  -> {
                    binding.pic.setImageResource(R.drawable.bottom_btn1)
                    binding.priceTxt.setTextColor(ContextCompat.getColor(binding.root.context, R.color.green))
                }
                item.title.equals("Dividends", ignoreCase = true)  -> {
                    binding.pic.setImageResource(R.drawable.btn_1)
                    binding.priceTxt.setTextColor(ContextCompat.getColor(binding.root.context, R.color.green))
                }
                item.title.equals("bonus", ignoreCase = true)  -> {
                    binding.pic.setImageResource(R.drawable.btn_3)
                    binding.priceTxt.setTextColor(ContextCompat.getColor(binding.root.context, R.color.green))
                }
                item.title.equals("gift", ignoreCase = true)  -> {
                    binding.pic.setImageResource(R.drawable.btn_3)
                    binding.priceTxt.setTextColor(ContextCompat.getColor(binding.root.context, R.color.green))
                }
                item.title.equals("interest", ignoreCase = true)  -> {
                    binding.pic.setImageResource(R.drawable.btn_1e)
                    binding.priceTxt.setTextColor(ContextCompat.getColor(binding.root.context, R.color.green))
                }
                item.title.equals("other income", ignoreCase = true)  -> {
                    binding.pic.setImageResource(R.drawable.income)
                    binding.priceTxt.setTextColor(ContextCompat.getColor(binding.root.context, R.color.green))
                }
                item.title.equals("Cinema", ignoreCase = true) -> {
                    binding.pic.setImageResource(R.drawable.img3)
                    binding.priceTxt.setTextColor(ContextCompat.getColor(binding.root.context, R.color.red))
                }
                item.title.equals("home loan", ignoreCase = true) -> {
                    binding.pic.setImageResource(R.drawable.btn_1e)
                    binding.priceTxt.setTextColor(ContextCompat.getColor(binding.root.context, R.color.red))
                }
                item.title.equals("car loan", ignoreCase = true) -> {
                    binding.pic.setImageResource(R.drawable.expense)
                    binding.priceTxt.setTextColor(ContextCompat.getColor(binding.root.context, R.color.red))
                }
                item.title.equals("subscription", ignoreCase = true) -> {
                    binding.pic.setImageResource(R.drawable.expense)
                    binding.priceTxt.setTextColor(ContextCompat.getColor(binding.root.context, R.color.red))
                }
                item.title.equals("Restaurant", ignoreCase = true) -> {
                    binding.pic.setImageResource(R.drawable.img1)
                    binding.priceTxt.setTextColor(ContextCompat.getColor(binding.root.context, R.color.red))
                }
                item.title.equals("McDonald's", ignoreCase = true) -> {
                    binding.pic.setImageResource(R.drawable.img2)
                    binding.priceTxt.setTextColor(ContextCompat.getColor(binding.root.context, R.color.red))
                }
                item.title.equals("shopping", ignoreCase = true) -> {
                    binding.pic.setImageResource(R.drawable.bottom_btn5)
                    binding.priceTxt.setTextColor(ContextCompat.getColor(binding.root.context, R.color.red))
                }
                item.title.equals("transport", ignoreCase = true) -> {
                    binding.pic.setImageResource(R.drawable.ic_2)
                    binding.priceTxt.setTextColor(ContextCompat.getColor(binding.root.context, R.color.red))
                }
                item.title.equals("utilities", ignoreCase = true) -> {
                    binding.pic.setImageResource(R.drawable.expense)
                    binding.priceTxt.setTextColor(ContextCompat.getColor(binding.root.context, R.color.red))
                }
                item.title.equals("entertainment", ignoreCase = true) -> {
                    binding.pic.setImageResource(R.drawable.top_logo_login)
                    binding.priceTxt.setTextColor(ContextCompat.getColor(binding.root.context, R.color.red))
                }
                item.title.equals("healthcare", ignoreCase = true) -> {
                    binding.pic.setImageResource(R.drawable.expense)
                    binding.priceTxt.setTextColor(ContextCompat.getColor(binding.root.context, R.color.red))
                }
                item.title.equals("grocery", ignoreCase = true) -> {
                    binding.pic.setImageResource(R.drawable.bottom_btn5)
                    binding.priceTxt.setTextColor(ContextCompat.getColor(binding.root.context, R.color.red))
                }
                item.title.equals("netflix", ignoreCase = true) -> {
                    binding.pic.setImageResource(R.drawable.netflix)
                    binding.priceTxt.setTextColor(ContextCompat.getColor(binding.root.context, R.color.red))
                }
                item.title.equals("rent", ignoreCase = true) -> {
                    binding.pic.setImageResource(R.drawable.bottom_btn1)
                    binding.priceTxt.setTextColor(ContextCompat.getColor(binding.root.context, R.color.red))
                }
                item.title.equals("paypal", ignoreCase = true) -> {
                    binding.pic.setImageResource(R.drawable.p1)
                    binding.priceTxt.setTextColor(ContextCompat.getColor(binding.root.context, R.color.red))
                }
                item.title.equals("starbucks", ignoreCase = true) -> {
                    binding.pic.setImageResource(R.drawable.s1)
                    binding.priceTxt.setTextColor(ContextCompat.getColor(binding.root.context, R.color.red))
                }
                item.price < 0 -> {
                    binding.pic.setImageResource(R.drawable.expense)
                    binding.priceTxt.setTextColor(ContextCompat.getColor(binding.root.context, R.color.red))
                }
            }

            // Additional check to ensure withdrawals are always red
            if (item.price < 0) {
                binding.priceTxt.setTextColor(ContextCompat.getColor(binding.root.context, R.color.red))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ViewholderExpenseBinding.inflate(
            LayoutInflater.from(parent.context), 
            parent, 
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<ExpenseDomain>) {
        items = newItems
        notifyDataSetChanged()
    }
}