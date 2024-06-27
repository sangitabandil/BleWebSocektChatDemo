package com.demo.bleandwebsockethandson.bluetooth.scan

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.demo.bleandwebsockethandson.databinding.FragmentScanBleDevicesBinding
import com.demo.bleandwebsockethandson.home.MainViewModel
import com.demo.bleandwebsockethandson.utils.ErrorCode
import com.demo.bleandwebsockethandson.utils.ErrorCode.BT_DISABLED
import com.demo.bleandwebsockethandson.utils.ErrorCode.PERMISSION_DENIED
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScanBleDevicesFragment : Fragment() {

    private val parentViewModel by activityViewModels<MainViewModel>()
    private val viewModel by viewModels<ScanBleDevicesViewModel>()
    private val binding by lazy { FragmentScanBleDevicesBinding.inflate(layoutInflater) }
    private val listAdapter = ScanBleDeviceAdapter(::onItemClick)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parentViewModel.handleToolbarBack(true)
        requestBlePermissionIfNeeded()
        showLoading(true)
        initRecyclerView()
        viewModel.viewState.observe(viewLifecycleOwner, viewStateObserver)
    }

    private fun initRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = listAdapter
            isVisible = true
            binding.progress.isVisible = false
        }
    }

    private fun requestBlePermissionIfNeeded() {
        blePermission.launch(
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
        )
    }

    private fun startBluetooth() {
        enableBle.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
    }

    private fun onItemClick(bluetoothDevice: BluetoothDevice, position: Int) {
        findNavController().navigate(
            ScanBleDevicesFragmentDirections.actionBleChatFragment(bluetoothDevice)
        )
    }

    private fun showLoading(isShow: Boolean) {
        binding.progress.isVisible = isShow
        binding.recyclerView.isVisible = !isShow
    }

    private fun displayResults(scanResults: Map<String, BluetoothDevice>) {
        showLoading(false)
        listAdapter.setItems(scanResults.values)
    }

    private fun showErrorAndBack(errorCode: ErrorCode) {
        Toast.makeText(context, errorCode.msg, Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }

    private val blePermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (result.containsValue(false)) {
                showErrorAndBack(PERMISSION_DENIED)
            } else {
                startBluetooth()
            }
        }

    private val enableBle =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.setupBleScan()
            } else {
                showErrorAndBack(BT_DISABLED)
            }
        }

    private val viewStateObserver = Observer<ScanBleDeviceViewState> { state ->
        when (state) {
            is ScanBleDeviceViewState.ActiveScanBleDevice -> showLoading(true)
            is ScanBleDeviceViewState.Error -> {
                showErrorAndBack(state.errorCode)
                Log.e("TAG", "Sub Error Code: ${state.subErrorCode}")
            }

            is ScanBleDeviceViewState.ScanResultsBleDevice -> displayResults(state.scanResults)
        }
    }
}
