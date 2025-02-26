package com.example.billbudddy.Adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.billbudddy.Domain.User
import com.example.billbudddy.databinding.ItemSplitUserBinding
import com.example.billbudddy.ChatActivity

class SplitUsersAdapter : RecyclerView.Adapter<SplitUsersAdapter.ViewHolder>() {
    private val users = mutableListOf<User>()
    private val selectedUsers = mutableSetOf<User>()

    inner class ViewHolder(private val binding: ItemSplitUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.apply {
                userName.text = user.name
                userCheckbox.isChecked = selectedUsers.contains(user)


                userCheckbox.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedUsers.add(user)
                    } else {
                        selectedUsers.remove(user)
                    }
                }


                root.setOnClickListener {

                    val context = root.context
                    val intent = Intent(context, ChatActivity::class.java).apply {
                        putExtra("receiverId", user.uid)
                        putExtra("receiverName", user.name)
                    }
                    context.startActivity(intent)
                }

                userCheckbox.setOnClickListener { view ->
                    view.isSelected = !view.isSelected
                    if (view.isSelected) {
                        selectedUsers.add(user)
                    } else {
                        selectedUsers.remove(user)
                    }
                    view.isSelected = !view.isSelected
                }
            }
        }
    }

    fun setUsers(newUsers: List<User>) {
        users.clear()
        users.addAll(newUsers)
        notifyDataSetChanged()
    }

    fun getSelectedUsers(): List<User> = selectedUsers.toList()

    fun clearSelection() {
        selectedUsers.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemSplitUserBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size
} 