package com.example.technanas.ui.profile

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.technanas.R
import com.example.technanas.TechNanasApp
import com.example.technanas.databinding.FragmentProfileBinding
import com.example.technanas.ui.LoginActivity
import com.example.technanas.ui.announcements.MyAnnouncementsActivity
import com.example.technanas.ui.farm.MyFarmsActivity
import com.example.technanas.ui.profile.SettingsActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val app by lazy { requireActivity().application as TechNanasApp }

    // shared prefs for avatar + theme
    private val prefs by lazy {
        requireContext().getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
    }

    // pick from gallery
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                val storedUri = copyUriToInternalStorage(it)
                if (storedUri != null) {
                    binding.ivAvatar.setImageURI(storedUri)
                    saveAvatarForCurrentUser(storedUri.toString(), source = "file")
                } else {
                    Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

    // camera
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if (bitmap != null) {
                val storedUri = saveBitmapToInternalStorage(bitmap)
                binding.ivAvatar.setImageURI(storedUri)
                saveAvatarForCurrentUser(storedUri.toString(), source = "file")
            }
        }

    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                cameraLauncher.launch(null)
            } else {
                Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = app.sessionManager.getUserId()
        if (userId == null) {
            Toast.makeText(requireContext(), "No user logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val isAdmin = app.sessionManager.isAdmin()
        if (isAdmin) {
            binding.layoutFarmDetails.visibility = View.GONE
            binding.layoutMyAnnouncements.visibility = View.GONE
            binding.layoutSocialLinks.visibility = View.GONE
        } else {
            binding.layoutFarmDetails.visibility = View.VISIBLE
            binding.layoutMyAnnouncements.visibility = View.VISIBLE
            binding.layoutSocialLinks.visibility = View.VISIBLE
        }

        // Refresh profile data from Firestore -> Room (by email)
        val userEmail = app.sessionManager.getUserEmail()
        if (!userEmail.isNullOrEmpty()) {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    app.userRepository.refreshFromRemote(userEmail)
                } catch (_: Exception) {
                }
            }
        }

        // Listen to user from Room
        viewLifecycleOwner.lifecycleScope.launch {
            app.userRepository.getUserByIdFlow(userId).collectLatest { user ->
                if (user != null) {
                    binding.tvName.text = user.fullName
                    binding.tvEmail.text = user.email
                }
            }
        }

        // avatar
        loadAvatarForCurrentUser()
        binding.ivAvatar.setOnClickListener {
            showAvatarSourceDialog()
        }

        // Account details
        binding.layoutAccountDetails.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent)
        }

        // My farms & stores
        binding.layoutFarmDetails.setOnClickListener {
            val intent = Intent(requireContext(), MyFarmsActivity::class.java)
            startActivity(intent)
        }

        // My announcements (both admin + entrepreneurs can see their own posts)
        binding.layoutMyAnnouncements.setOnClickListener {
            val intent = Intent(requireContext(), MyAnnouncementsActivity::class.java)
            startActivity(intent)
        }

        // Settings
        binding.layoutSettings.setOnClickListener {
            val intent = Intent(requireContext(), SettingsActivity::class.java)
            startActivity(intent)
        }

        // Logout
        binding.layoutLogout.setOnClickListener {
            app.sessionManager.logout()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }

        binding.layoutSocialLinks.setOnClickListener {
            val intent = Intent(requireContext(), EditSocialLinksActivity::class.java)
            startActivity(intent)
        }
    }

    // =================== AVATAR UI ===================

    private fun showAvatarSourceDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_avatar_picker, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val opt1 = dialogView.findViewById<ImageView>(R.id.ivAvatarOption1)
        val opt2 = dialogView.findViewById<ImageView>(R.id.ivAvatarOption2)
        val opt3 = dialogView.findViewById<ImageView>(R.id.ivAvatarOption3)

        opt1.setOnClickListener {
            binding.ivAvatar.setImageResource(R.drawable.user)
            saveAvatarForCurrentUser("default_1", source = "default")
            dialog.dismiss()
        }
        opt2.setOnClickListener {
            binding.ivAvatar.setImageResource(R.drawable.user2)
            saveAvatarForCurrentUser("default_2", source = "default")
            dialog.dismiss()
        }
        opt3.setOnClickListener {
            binding.ivAvatar.setImageResource(R.drawable.people)
            saveAvatarForCurrentUser("default_3", source = "default")
            dialog.dismiss()
        }

        val tvGallery = dialogView.findViewById<TextView>(R.id.tvChooseGallery)
        val tvCamera = dialogView.findViewById<TextView>(R.id.tvTakePhoto)

        tvGallery.setOnClickListener {
            dialog.dismiss()
            pickImageLauncher.launch("image/*")
        }

        tvCamera.setOnClickListener {
            dialog.dismiss()
            handleCameraClick()
        }

        dialog.show()
    }

    private fun handleCameraClick() {
        val permission = Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(requireContext(), permission)
            == PackageManager.PERMISSION_GRANTED
        ) {
            cameraLauncher.launch(null)
        } else {
            requestCameraPermission.launch(permission)
        }
    }

    // =================== AVATAR PERSISTENCE ===================

    private fun saveAvatarForCurrentUser(path: String, source: String) {
        val userId = app.sessionManager.getUserId() ?: return

        prefs.edit()
            .putString("avatar_path_user_$userId", path)
            .putString("avatar_source_user_$userId", source)
            .apply()
    }

    private fun loadAvatarForCurrentUser() {
        val userId = app.sessionManager.getUserId() ?: run {
            binding.ivAvatar.setImageResource(R.mipmap.ic_launcher_round)
            return
        }

        val path = prefs.getString("avatar_path_user_$userId", null)
        val source = prefs.getString("avatar_source_user_$userId", null)

        if (path == null || source == null) {
            binding.ivAvatar.setImageResource(R.mipmap.ic_launcher_round)
            return
        }

        when (source) {
            "default" -> {
                when (path) {
                    "default_1" -> binding.ivAvatar.setImageResource(R.drawable.user)
                    "default_2" -> binding.ivAvatar.setImageResource(R.drawable.user2)
                    "default_3" -> binding.ivAvatar.setImageResource(R.drawable.people)
                    else -> binding.ivAvatar.setImageResource(R.mipmap.ic_launcher_round)
                }
            }
            "file" -> {
                val uri = Uri.parse(path)
                binding.ivAvatar.setImageURI(uri)
            }
            else -> binding.ivAvatar.setImageResource(R.mipmap.ic_launcher_round)
        }
    }

    private fun copyUriToInternalStorage(sourceUri: Uri): Uri? {
        return try {
            val inputStream: InputStream? =
                requireContext().contentResolver.openInputStream(sourceUri)
            if (inputStream != null) {
                val fileName = "avatar_${System.currentTimeMillis()}.jpg"
                val file = File(requireContext().filesDir, fileName)
                FileOutputStream(file).use { out ->
                    inputStream.copyTo(out)
                }
                inputStream.close()
                Uri.fromFile(file)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun saveBitmapToInternalStorage(bitmap: Bitmap): Uri {
        val fileName = "avatar_${System.currentTimeMillis()}.jpg"
        val file = File(requireContext().filesDir, fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        return Uri.fromFile(file)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
