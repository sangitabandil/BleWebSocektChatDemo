package com.demo.bleandwebsockethandson.socket.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.demo.bleandwebsockethandson.R
import com.demo.bleandwebsockethandson.bluetooth.Message
import com.demo.bleandwebsockethandson.bluetooth.chat.ChatAdapter
import com.demo.bleandwebsockethandson.databinding.FragmentSocketChatBinding
import com.demo.bleandwebsockethandson.home.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SocketChatFragment : Fragment() {

    private val binding by lazy { FragmentSocketChatBinding.inflate(layoutInflater) }
    private val parentViewModel by activityViewModels<MainViewModel>()
    private val viewModel by viewModels<SocketChatViewModel>()
    private val messageAdapter = ChatAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.viewState.observe(viewLifecycleOwner, viewStateObserver)
        val safeArgs: SocketChatFragmentArgs by navArgs()
        viewModel.handleArgs(safeArgs.ipAddress, safeArgs.name)
        initViews()
        initListeners()
    }

    private fun initViews() {
        parentViewModel.handleToolbarBack(true)
        binding.rvMessage.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = messageAdapter
            isVisible = true
            messageAdapter.setMessages(viewModel.getMessages())
        }
    }

    private fun initListeners() = binding.apply {
        ibSend.setOnClickListener {
            viewModel.sendMessage(sendText.text.toString())
            sendText.setText("")
        }

        sendText.doOnTextChanged { text, _, _, _ ->
            ibSend.isEnabled = !text.isNullOrBlank()
        }
    }

    private val viewStateObserver = Observer<SocketChatViewState> {
        when (it) {
            is SocketChatViewState.UpdateTitle -> setActivityTitle(it.name)
            is SocketChatViewState.AddMessage -> updateMessageRecyclerView(it.msg)
            is SocketChatViewState.SetOfflineMode -> setOfflineUI(it.offline)
        }
    }

    private fun updateMessageRecyclerView(msg: Message) {
        if (msg.text.isBlank())
            return
        binding.rvMessage.smoothScrollToPosition(messageAdapter.addMessage(msg))
        viewModel.saveMessageList(messageAdapter.getLastTenMessages())
    }

    private fun setOfflineUI(offline: Boolean) {
        // Show That user is offline && make send disabled
    }

    private fun setActivityTitle(name: String) {
        activity?.title = name
    }

    override fun onDestroy() {
        setActivityTitle(getString(R.string.app_name))
        super.onDestroy()
    }
}