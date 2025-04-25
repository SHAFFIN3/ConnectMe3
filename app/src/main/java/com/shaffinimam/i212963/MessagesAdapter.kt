package com.shaffinimam.i212963

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MessagesAdapter(private val messageList: MutableList<Message>) :
    RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>() {




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.message_item, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int)
    {
         val currentUserId = SharedPrefManager.getUserId(holder.itemView.context)

        val message = messageList[position]

        // Check if the current message is from the sender or receiver
        if (message.senderId == currentUserId) {
            // This message is from the sender
            holder.senderMessageLayout.visibility = View.VISIBLE
            holder.receiverMessageLayout.visibility = View.GONE
            holder.senderMessageText.text = message.message
        } else {
            // This message is from the receiver
            holder.receiverMessageLayout.visibility = View.VISIBLE
            holder.senderMessageLayout.visibility = View.GONE
            holder.receiverMessageText.text = message.message
        }
    }

    override fun getItemCount(): Int = messageList.size

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Sender's message layout and text
        val senderMessageLayout: LinearLayout = itemView.findViewById(R.id.sender_message_layout)
        val senderMessageText: TextView = itemView.findViewById(R.id.sender_message_text)

        // Receiver's message layout and text
        val receiverMessageLayout: LinearLayout = itemView.findViewById(R.id.receiver_message_layout)
        val receiverMessageText: TextView = itemView.findViewById(R.id.receiver_message_text)
    }
}
