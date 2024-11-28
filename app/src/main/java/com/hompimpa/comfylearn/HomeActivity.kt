package com.hompimpa.comfylearn

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hompimpa.comfylearn.databinding.ActivityHomeBinding
import com.hompimpa.comfylearn.helper.MainViewModel
import com.hompimpa.comfylearn.helper.ViewModelFactory
import com.hompimpa.comfylearn.ui.settings.SettingPreferences
import com.hompimpa.comfylearn.ui.settings.SettingsActivity
import com.hompimpa.comfylearn.ui.settings.dataStore

class HomeActivity : AppCompatActivity() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityHomeBinding
    private lateinit var auth: FirebaseAuth
    private var shouldRecreate = false // Flag to track if recreation is needed

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarHome.toolbar)

        // Initialize the ViewModel
        val pref = SettingPreferences.getInstance(dataStore)
        mainViewModel = ViewModelProvider(this, ViewModelFactory(pref))[MainViewModel::class.java]
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_home)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_study,
                R.id.nav_games,
                R.id.nav_learnprog,
                R.id.nav_settings
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        auth = Firebase.auth
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        } else {
            setupUserHeader(navView)
        }

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivityForResult(intent, SETTINGS_REQUEST_CODE)
                    true
                }

                else -> {
                    NavigationUI.onNavDestinationSelected(menuItem, navController)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
            }
        }
    }

    private fun setupUserHeader(navView: NavigationView) {
        val headerView: View = navView.getHeaderView(0)
        val userNameTextView: TextView = headerView.findViewById(R.id.name_text)
        val userEmailTextView: TextView = headerView.findViewById(R.id.email_text)
        val userProfileImageView: ImageView = headerView.findViewById(R.id.imageView)

        val firebaseUser = auth.currentUser
        userNameTextView.text = firebaseUser?.displayName ?: "No name available"
        userEmailTextView.text = firebaseUser?.email ?: "No email available"

        firebaseUser?.photoUrl?.let { photoUrl ->
            Glide.with(this)
                .load(photoUrl)
                .placeholder(R.mipmap.ic_launcher_round)
                .into(userProfileImageView)
        } ?: userProfileImageView.setImageResource(R.mipmap.ic_launcher_round)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_home)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onResume() {
        super.onResume()
        if (shouldRecreate) {
            shouldRecreate = false
            recreate() // Recreate the activity if the flag is set
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SETTINGS_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            shouldRecreate = true // Set the flag to recreate the activity on resume
        }
    }

    companion object {
        const val SETTINGS_REQUEST_CODE = 1
    }
}
