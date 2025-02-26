package com.example.billbudddy

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.billbudddy.Adapter.MonthlyBudgetAdapter
import com.example.billbudddy.Domain.MonthlyBudgetData
import com.example.billbudddy.ViewModel.MainViewModel
import com.example.billbudddy.ViewModel.MainViewModelFactory
import com.example.billbudddy.databinding.ActivityReportBinding
import com.example.billbudddy.util.FlipboardLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class ReportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReportBinding
    private lateinit var mainViewModel: MainViewModel
    private lateinit var budgetAdapter: MonthlyBudgetAdapter
    private lateinit var flipboardLayoutManager: FlipboardLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        mainViewModel = ViewModelProvider(this, MainViewModelFactory(this))[MainViewModel::class.java]
        
        setupRecyclerView()
        observeData()
        setVariable()
        updateAmounts()
    }

    private fun setupRecyclerView() {
        budgetAdapter = MonthlyBudgetAdapter(emptyList())
        
        flipboardLayoutManager = FlipboardLayoutManager(this).apply {
            orientation = LinearLayoutManager.VERTICAL
        }
        
        binding.budgetRecyclerView.apply {
            layoutManager = flipboardLayoutManager
            adapter = budgetAdapter
            setHasFixedSize(true)
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    outRect.top = (parent.height * 0.1f).toInt()
                    outRect.bottom = (parent.height * 0.1f).toInt()
                }
            })
        }
    }

    private fun observeData() {
        mainViewModel.monthlyBudgetData.observe(this) { monthlyData ->
            budgetAdapter.updateItems(monthlyData)
        }
        mainViewModel.getMonthlyBudgetData()
    }

    private fun setVariable() {
        binding.apply {
            backBtn.setOnClickListener { 
                finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        }
    }

    private fun updateAmounts() {
        try {
            // Get references to views
            val totalAmountView = findViewById<TextView>(R.id.textView11)
            val incomeAmountView = findViewById<TextView>(R.id.textView13)
            val expenseAmountView = findViewById<TextView>(R.id.expenseAmountText)

            // Get data from MainViewModel which handles local storage
            val transactions = mainViewModel.loadData()
            var totalIncome = 0.0
            var totalExpense = 0.0

            transactions?.forEach { transaction ->
                when (transaction.pic.lowercase()) {
                    "income" -> totalIncome += transaction.price
                    else -> totalExpense += transaction.price
                }
            }

            val total = totalIncome - totalExpense

            // Update UI with formatted amounts
            totalAmountView.text = String.format("$%.2f", total)
            incomeAmountView.text = String.format("$%.2f", totalIncome)
            expenseAmountView.text = String.format("$%.2f", totalExpense)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error updating amounts: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}