package com.demo.bleandwebsockethandson.bluetooth.scan

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.demo.bleandwebsockethandson.databinding.ItemDeviceBinding

@SuppressLint("MissingPermission")
class ScanBleDeviceAdapter(val onItemClick: (BluetoothDevice, Int) -> Unit) :
    RecyclerView.Adapter<ScanBleDeviceAdapter.ViewHolder>() {
    private var items = listOf<BluetoothDevice>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            ItemDeviceBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    fun setItems(values: Collection<BluetoothDevice>) {
        items = values.toList()
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemDeviceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(device: BluetoothDevice) {
            binding.tvDeviceName.text = device.name
            binding.tvDeviceAddress.text = device.address
            itemView.setOnClickListener {
                onItemClick.invoke(device, adapterPosition)
            }
        }

    }

}