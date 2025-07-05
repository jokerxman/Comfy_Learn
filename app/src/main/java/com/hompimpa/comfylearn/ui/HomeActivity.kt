package com.hompimpa.comfylearn.ui

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
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
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.databinding.ActivityHomeBinding
import com.hompimpa.comfylearn.helper.BaseActivity
import com.hompimpa.comfylearn.helper.SettingPreferences
import com.hompimpa.comfylearn.helper.dataStore
import com.hompimpa.comfylearn.ui.auth.LoginActivity
import com.hompimpa.comfylearn.ui.learnProg.LearnProgFragment
import com.hompimpa.comfylearn.ui.settings.SettingsActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Locale

class HomeActivity : BaseActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityHomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var settingsLauncher: ActivityResultLauncher<Intent>

    override fun attachBaseContext(newBase: Context) {
        val prefs = SettingPreferences.getInstance(newBase.dataStore)
        val languageCode = runBlocking { prefs.getLanguageSetting().first() }

        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val configuration = Configuration(newBase.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        val context = newBase.createConfigurationContext(configuration)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarHome.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_home)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_study,
                R.id.nav_games,
                R.id.nav_learnprog
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

        settingsLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                recreate()
            }
        }

        navView.setNavigationItemSelectedListener { menuItem ->
            val handled: Boolean = when (menuItem.itemId) {
                R.id.nav_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    settingsLauncher.launch(intent)
                    true
                }
                else -> {
                    NavigationUI.onNavDestinationSelected(menuItem, navController)
                }
            }
            if (handled) {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
            handled
        }
    }

    private fun setupUserHeader(navView: NavigationView) {
        val headerView: View = navView.getHeaderView(0)
        val userNameTextView: TextView = headerView.findViewById(R.id.name_text)
        val userEmailTextView: TextView = headerView.findViewById(R.id.email_text)
        val userProfileImageView: ImageView = headerView.findViewById(R.id.imageView)

        val firebaseUser = auth.currentUser
        userNameTextView.text = firebaseUser?.displayName ?: getString(R.string.no_name)
        userEmailTextView.text = firebaseUser?.email ?: getString(R.string.no_email)

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
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data) // Keep this!
        Log.d("HomeActivity_DEBUG", "onActivityResult in HomeActivity: requestCode=$requestCode, resultCode=$resultCode, data present: ${data != null}")
        if (requestCode == LearnProgFragment.FILL_IN_GAME_REQUEST_CODE) {
            Log.d("HomeActivity_DEBUG", "HomeActivity received result for FILL_IN_GAME_REQUEST_CODE.")
        }
    }
}