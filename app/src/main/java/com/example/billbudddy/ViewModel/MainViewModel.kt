package com.example.billbudddy.ViewModel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.billbudddy.Domain.BudgetDomain
import com.example.billbudddy.Domain.ExpenseDomain
import com.example.billbudddy.Domain.MonthlyBudgetData
import com.example.billbudddy.Repository.MainRepository
import java.util.*

class MainViewModel(context: Context) : ViewModel() {
    private val repository = MainRepository(context)
    
    private val _balance = MutableLiveData<Double>()
    val balance: LiveData<Double> = _balance

    private val _monthlyBudgetData = MutableLiveData<List<MonthlyBudgetData>>()
    val monthlyBudgetData: LiveData<List<MonthlyBudgetData>> = _monthlyBudgetData

    init {
        try {
            _balance.value = repository.getBalance()
            getMonthlyBudgetData()
        } catch (e: Exception) {
            e.printStackTrace()
            _balance.value = 0.0
            _monthlyBudgetData.value = emptyList()
        }
    }

    fun loadData() = try {
        repository.items ?: emptyList()
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }

    fun loadBudget() = repository.budget

    fun addDeposit(category: String, amount: Double) {
        repository.addTransaction(category, amount, true)
        _balance.value = repository.getBalance()
        getMonthlyBudgetData()
    }

    fun addWithdraw(category: String, amount: Double) {
        repository.addTransaction(category, amount, false)
        _balance.value = repository.getBalance()
        getMonthlyBudgetData()
    }

    fun getMonthlyStats() = try {
        repository.getMonthlyStats()
    } catch (e: Exception) {
        e.printStackTrace()
        Triple(0.0, 0.0, 0.0)  // Return default values if there's an error
    }

    fun getLastTwelveMonthsData(): List<MonthlyBudgetData> {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        
        return (0..11).map { monthsAgo ->
            calendar.set(Calendar.MONTH, currentMonth - monthsAgo)
            val month = calendar.get(Calendar.MONTH)
            val year = calendar.get(Calendar.YEAR)
            val (_, income, expense) = repository.getMonthlyStats(month, year)
            MonthlyBudgetData(month, year, income, expense)
        }
    }

    fun getMonthlyBudgetData() {
        try {
            val calendar = Calendar.getInstance()
            val monthlyData = (0..11).map { month ->
                val (_, income, expense) = repository.getMonthlyStats(month, 2025)
                MonthlyBudgetData(
                    month = month,
                    year = 2025,
                    income = income,
                    expense = expense
                )
            }
            _monthlyBudgetData.value = monthlyData
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}