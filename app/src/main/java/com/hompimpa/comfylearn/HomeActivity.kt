package com.hompimpa.comfylearn

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hompimpa.comfylearn.databinding.ActivityHomeBinding
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityHomeBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize binding first
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarHome.toolbar)

        // Now you can access the binding properties
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_home)

        // Setup action bar and navigation controller
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_study, R.id.nav_games, R.id.nav_learnprog, R.id.nav_settings
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        auth = Firebase.auth
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            // Not signed in, launch the Login activity
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        } else {
            // User is already signed in
            val name = firebaseUser.displayName // Get the user's display name
            val email = firebaseUser.email // Get the user's email
            val photoUrl = firebaseUser.photoUrl // Get the user's profile image

            // Get the header view (nav_header_home layout)
            val headerView: View = navView.getHeaderView(0)

            // Find the TextView in the header layout
            val userNameTextView: TextView = headerView.findViewById(R.id.name_text)
            val userEmailTextView: TextView = headerView.findViewById(R.id.email_text)
            val userProfileImageView: ImageView = headerView.findViewById(R.id.imageView)

            // Set the name and email to the TextViews in the header
            userNameTextView.text = name ?: "No name available"
            userEmailTextView.text = email ?: "No email available"

            // Load the profile picture into the ImageView using Glide
            if (photoUrl != null) {
                Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.mipmap.ic_launcher_round) // You can provide a placeholder image
                    .into(userProfileImageView)
            } else {
                // If no profile picture is available, set a default image
                userProfileImageView.setImageResource(R.mipmap.ic_launcher_round)
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sign_out_menu -> {
                signOut()
                true
            }

            R.id.action_settings -> {
                // Use NavController to navigate to SettingsFragment
                val navController = findNavController(R.id.nav_host_fragment_content_home)
                navController.navigate(R.id.nav_settings) // Navigates to SettingsFragment
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_home)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun signOut() {
        lifecycleScope.launch {
            val credentialManager = CredentialManager.create(this@HomeActivity)
            auth.signOut()
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
            finish()
        }
    }
}