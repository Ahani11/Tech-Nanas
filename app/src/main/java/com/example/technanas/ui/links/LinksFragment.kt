package com.example.technanas.ui.links

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.technanas.data.model.LinkCategory
import com.example.technanas.data.model.UsefulLink
import com.example.technanas.databinding.FragmentLinksBinding

class LinksFragment : Fragment() {

    private var _binding: FragmentLinksBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: LinkAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLinksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = LinkAdapter { link ->
            openLink(link.url)
        }

        binding.recyclerViewLinks.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewLinks.adapter = adapter

        val list = createLinks()
        adapter.submitList(list)
    }

    private fun createLinks(): List<UsefulLink> {
        return listOf(
            UsefulLink(
                id = 1,
                title = "TechNanas Website",
                description = "Official TechNanas site (placeholder).",
                url = "https://example.com",
                category = LinkCategory.OFFICIAL
            ),
            UsefulLink(
                id = 2,
                title = "Google",
                description = "General search.",
                url = "https://www.google.com",
                category = LinkCategory.RESOURCE
            )
        )
    }

    private fun openLink(url: String) {
        try {
            val fullUrl = if (url.startsWith("http")) url else "https://$url"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Cannot open link", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
