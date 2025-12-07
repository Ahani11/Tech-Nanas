package com.example.technanas.ui.farm

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.technanas.data.model.Farm
import com.example.technanas.databinding.ItemFarmBinding

class FarmAdapter(
    private val onClick: (Farm) -> Unit
) : ListAdapter<Farm, FarmAdapter.FarmViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<Farm>() {
        override fun areItemsTheSame(oldItem: Farm, newItem: Farm) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Farm, newItem: Farm) = oldItem == newItem
    }

    inner class FarmViewHolder(private val binding: ItemFarmBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(farm: Farm) {
            binding.tvFarmName.text = farm.name
            binding.tvFarmState.text = farm.state ?: "-"
            binding.root.setOnClickListener { onClick(farm) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FarmViewHolder {
        val binding = ItemFarmBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FarmViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FarmViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
