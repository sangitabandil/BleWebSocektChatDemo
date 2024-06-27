package com.demo.bleandwebsockethandson.bluetooth.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.demo.bleandwebsockethandson.bluetooth.Message
import com.demo.bleandwebsockethandson.bluetooth.MessageType
import com.demo.bleandwebsockethandson.databinding.ItemRecievedMessageBinding
import com.demo.bleandwebsockethandson.databinding.ItemSendMessageBinding
import java.io.InvalidClassException


private const val RECEIVE_MESSAGE = 0
private const val SEND_MESSAGE = 1

class ChatAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var messages = mutableListOf<Message>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        LayoutInflater.from(parent.context).let {
            return when (viewType) {
                SEND_MESSAGE -> SendViewHolder(ItemSendMessageBinding.inflate(it, parent, false))
                else -> ReceiveViewHolder(ItemRecievedMessageBinding.inflate(it, parent, false))
            }
        }

    override fun getItemViewType(position: Int) = when (messages[position].type) {
        MessageType.INCOMING -> RECEIVE_MESSAGE
        MessageType.OUTGOING -> SEND_MESSAGE
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
        when (holder) {
            is SendViewHolder -> {
                holder.bind(messages[position])
            }

            is ReceiveViewHolder -> {
                holder.bind(messages[position])
            }

            else -> throw InvalidClassException("Unknown View Holder")
        }

    override fun getItemCount() = messages.size

    fun addMessage(message: Message): Int {
        val insertedPosition = messages.size
        messages.add(insertedPosition, message)
        notifyItemInserted(insertedPosition)
        return insertedPosition
    }

    fun setMessages(list: MutableList<Message>) {
        messages = list
        notifyDataSetChanged()
    }

    fun getLastTenMessages() = messages.takeLast(10)

    inner class SendViewHolder(private val binding: ItemSendMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.tvMessage.text = message.text
        }
    }

    inner class ReceiveViewHolder(private val binding: ItemRecievedMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.tvMessage.text = message.text
        }
    }

}