package com.example.technanas.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.technanas.R
import com.example.technanas.TechNanasApp
import com.example.technanas.data.model.User
import com.example.technanas.databinding.ActivityMainBinding
import com.example.technanas.ui.announcements.AnnouncementsFragment
import com.example.technanas.ui.entrepreneur.EntrepreneursFragment
import com.example.technanas.ui.faq.FAQChatFragment
import com.example.technanas.ui.profile.ProfileFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val app by lazy { application as TechNanasApp }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // If user is not logged in, go back to LoginActivity
        if (!app.sessionManager.isLoggedIn()) {
            redirectToLogin()
            return
        }

        // Configure title based on role
        configureUiForRole()

        // Bottom navigation: 3 tabs as fragments
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_announcements -> {
                    openFragment(AnnouncementsFragment())
                    true
                }
                R.id.nav_entrepreneurs -> {
                    openFragment(EntrepreneursFragment())
                    true
                }
                R.id.nav_faq -> {
                    openFragment(FAQChatFragment())
                    true
                }
                else -> false
            }
        }

        // Default tab
        if (savedInstanceState == null) {
            binding.bottomNav.selectedItemId = R.id.nav_announcements
            openFragment(AnnouncementsFragment())
        }
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    /**
     * Set toolbar title depending on user role.
     */
    private fun configureUiForRole() {
        val role = app.sessionManager.getUserRole()
        val isAdminFlag = app.sessionManager.isAdmin()

        val resolvedRole = when {
            role != null -> role
            isAdminFlag -> User.ROLE_ADMIN
            else -> User.ROLE_ENTREPRENEUR
        }

        val titleSuffix = when (resolvedRole) {
            User.ROLE_ADMIN -> "Admin"
            User.ROLE_BUYER -> "Buyer"
            User.ROLE_ENTREPRENEUR -> "Entrepreneur"
            else -> null
        }

        supportActionBar?.title = if (titleSuffix != null) {
            "TechNanas - $titleSuffix"
        } else {
            getString(R.string.app_name)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    private fun openFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, fragment)
            .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                openFragment(ProfileFragment())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
