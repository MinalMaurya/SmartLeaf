package com.example.smartleaf.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.smartleaf.databinding.ItemPlantBinding
import com.example.smartleaf.ui.models.Plant

class PlantAdapter(
    private var list: List<Plant>,
    private val onClick: (Plant) -> Unit
) : RecyclerView.Adapter<PlantAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemPlantBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPlantBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val plant = list[position]
        holder.binding.tvPlantName.text = plant.name
        holder.binding.root.setOnClickListener { onClick(plant) }
    }

    override fun getItemCount() = list.size

    fun updateList(newList: List<Plant>) {
        list = newList
        notifyDataSetChanged()
    }
}