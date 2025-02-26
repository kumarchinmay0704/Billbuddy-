package com.example.billbudddy

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import android.widget.ArrayAdapter
import com.example.billbudddy.databinding.ActivityTransactionBinding
import com.example.billbudddy.ViewModel.MainViewModel
import com.example.billbudddy.ViewModel.MainViewModelFactory

class TransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransactionBinding
    private lateinit var mainViewModel: MainViewModel
    private var isDeposit = true
    
    private val withdrawCategories = listOf(
        "Home Loan", "Car Loan", "Subscription",
        "Restaurant", "McDonald's", "Cinema", "Shopping",
        "Transport", "Utilities", "Entertainment", "Healthcare",
        "Grocery", "Netflix", "Rent", "PayPal", "Starbucks"
    )

    private val depositCategories = listOf(
        "Salary", "Freelance", "Investment", "Business",
        "Rental Income", "Dividends", "Bonus", "Gift",
        "Interest", "Other Income"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainViewModel = MainViewModelFactory.getViewModel(this, applicationContext)
        isDeposit = intent.getBooleanExtra("isDeposit", true)
        binding.titleTxt.text = if (isDeposit) "Deposit" else "Withdraw"

        setupCategorySpinner()
        binding.backBtn.setOnClickListener { finish() }
        
        binding.submitBtn.setOnClickListener {
            val amount = binding.amountEdt.text.toString().toDoubleOrNull() ?: 0.0
            if (amount > 0) {
                val category = binding.categorySpinner.selectedItem.toString()
                if (isDeposit) {
                    mainViewModel.addDeposit(category, amount)
                } else {
                    mainViewModel.addWithdraw(category, amount)
                }
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    private fun setupCategorySpinner() {
        val categories = if (isDeposit) depositCategories else withdrawCategories
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.categorySpinner.adapter = adapter
    }
} 