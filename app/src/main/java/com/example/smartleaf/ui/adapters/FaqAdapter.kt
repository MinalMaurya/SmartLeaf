package com.example.smartleaf.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.smartleaf.databinding.ItemFaqCardBinding
import com.example.smartleaf.ui.models.FaqItem

class FaqAdapter(
    private val items: List<FaqItem>,
    private val onClick: (FaqItem) -> Unit
) : RecyclerView.Adapter<FaqAdapter.VH>() {

    class VH(val b: ItemFaqCardBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemFaqCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.b.tvQuestion.text = item.question
        holder.b.root.setOnClickListener { onClick(item) }
    }

    override fun getItemCount(): Int = items.size
}