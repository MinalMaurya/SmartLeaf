package com.example.smartleaf.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.smartleaf.R
import com.example.smartleaf.databinding.ItemCategoryBinding
import com.example.smartleaf.ui.models.Category

class CategoryAdapter(
    private var list: List<Category>,
    private val onClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.VH>() {

    inner class VH(val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]

        holder.binding.tvCategoryName.text = item.name

        // ✅ Default icon (used when no specific icon exists)
        val defaultIcon = R.drawable.ic_leaf_logo

        // ✅ Use specific icon only if you have it, otherwise fallback
        val iconRes = when (item.id) {
            "c3" -> R.drawable.veg      // Vegetables (only if you have this)
            "c4" -> R.drawable.fruit    // Fruits (only if you have this)
            else -> defaultIcon
        }

        holder.binding.imgCategoryIcon.setImageResource(iconRes)

        holder.binding.root.setOnClickListener { onClick(item) }
    }

    override fun getItemCount(): Int = list.size

    fun updateList(newList: List<Category>) {
        list = newList
        notifyDataSetChanged()
    }
}