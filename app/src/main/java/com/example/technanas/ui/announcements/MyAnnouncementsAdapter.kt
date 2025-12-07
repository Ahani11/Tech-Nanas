package com.example.technanas.ui.announcements

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.technanas.R
import com.example.technanas.data.model.Announcement
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MyAnnouncementsAdapter :
    ListAdapter<Announcement, MyAnnouncementsAdapter.MyAnnouncementViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<Announcement>() {
        override fun areItemsTheSame(oldItem: Announcement, newItem: Announcement): Boolean {
            // We don't have stable IDs from Firestore, so compare by content + date
            return oldItem.title == newItem.title &&
                    oldItem.dateMillis == newItem.dateMillis
        }

        override fun areContentsTheSame(oldItem: Announcement, newItem: Announcement): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyAnnouncementViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_announcement, parent, false)
        return MyAnnouncementViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyAnnouncementViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MyAnnouncementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvMeta: TextView = itemView.findViewById(R.id.tvMeta)

        private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        fun bind(announcement: Announcement) {
            tvTitle.text = announcement.title

            val typeText = announcement.type.name.lowercase().replaceFirstChar {
                it.uppercase()
            }
            val dateText = dateFormat.format(Date(announcement.dateMillis))

            tvMeta.text = "$typeText • $dateText"
        }
    }
}
