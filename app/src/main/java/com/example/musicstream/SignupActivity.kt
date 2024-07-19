package com.example.musicstream

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.musicstream.databinding.ActivitySignupBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var storedVerificationId: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.createAccountBtn.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val phoneNumber = binding.phoneEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString()
            val confirmPassword = binding.confirmPasswordEditText.text.toString()

            if (email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.emailEditText.error = "Invalid email address"
                return@setOnClickListener
            }

            if (phoneNumber.isEmpty() || !isValidPhoneNumber(phoneNumber)) {
                binding.phoneEditText.error = "Please enter a valid phone number"
                return@setOnClickListener
            }

            if (password.length < 6) {
                binding.passwordEditText.error = "Password should be at least 6 characters"
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                binding.confirmPasswordEditText.error = "Passwords don't match"
                return@setOnClickListener
            }

            if (auth.currentUser != null) {
                Toast.makeText(this, "You are already signed in!", Toast.LENGTH_SHORT).show()
                // Navigate to the next activity or perform other actions
            } else {
                startPhoneNumberVerification(phoneNumber)
            }
        }

        binding.gotoLoginBtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        setInProgress(true)
        val phoneAuthProvider = PhoneAuthProvider.getInstance()
        phoneAuthProvider.verifyPhoneNumber(
            "+91$phoneNumber", // Assuming Indian numbers, update if needed
            60, // Timeout duration in seconds
            TimeUnit.SECONDS,
            this,
            object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Auto verification might occur here, but unlikely in this scenario
                    // ...
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    setInProgress(false)
                    Log.e("SignupActivity", "Verification failed", e)
                    Toast.makeText(
                        applicationContext,
                        "Verification failed: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    setInProgress(false)
                    storedVerificationId = verificationId
                    resendToken = token
                    startActivity(
                        Intent(this@SignupActivity, OtpVerificationActivity::class.java)
                            .putExtra("verificationId", storedVerificationId)
                            .putExtra("phoneNumber", phoneNumber)
                            .putExtra("password", binding.passwordEditText.text.toString())
                            .putExtra("resendToken", resendToken)
                    )
                }
            }
        )
    }

    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        val phonePattern = "^[6-9]\\d{9}$" // Indian phone number validation pattern
        return phoneNumber.matches(Regex(phonePattern))
    }

    private fun setInProgress(inProgress: Boolean) {
        if (inProgress) {
            binding.createAccountBtn.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.createAccountBtn.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
        }
    }
}
