package com.example.musicstream

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.musicstream.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.loginBtn.setOnClickListener {
            val emailOrPhone = binding.emailPhoneEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString()

            if (emailOrPhone.isEmpty()) {
                binding.emailPhoneEditText.error = "Email or Phone required"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.passwordEditText.error = "Password required"
                return@setOnClickListener
            }

            if (Patterns.EMAIL_ADDRESS.matcher(emailOrPhone).matches()) {
                // Email login
                signInWithEmail(emailOrPhone, password)
            } else {
                // Phone login (assuming emailOrPhone is a valid phone number)
                signInWithPhoneNumber(emailOrPhone, password)
            }
        }

        binding.gotoSignupBtn.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
            finish()
        }
    }

    private fun signInWithEmail(email: String, password: String) {
        binding.progressBar.visibility = View.VISIBLE

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@LoginActivity, "Login failed: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun signInWithPhoneNumber(phoneNumber: String, password: String) {
        // Placeholder method to handle phone number login
        Toast.makeText(
            this@LoginActivity,
            "Phone number login is not implemented in this example.",
            Toast.LENGTH_SHORT
        ).show()
        // Implement phone number login logic here if required
    }
}
