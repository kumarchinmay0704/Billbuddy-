package com.example.billbudddy

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.billbudddy.Adapter.ExpenseListAdapter
import com.example.billbudddy.ViewModel.MainViewModel
import com.example.billbudddy.ViewModel.MainViewModelFactory
import com.example.billbudddy.databinding.ActivityExpenseBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale

class ActivityExpense : AppCompatActivity() {
    private lateinit var binding: ActivityExpenseBinding
    private lateinit var mainViewModel: MainViewModel
    private lateinit var adapter: ExpenseListAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    private val transactionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            updateUI()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityExpenseBinding.inflate(layoutInflater)
            setContentView(binding.root)

            mainViewModel = MainViewModelFactory.getViewModel(this, applicationContext)

            window.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )

            auth = FirebaseAuth.getInstance()
            databaseReference = FirebaseDatabase.getInstance().reference.child("users")

            initRecyclerview()
            setVariable()
            setClickListeners()
            observeData()
            fetchUserData()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error initializing app: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun observeData() {
        mainViewModel.balance.observe(this) { balance ->
            binding.balanceTxt.text = String.format(Locale.getDefault(), "$%.2f", balance)
        }
    }

    private fun setVariable() {
        binding.cardBtn.setOnClickListener {
            startActivity(Intent(this, ReportActivity::class.java))
        }
    }

    private fun initRecyclerview() {
        try {
            val layoutManager = LinearLayoutManager(this)
            layoutManager.reverseLayout = true
            layoutManager.stackFromEnd = true
            binding.view1.layoutManager = layoutManager
            val data = mainViewModel.loadData()
            adapter = ExpenseListAdapter(data ?: emptyList())
            binding.view1.adapter = adapter
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error initializing RecyclerView: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setClickListeners() {
        binding.depositBtn.setOnClickListener {
            val intent = Intent(this, TransactionActivity::class.java)
            intent.putExtra("isDeposit", true)
            transactionLauncher.launch(intent)
        }

        binding.withdrawBtn.setOnClickListener {
            val intent = Intent(this, TransactionActivity::class.java)
            intent.putExtra("isDeposit", false)
            transactionLauncher.launch(intent)
        }
    }

    private fun updateUI() {
        try {
            val data = mainViewModel.loadData()
            adapter.updateItems(data ?: emptyList())
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error updating UI: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchUserData() {
        try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                databaseReference.child(currentUser.uid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            try {
                                if (snapshot.exists()) {
                                    val userData = snapshot.getValue(UserData::class.java)
                                    if (userData != null) {

                                        binding.textView.text = userData.name ?: "User"
                                        binding.textView3.text = userData.email ?: "Email"
                                    } else {
                                        Toast.makeText(this@ActivityExpense, "Failed to parse user data", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(this@ActivityExpense, "No user data found", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Log.e("Firebase", "Error parsing: ${e.message}")
                                Toast.makeText(this@ActivityExpense, "Error parsing data: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("Firebase", "Database error: ${error.message}")
                            Toast.makeText(this@ActivityExpense, "Database Error: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
            }
        } catch (e: Exception) {
            Log.e("Firebase", "Fetch error: ${e.message}")
            Toast.makeText(this, "Error fetching data: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}