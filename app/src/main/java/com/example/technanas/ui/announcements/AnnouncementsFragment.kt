package com.example.technanas.ui.announcements

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.technanas.TechNanasApp
import com.example.technanas.data.model.Announcement
import com.example.technanas.data.model.AnnouncementType
import com.example.technanas.databinding.FragmentAnnouncementsBinding
import com.example.technanas.ui.entrepreneur.EntrepreneurProfileActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AnnouncementsFragment : Fragment() {

    private var _binding: FragmentAnnouncementsBinding? = null
    private val binding get() = _binding!!

    private val app by lazy { requireActivity().application as TechNanasApp }

    private lateinit var adapter: AnnouncementAdapter
    private var allAnnouncements: List<Announcement> = emptyList()
    private var selectedType: AnnouncementType? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnnouncementsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ Use your existing adapter class
        adapter = AnnouncementAdapter { announcement ->
            // When the user taps an announcement, open creator profile
            openCreatorProfile(announcement)
        }

        binding.recyclerViewAnnouncements.layoutManager =
            LinearLayoutManager(requireContext())
        binding.recyclerViewAnnouncements.adapter = adapter

        // ---- Filter chips ----
        binding.chipAll.isChecked = true

        binding.chipAll.setOnClickListener {
            selectedType = null
            filterAnnouncements()
        }
        binding.chipPrice.setOnClickListener {
            selectedType = AnnouncementType.PRICE
            filterAnnouncements()
        }
        binding.chipPromo.setOnClickListener {
            selectedType = AnnouncementType.PROMOTION
            filterAnnouncements()
        }
        binding.chipEvent.setOnClickListener {
            selectedType = AnnouncementType.EVENT
            filterAnnouncements()
        }
        binding.chipTraining.setOnClickListener {
            selectedType = AnnouncementType.TRAINING
            filterAnnouncements()
        }

        // 1) Observe Room cache
        viewLifecycleOwner.lifecycleScope.launch {
            app.announcementRepository.getAllAnnouncements().collectLatest { list ->
                allAnnouncements = list
                filterAnnouncements()
            }
        }

        // 2) Sync Firestore -> Room (source of truth)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                app.announcementRepository.refreshFromRemote()
            } catch (_: Exception) {
                // offline -> keep cached data
            }
        }
    }

    private fun filterAnnouncements() {
        val filtered = if (selectedType == null) {
            allAnnouncements
        } else {
            allAnnouncements.filter { it.type == selectedType }
        }
        adapter.submitList(filtered)
    }

    /**
     * Find the ownerEmail for this announcement in Firestore
     * and open the entrepreneur / admin public profile screen.
     */
    private fun openCreatorProfile(announcement: Announcement) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val ownerEmail =
                    app.announcementRepository.findOwnerEmailFor(announcement)

                if (ownerEmail.isNullOrBlank()) {
                    Toast.makeText(
                        requireContext(),
                        "Creator info not available",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                val intent = Intent(requireContext(), EntrepreneurProfileActivity::class.java)
                intent.putExtra(EntrepreneurProfileActivity.EXTRA_OWNER_EMAIL, ownerEmail)
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    requireContext(),
                    "Failed to open profile",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
