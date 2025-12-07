package com.example.technanas.ui.entrepreneur

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.technanas.R
import com.example.technanas.data.model.User

class EntrepreneurAdapter(
    private val onItemClick: ((User) -> Unit)? = null
) : ListAdapter<User, EntrepreneurAdapter.ViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean =
            oldItem == newItem
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvBusinessType: TextView = itemView.findViewById(R.id.tvBusinessType)
        private val tvContact: TextView = itemView.findViewById(R.id.tvContact)

        fun bind(user: User) {
            tvName.text = user.fullName

            val businessType = when (user.subRole) {
                "FARMER" -> "Farmer"
                "WHOLESALER" -> "Wholesaler"
                "RETAILER" -> "Retailer"
                else -> "Entrepreneur"
            }
            tvBusinessType.text = businessType

            val contact = if (user.phone.isNotBlank()) {
                user.phone
            } else {
                user.email
            }
            tvContact.text = contact

            itemView.setOnClickListener {
                onItemClick?.invoke(user)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_entrepreneur, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
