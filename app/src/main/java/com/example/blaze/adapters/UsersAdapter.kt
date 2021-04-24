package com.example.blaze.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.blaze.databinding.UsersRowItemBinding
import com.example.blaze.ui.users.UsersFragmentDirections
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.name

class UsersAdapter : RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    private val client = ChatClient.instance()
    private var userList = emptyList<User>()

    class UserViewHolder(val binding: UsersRowItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder(
                UsersRowItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                )
        )
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser = userList[position]

        holder.binding.avatarImageView.setUserData(currentUser)
        holder.binding.emailTextView.text = currentUser.extraData["emailAddress"].toString()
        holder.binding.usernameTextView.text = currentUser.name
        holder.binding.rootLayout.setOnClickListener {
            val builder = AlertDialog.Builder(holder.itemView.context)
            builder.setPositiveButton("Yes") { _, _ ->
                createNewChannel(currentUser.id, holder)
            }
            builder.setNegativeButton("No") { _, _ -> }
            builder.setTitle("Start conversation with ${currentUser.name}?")
            builder.create().show()
        }
    }

    fun setData(newList: List<User>) {
        userList = newList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    private fun createNewChannel(selectedUser: String, holder: UserViewHolder) {
        client.createChannel(
                channelType = "messaging",
                members = listOf(client.getCurrentUser()!!.id, selectedUser)
        ).enqueue { result ->
            if (result.isSuccess) {
                navigateToChatFragment(holder, result.data().cid)
            } else {
                Log.e("UsersAdapter", result.error().message.toString())
            }
        }
    }

    private fun navigateToChatFragment(holder: UserViewHolder, cid: String) {
        val action = UsersFragmentDirections.actionUsersFragmentToChatFragment(cid)
        holder.itemView.findNavController().navigate(action)
    }

}