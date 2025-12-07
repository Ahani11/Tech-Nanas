package com.example.technanas.ui.announcements

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.technanas.data.model.Announcement
import com.example.technanas.databinding.ItemAnnouncementBinding
import com.example.technanas.util.DateUtils

class AnnouncementAdapter(
    private val onClick: (Announcement) -> Unit
) : RecyclerView.Adapter<AnnouncementAdapter.AnnouncementViewHolder>() {

    private val items = mutableListOf<Announcement>()

    fun submitList(list: List<Announcement>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class AnnouncementViewHolder(
        private val binding: ItemAnnouncementBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Announcement) {
            binding.tvTitle.text = item.title
            binding.tvShortDescription.text = item.shortDescription
            binding.tvDate.text = DateUtils.formatDate(item.dateMillis)

            binding.root.setOnClickListener {
                onClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnnouncementViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemAnnouncementBinding.inflate(inflater, parent, false)
        return AnnouncementViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: AnnouncementViewHolder, position: Int) {
        holder.bind(items[position])
    }
}
