package com.hompimpa.comfylearn.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.databinding.ActivityRegisterBinding
import com.hompimpa.comfylearn.helper.BaseActivity
import com.hompimpa.comfylearn.ui.HomeActivity

class RegisterActivity : BaseActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        auth.setLanguageCode("en")

        binding.btnRegister.setOnClickListener {
            val email = binding.edRegisterEmail.text.toString().trim()
            val password = binding.edRegisterPassword.text.toString().trim()
            val fullName = binding.edRegisterName.text.toString().trim()

            registerUser(email, password, fullName)
        }

        binding.tvToLogin.setOnClickListener {
            startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
            finish()
        }
    }

    private fun registerUser(email: String, password: String, fullName: String) {
        if (email.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
            Toast.makeText(this, getString(R.string.all_fields_must_not_be_empty), Toast.LENGTH_SHORT).show()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, getString(R.string.invalid_email_format), Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 8) {
            Toast.makeText(this, getString(R.string.password_must_be_at_least_8_characters), Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    auth.currentUser?.let { user ->
                        val profileUpdates = userProfileChangeRequest {
                            displayName = fullName
                        }
                        user.updateProfile(profileUpdates).addOnCompleteListener { profileTask ->
                            showLoading(false)
                            if (profileTask.isSuccessful) {
                                updateUI(user)
                            } else {
                                Toast.makeText(this, "Registration succeeded, but failed to set display name.", Toast.LENGTH_LONG).show()
                                updateUI(user)
                            }
                        }
                    }
                } else {
                    showLoading(false)
                    val errorMessage = when (task.exception) {
                        is FirebaseAuthUserCollisionException -> getString(R.string.email_already_registered)
                        is FirebaseAuthWeakPasswordException -> getString(R.string.password_is_too_weak)
                        else -> getString(R.string.registration_failed)
                    }
                    Toast.makeText(baseContext, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !isLoading
        binding.edRegisterEmail.isEnabled = !isLoading
        binding.edRegisterPassword.isEnabled = !isLoading
        binding.edRegisterName.isEnabled = !isLoading
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            val intent = Intent(this@RegisterActivity, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    companion object {
        private const val TAG = "RegisterActivity"
    }
}