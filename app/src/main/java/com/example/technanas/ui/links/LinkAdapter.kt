package com.example.technanas.ui.links

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.technanas.data.model.UsefulLink
import com.example.technanas.databinding.ItemLinkBinding

class LinkAdapter(
    private val onClick: (UsefulLink) -> Unit
) : RecyclerView.Adapter<LinkAdapter.LinkViewHolder>() {

    private val items = mutableListOf<UsefulLink>()

    fun submitList(list: List<UsefulLink>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class LinkViewHolder(
        private val binding: ItemLinkBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: UsefulLink) {
            binding.tvTitle.text = item.title
            binding.tvDescription.text = item.description
            binding.tvUrl.text = item.url

            binding.root.setOnClickListener {
                onClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinkViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemLinkBinding.inflate(inflater, parent, false)
        return LinkViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: LinkViewHolder, position: Int) {
        holder.bind(items[position])
    }
}
