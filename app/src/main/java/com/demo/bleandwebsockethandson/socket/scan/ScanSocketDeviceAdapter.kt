package com.demo.bleandwebsockethandson.socket.scan

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.demo.bleandwebsockethandson.databinding.ItemScanSocketDeviceBinding

@SuppressLint("MissingPermission")
class ScanSocketDeviceAdapter(val onItemClick: (String) -> Unit) :
    RecyclerView.Adapter<ScanSocketDeviceAdapter.ViewHolder>() {

    private var items = listOf<String>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            ItemScanSocketDeviceBinding.inflate(
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

    fun setItems(scanResults: MutableList<String>) {
        items = scanResults
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemScanSocketDeviceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(result: String) {
            binding.tvDeviceName.text = result
            itemView.setOnClickListener {
                onItemClick.invoke(result)
            }
        }

    }

}