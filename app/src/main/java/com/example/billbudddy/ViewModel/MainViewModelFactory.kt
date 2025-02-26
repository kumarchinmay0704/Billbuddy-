package com.example.billbudddy.ViewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner

class MainViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

    companion object {
        private var viewModel: MainViewModel? = null

        fun getViewModel(owner: ViewModelStoreOwner, context: Context): MainViewModel {
            if (viewModel == null) {
                viewModel = ViewModelProvider(owner, MainViewModelFactory(context))[MainViewModel::class.java]
            }
            return viewModel!!
        }
    }
} 