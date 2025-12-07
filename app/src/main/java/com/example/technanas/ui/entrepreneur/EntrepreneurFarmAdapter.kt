package com.example.technanas.ui.entrepreneur

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.technanas.R
import com.example.technanas.data.model.Farm

class EntrepreneurFarmAdapter :
    ListAdapter<Farm, EntrepreneurFarmAdapter.ViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<Farm>() {
        override fun areItemsTheSame(oldItem: Farm, newItem: Farm): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Farm, newItem: Farm): Boolean =
            oldItem == newItem
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvFarmName: TextView = itemView.findViewById(R.id.tvFarmName)
        private val tvFarmAddress: TextView = itemView.findViewById(R.id.tvFarmAddress)
        private val tvFarmState: TextView = itemView.findViewById(R.id.tvFarmState)

        fun bind(farm: Farm) {
            tvFarmName.text = farm.name.ifBlank { "Farm / Store" }

            val address = buildString {
                if (!farm.address.isNullOrBlank()) append(farm.address)
                if (!farm.state.isNullOrBlank()) {
                    if (isNotEmpty()) append(", ")
                    append(farm.state)
                }
            }
            tvFarmAddress.text = if (address.isNotBlank()) address else "Address not set"

            tvFarmState.text = farm.state?.ifBlank { "" }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_entrepreneur_farm, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
