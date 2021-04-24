package com.example.blaze.ui.chat

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.blaze.R
import com.example.blaze.databinding.FragmentChatBinding
import com.getstream.sdk.chat.viewmodel.MessageInputViewModel
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel
import com.google.android.material.snackbar.Snackbar
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.message.input.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.header.viewmodel.MessageListHeaderViewModel
import io.getstream.chat.android.ui.message.list.header.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.viewmodel.factory.MessageListViewModelFactory

class ChatFragment : Fragment(R.layout.fragment_chat) {

    private val args: ChatFragmentArgs by navArgs()
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentChatBinding.bind(view)

        setupMessages()

        binding.messagesHeaderView.setBackButtonClickListener{
            findNavController().navigate(R.id.action_chatFragment_to_channelFragment)
        }

        binding.messageList.setMessageFlagHandler{
            Snackbar.make(requireActivity().findViewById(R.id.navHostFragment), "Feature not available yet", Snackbar.LENGTH_LONG).show()
        }

        binding.messageList.setUserMuteHandler{
            Snackbar.make(requireActivity().findViewById(R.id.navHostFragment), "Feature not available yet", Snackbar.LENGTH_LONG).show()
        }

        binding.messageList.setUserBlockHandler{ _: User, _: String ->
            Snackbar.make(requireActivity().findViewById(R.id.navHostFragment), "Feature not available yet", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun setupMessages() {
        val factory = MessageListViewModelFactory(cid = args.channelId)

        val messageListHeaderViewModel: MessageListHeaderViewModel by viewModels { factory }
        val messageListViewModel: MessageListViewModel by viewModels { factory }
        val messageInputViewModel: MessageInputViewModel by viewModels { factory }

        messageListHeaderViewModel.bindView(binding.messagesHeaderView, viewLifecycleOwner)
        messageListViewModel.bindView(binding.messageList, viewLifecycleOwner)
        messageInputViewModel.bindView(binding.messageInputView, viewLifecycleOwner)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}