package com.example.billbudddy.Repository

import android.content.Context
import android.content.SharedPreferences
import com.example.billbudddy.Domain.BudgetDomain
import com.example.billbudddy.Domain.ExpenseDomain
import com.example.billbudddy.Domain.MonthlyBudgetData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class MainRepository(context: Context) {
    private var balance = 0.0
    val items = mutableListOf<ExpenseDomain>()
    val budget = mutableListOf<BudgetDomain>()
    private val prefs: SharedPreferences = context.getSharedPreferences("expense_tracker", Context.MODE_PRIVATE)
    private val gson = Gson()

    init {

        loadSavedData()
    }

    private fun loadSavedData() {
        balance = prefs.getFloat("balance", 0f).toDouble()
        
        val itemsJson = prefs.getString("transactions", "[]")
        val budgetJson = prefs.getString("budget", "[]")
        
        val transactionType = object : TypeToken<List<ExpenseDomain>>() {}.type
        val budgetType = object : TypeToken<List<BudgetDomain>>() {}.type
        
        items.clear()
        items.addAll(gson.fromJson(itemsJson, transactionType))
        
        budget.clear()
        val savedBudget = gson.fromJson<List<BudgetDomain>>(budgetJson, budgetType)
        if (savedBudget.isEmpty()) {

            budget.addAll(listOf(
                BudgetDomain("Home Loan", 1200.0, 80.8),
                BudgetDomain("Subscription", 1200.0, 10.0),
                BudgetDomain("Car Loan", 800.0, 30.0)
            ))
        } else {
            budget.addAll(savedBudget)
        }
    }

    private fun saveData() {
        prefs.edit().apply {
            putFloat("balance", balance.toFloat())
            putString("transactions", gson.toJson(items))
            putString("budget", gson.toJson(budget))
            apply()
        }
    }

    fun updateBudgetProgress() {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)


        budget.forEach { budgetItem ->
            val totalSpent = items.sumOf { transaction ->
                val date = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
                    .parse(transaction.time)
                calendar.time = date!!
                
                if (calendar.get(Calendar.MONTH) == currentMonth && 
                    calendar.get(Calendar.YEAR) == currentYear &&
                    !transaction.pic.equals("income", ignoreCase = true) &&
                    transaction.title.equals(budgetItem.title, ignoreCase = true)) {
                    transaction.price
                } else {
                    0.0
                }
            }
            

            val newProgress = (totalSpent / budgetItem.price * 100).coerceIn(0.0, 100.0)
            budget[budget.indexOf(budgetItem)] = budgetItem.copy(percent = newProgress)
        }
        saveData()
    }

    fun addTransaction(title: String, amount: Double, isDeposit: Boolean) {

        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
        val currentDate = dateFormat.format(calendar.time)

        val transaction = ExpenseDomain(
            title = title,
            price = amount,
            pic = when {
                isDeposit -> "income"
                title.lowercase() == "restaurant" -> "img1"
                title.lowercase() == "mcdonald's" -> "img2"
                title.lowercase() == "cinema" -> "img3"
                else -> "expense"
            },
            time = currentDate
        )
        items.add(0, transaction)
        balance = if (isDeposit) balance + amount else balance - amount
        

        if (!isDeposit) {
            updateBudgetProgress()
        }
        saveData()
    }

    fun getBalance() = balance

    fun getMonthlyStats(targetMonth: Int? = null, targetYear: Int? = null): Triple<Double, Double, Double> {
        val calendar = Calendar.getInstance()
        val currentMonth = targetMonth ?: calendar.get(Calendar.MONTH)
        val currentYear = targetYear ?: calendar.get(Calendar.YEAR)

        var monthlyIncome = 0.0
        var monthlyExpense = 0.0

        items.forEach { transaction ->

            val date = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
                .parse(transaction.time)
            calendar.time = date!!
            
            if (calendar.get(Calendar.MONTH) == currentMonth && 
                calendar.get(Calendar.YEAR) == currentYear) {
                if (transaction.pic == "income") {
                    monthlyIncome += transaction.price
                } else {
                    monthlyExpense += transaction.price
                }
            }
        }

        return Triple(balance, monthlyIncome, monthlyExpense)
    }

    fun getLastTwelveMonthsData(): List<MonthlyBudgetData> {
        return (0..11).map { month ->
            val tempCalendar = Calendar.getInstance()
            tempCalendar.set(Calendar.YEAR, 2025)
            tempCalendar.set(Calendar.MONTH, month)
            
            val stats = getMonthlyStats(
                tempCalendar.get(Calendar.MONTH),
                tempCalendar.get(Calendar.YEAR)
            )
            MonthlyBudgetData(
                month = month+1 ,
                year = 2025,
                income = stats.second,
                expense = stats.third
            )
        }
    }
}