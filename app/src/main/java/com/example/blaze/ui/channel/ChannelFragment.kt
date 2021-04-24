package com.example.blaze.ui.channel

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.blaze.R
import com.example.blaze.databinding.FragmentChannelBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.ui.avatar.AvatarView
import io.getstream.chat.android.ui.channel.list.viewmodel.ChannelListViewModel
import io.getstream.chat.android.ui.channel.list.viewmodel.bindView
import io.getstream.chat.android.ui.channel.list.viewmodel.factory.ChannelListViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ChannelFragment : Fragment(R.layout.fragment_channel) {

    private val args : ChannelFragmentArgs by navArgs()
    private var _binding : FragmentChannelBinding?= null
    private val binding get() = _binding!!

    private val client = ChatClient.instance()
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        })
    }

    @SuppressLint("WrongConstant")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentChannelBinding.bind(view)

        setupUser()
        setupChannels()
        setupDrawer()

        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(Gravity.START)
        }

        binding.channelsView.setChannelItemClickListener{ channel ->
            val action = ChannelFragmentDirections.actionChannelFragmentToChatFragment(channel.cid)
            findNavController().navigate(action)
        }
    }

    private fun setupUser(){

        if(client.getCurrentUser() == null){
            val re = Regex("[^A-Za-z0-9 ]")
            val userUid = re.replace(args.blazeUser.email,"")

            user = User(
                    id = userUid,
                    extraData = mutableMapOf(
                            "name" to args.blazeUser.username,
                            "image" to args.blazeUser.displayPhotoUrl,
                            "emailAddress" to args.blazeUser.email
                    )
            )
            val token = client.devToken(user.id)
            client.connectUser(
                user = user,
                token = token
            ).enqueue { result ->
                if (result.isSuccess) {
                    Log.d("ChannelFragment", "Success Connecting the User")
                } else {
                    Log.d("ChannelFragment", result.error().message.toString())
                }
            }
        }
    }

    private fun setupChannels(){
        val filters = Filters.and(
                Filters.eq("type", "messaging"),
                Filters.`in`("members", listOf(client.getCurrentUser()!!.id))
        )
        val viewModelFactory = ChannelListViewModelFactory(
                filters,
                ChannelListViewModel.DEFAULT_SORT
        )
        val listViewModel: ChannelListViewModel by viewModels { viewModelFactory }

        listViewModel.bindView(binding.channelsView, viewLifecycleOwner)
    }

    private fun setupDrawer(){
        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            if (menuItem.itemId == R.id.logout_menu) {
                logout()
            }
            if(menuItem.itemId == R.id.channels_menu){
                findNavController().navigate(R.id.action_channelFragment_to_usersFragment)
            }
            false
        }

        val currentUser = client.getCurrentUser()!!
        val headerView = binding.navigationView.getHeaderView(0)
        val headerAvatar = headerView.findViewById<AvatarView>(R.id.avatarView)
        headerAvatar.setUserData(currentUser)
        val headerUsername = headerView.findViewById<TextView>(R.id.username_textView)
        headerUsername.text = currentUser.name
    }

    private fun logout(){
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") { _, _ ->
            client.disconnect()
            signOutFromFirebase()
            findNavController().navigate(R.id.action_channelFragment_to_loginFragment)
        }
        builder.setNegativeButton("No") { _, _ -> }
        builder.setTitle("Logout?")
        builder.setMessage("Are you sure you want to logout?")
        builder.create().show()
    }

    private fun signOutFromFirebase(){
        GlobalScope.launch(Dispatchers.IO){
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()

            val googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

            Firebase.auth.signOut()
            googleSignInClient.signOut()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}