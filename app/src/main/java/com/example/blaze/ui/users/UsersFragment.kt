package com.example.blaze.ui.users

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.blaze.R
import com.example.blaze.adapters.UsersAdapter
import com.example.blaze.databinding.FragmentUsersBinding
import com.google.android.material.snackbar.Snackbar
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.User

class UsersFragment : Fragment(R.layout.fragment_users) {

    private var _binding: FragmentUsersBinding? = null
    private val binding get() = _binding!!

    private val usersAdapter by lazy { UsersAdapter() }
    private val client = ChatClient.instance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentUsersBinding.bind(view)

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)

        setupToolbar()
        setupRecyclerView()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_usersFragment_to_channelFragment)
        }

        binding.searchInputView.setContinuousInputChangedListener { query ->
            if(query.isNotEmpty()){
                searchUser(query)
            }
        }

    }


    private fun setupRecyclerView() {
        binding.usersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.usersRecyclerView.adapter = usersAdapter
    }

    private fun searchUser(query: String) {
        val filters = Filters.and(
                Filters.autocomplete("name", query),
                Filters.ne("id", client.getCurrentUser()!!.id)
        )
        val request = QueryUsersRequest(
                filter = filters,
                offset = 0,
                limit = 100
        )
        client.queryUsers(request).enqueue { result ->
            if (result.isSuccess) {
                val users: List<User> = result.data()
                if(users.isEmpty()) {
                    Snackbar.make(
                        requireActivity().findViewById(R.id.navHostFragment),
                        "No results found!",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    usersAdapter.setData(emptyList())
                }
                else
                    usersAdapter.setData(users)
            } else {
                Snackbar.make(
                    requireActivity().findViewById(R.id.navHostFragment),
                    "Please check your internet connection",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}