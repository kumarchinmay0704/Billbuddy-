package com.example.billbudddy.Adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieDrawable
import com.example.billbudddy.Domain.User
import com.example.billbudddy.R
import com.example.billbudddy.databinding.ItemUserBinding

class UserAdapter(private val onUserClick: (User) -> Unit) : 
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {
    
    private var users = mutableListOf<User>()

    fun updateUsers(newUsers: List<User>) {
        users.clear()
        users.addAll(newUsers)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(
            LayoutInflater.from(parent.context), 
            parent, 
            false
        )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount() = users.size

    inner class UserViewHolder(private val binding: ItemUserBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(user: User) {
            try {
                binding.apply {
                    userName.text = user.name
                    userEmail.text = user.email
                    

                    userAvatar.apply {
                        setAnimation(R.raw.animation4)
                        repeatCount = LottieDrawable.INFINITE
                        playAnimation()
                    }
                    
                    root.setOnClickListener { onUserClick(user) }
                }
            } catch (e: Exception) {
                Log.e("UserAdapter", "Error binding user data", e)
                binding.apply {
                    userName.text = "Unknown User"
                    userEmail.text = "No email available"
                }
            }
        }
    }
} 