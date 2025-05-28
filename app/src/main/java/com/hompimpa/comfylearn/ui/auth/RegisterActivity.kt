package com.hompimpa.comfylearn.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

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
            Toast.makeText(
                this,
                getString(R.string.all_fields_must_not_be_empty), Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(
                this,
                getString(R.string.invalid_email_format), Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (password.length < 8) {
            Toast.makeText(
                this,
                getString(R.string.password_must_be_at_least_6_characters), Toast.LENGTH_SHORT
            ).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    auth.currentUser?.let { user ->
                        user.updateProfile(userProfileChangeRequest {
                            displayName = fullName
                        }).addOnCompleteListener { profileTask ->
                            if (profileTask.isSuccessful) {
                                updateUI(user)
                            } else {
                                handleError(
                                    "Registration succeeded, but name update failed.",
                                    profileTask.exception
                                )
                                updateUI(user)
                            }
                        }
                    } ?: run {
                        handleError("User  creation succeeded, but user object is null.")
                    }
                } else {
                    handleError(
                        "Authentication failed: ${task.exception?.localizedMessage}",
                        task.exception
                    )
                    updateUI(null)
                }
            }
    }

    private fun handleError(message: String, exception: Exception? = null) {
        Log.w(TAG, message, exception)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
            finish()
        }
    }

    companion object {
        private const val TAG = "RegisterActivity"
    }
}