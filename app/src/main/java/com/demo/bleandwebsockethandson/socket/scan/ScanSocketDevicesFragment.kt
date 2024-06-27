package com.demo.bleandwebsockethandson.socket.scan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.demo.bleandwebsockethandson.databinding.FragmentScanSocketDevicesBinding
import com.demo.bleandwebsockethandson.home.MainViewModel
import com.demo.bleandwebsockethandson.utils.ErrorCode
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScanSocketDevicesFragment : Fragment() {

    private val binding by lazy { FragmentScanSocketDevicesBinding.inflate(layoutInflater) }
    private val scanSocketDeviceAdapter = ScanSocketDeviceAdapter(::onItemClick)
    private val parentViewModel by activityViewModels<MainViewModel>()
    private val viewModel by viewModels<ScanSocketDeviceViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.viewState.observe(viewLifecycleOwner, viewStateObserver)
        parentViewModel.handleToolbarBack(true)
        initListeners()
        initRecyclerView()
    }

    private fun initListeners() = binding.apply {
        btnSearch.setOnClickListener {
            viewModel.searchDevices()
        }
    }

    private fun initRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = scanSocketDeviceAdapter
            isVisible = true
            binding.progress.isVisible = false
        }
    }

    private fun showErrorAndBack(errorCode: ErrorCode, errorMsg: String?) {
        Toast.makeText(context, getString(errorCode.msg) + "\n" + errorMsg, Toast.LENGTH_SHORT)
            .show()
        findNavController().navigateUp()
    }

    private fun displayDevices(scanResults: MutableList<String>) {
        binding.btnSearch.isEnabled = true
        scanSocketDeviceAdapter.setItems(scanResults)
    }

    private fun showProgressBar(isShow: Boolean) {
        binding.btnSearch.isEnabled = false
        binding.progress.isVisible = isShow
        binding.recyclerView.isVisible = !isShow
    }

    private val viewStateObserver = Observer<ScanSocketDeviceViewState> {
        when (it) {
            is ScanSocketDeviceViewState.Error -> {
                showProgressBar(false)
                showErrorAndBack(it.errorCode, it.errorMsg)
            }

            is ScanSocketDeviceViewState.ScanResultsDevice -> {
                showProgressBar(false)
                displayDevices(it.scanResults)
            }

            is ScanSocketDeviceViewState.ScanStart -> showProgressBar(true)
            is ScanSocketDeviceViewState.GotDetails -> navigateToChat(it.name, it.ipAddress)
        }
    }

    private fun onItemClick(ipAddress: String) {
        viewModel.fetchNameFromIp(ipAddress)
    }

    private fun navigateToChat(name: String, ipAddress: String) {
        findNavController().navigate(
            ScanSocketDevicesFragmentDirections.actionSocketChatFragment(ipAddress, name)
        )
    }
}