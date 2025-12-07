package com.example.technanas.ui.entrepreneur

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.technanas.TechNanasApp
import com.example.technanas.data.model.User
import com.example.technanas.databinding.FragmentEntrepreneursBinding
import com.example.technanas.databinding.ItemEntrepreneurBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class EntrepreneursFragment : Fragment() {

    private var _binding: FragmentEntrepreneursBinding? = null
    private val binding get() = _binding!!

    private val app by lazy { requireActivity().application as TechNanasApp }

    private lateinit var adapter: EntrepreneurAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEntrepreneursBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = EntrepreneurAdapter { user ->
            // ✅ use the constant from EntrepreneurProfileActivity
            val intent = Intent(requireContext(), EntrepreneurProfileActivity::class.java)
            intent.putExtra(EntrepreneurProfileActivity.EXTRA_OWNER_EMAIL, user.email)
            startActivity(intent)
        }

        binding.rvEntrepreneurs.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEntrepreneurs.adapter = adapter

        // 1) Firestore -> Room sync
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                app.userRepository.refreshEntrepreneursFromRemote()
            } catch (e: Exception) {
                // offline or Firestore error -> just use local cache
            }
        }

        // 2) Observe cached entrepreneurs
        viewLifecycleOwner.lifecycleScope.launch {
            app.userRepository.getEntrepreneursFlow().collectLatest { list ->
                binding.progressBar.visibility = View.GONE
                if (list.isEmpty()) {
                    binding.tvEmpty.visibility = View.VISIBLE
                    adapter.submitList(emptyList())
                } else {
                    binding.tvEmpty.visibility = View.GONE
                    adapter.submitList(list)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ---------------- Adapter ----------------

    private class EntrepreneurAdapter(
        private val onClick: (User) -> Unit
    ) : RecyclerView.Adapter<EntrepreneurAdapter.ViewHolder>() {

        private val items = mutableListOf<User>()

        fun submitList(list: List<User>) {
            items.clear()
            items.addAll(list)
            notifyDataSetChanged()
        }

        inner class ViewHolder(
            private val binding: ItemEntrepreneurBinding
        ) : RecyclerView.ViewHolder(binding.root) {

            fun bind(item: User) {
                binding.tvName.text = item.fullName
                // other labels in the card (role, phone text) can stay static from XML for now

                binding.root.setOnClickListener {
                    onClick(item)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemEntrepreneurBinding.inflate(inflater, parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size
    }
}
