package com.demo.bleandwebsockethandson.bluetooth.chat

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.demo.bleandwebsockethandson.R
import com.demo.bleandwebsockethandson.bluetooth.BleChatServer
import com.demo.bleandwebsockethandson.databinding.FragmentBleChatBinding
import com.demo.bleandwebsockethandson.home.MainActivity
import com.demo.bleandwebsockethandson.home.MainViewModel
import com.demo.bleandwebsockethandson.utils.PreferenceManager
import com.demo.bleandwebsockethandson.utils.addLoadingView
import com.demo.bleandwebsockethandson.utils.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@SuppressLint("MissingPermission")
@AndroidEntryPoint
class BleChatFragment : Fragment() {

    private val binding by lazy { FragmentBleChatBinding.inflate(layoutInflater) }
    private val messageAdapter = ChatAdapter()

    private val viewModel by viewModels<BleChatViewModel>()
    private val parentViewModel by activityViewModels<MainViewModel>()

    @Inject
    lateinit var preferenceManager: PreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val safeArgs: BleChatFragmentArgs by navArgs()
        viewModel.connectDevice(safeArgs.device)
        initViews()
    }

    private fun initViews() {
        parentViewModel.handleToolbarBack(true)
        (activity as MainActivity).title = viewModel.currentDevice.name
        binding.rvMessage.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = messageAdapter
            isVisible = true
            messageAdapter.setMessages(preferenceManager.getMessageList(viewModel.currentDevice.name))
        }
        binding.sendText.doOnTextChanged { text, _, _, _ ->
            binding.ibSend.isEnabled = !text.isNullOrBlank()
        }
    }

    private fun chatWith() {
        Snackbar.make(binding.root, "Connected Successfully!", Snackbar.LENGTH_SHORT)
            .setTextColor(ContextCompat.getColor(requireContext(), R.color.green)).show()
        binding.ibSend.setOnClickListener {
            val message = binding.sendText.text.toString().trim()
            if (message.isNotEmpty()) {
                BleChatServer.sendMessage(message)
                binding.sendText.setText("")
            }
        }
    }

    private fun updateStatus(isConnecting: Boolean = false) {
        hideKeyboard()
        if (isConnecting) {
            Snackbar.make(binding.root, "Connecting!", Snackbar.LENGTH_INDEFINITE).addLoadingView()
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.disabled)).show()

        } else {
            Snackbar.make(binding.root, "Oops! Connection Lost!", Snackbar.LENGTH_INDEFINITE)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                .setAction("Try Again") { viewModel.connectDevice(viewModel.currentDevice) }.show()
        }
    }

    override fun onStart() {
        super.onStart()
        BleChatServer.connectionRequest.observe(viewLifecycleOwner, connectionRequestObserver)
        BleChatServer.deviceConnection.observe(viewLifecycleOwner, deviceConnectionObserver)
        BleChatServer.messages.observe(viewLifecycleOwner, messageObserver)
    }

    private val connectionRequestObserver = Observer<BluetoothDevice> { device ->
        BleChatServer.setCurrentChatConnection(device)
    }

    private val messageObserver =
        Observer<com.demo.bleandwebsockethandson.bluetooth.Message> { message ->
            if (message.text.isBlank())
                return@Observer
            binding.rvMessage.smoothScrollToPosition(messageAdapter.addMessage(message))
            preferenceManager.saveMessageList(
                viewModel.currentDevice.name,
                messageAdapter.getLastTenMessages()
            )
            BleChatServer.makeCurrentMsgEmpty()
        }

    private val deviceConnectionObserver = Observer<DeviceConnectionState> { state ->
        when (state) {
            is DeviceConnectionState.Connected -> chatWith()
            is DeviceConnectionState.Disconnected -> updateStatus()
        }
    }

    override fun onDestroyView() {
        (activity as MainActivity).setTitle(R.string.app_name)
        super.onDestroyView()
    }
}