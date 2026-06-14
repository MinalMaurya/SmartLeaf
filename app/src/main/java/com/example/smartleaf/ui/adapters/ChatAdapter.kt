package com.example.smartleaf.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.smartleaf.databinding.ItemChatMessageBinding
import com.example.smartleaf.ui.models.ChatMessage

class ChatAdapter(
    private val items: MutableList<ChatMessage>,
    private val onBubbleTap: (ChatMessage) -> Unit   // ✅ returns ChatMessage like you use in Activity
) : RecyclerView.Adapter<ChatAdapter.VH>() {

    inner class VH(val b: ItemChatMessageBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemChatMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val msg = items[position]
        val b = holder.b

        if (msg.isUser) {
            b.tvUser.visibility = View.VISIBLE
            b.botContainer.visibility = View.GONE
            b.tvUser.text = msg.text
        } else {
            b.tvUser.visibility = View.GONE
            b.botContainer.visibility = View.VISIBLE
            b.tvBot.text = msg.text

            // ✅ Tap speaker icon OR the whole bot card
            b.btnSpeak.setOnClickListener { onBubbleTap(msg) }
            b.botContainer.setOnClickListener { onBubbleTap(msg) }
        }
    }

    override fun getItemCount(): Int = items.size

    // ✅ Your activity calls adapter.add(...)
    fun add(message: ChatMessage) {
        items.add(message)
        notifyItemInserted(items.size - 1)
    }
}