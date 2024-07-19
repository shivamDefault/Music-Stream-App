package com.example.musicstream

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.musicstream.databinding.ActivityOtpVerificationBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class OtpVerificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtpVerificationBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var storedVerificationId: String
    private lateinit var phoneNumber: String
    private lateinit var password: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private var resendEnabled = true // Flag to track if resending is allowed
    private var resendTimer: CountDownTimer? = null // Timer for resend delay

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        storedVerificationId = intent.getStringExtra("verificationId").toString()
        phoneNumber = intent.getStringExtra("phoneNumber").toString()
        password = intent.getStringExtra("password").toString()
        resendToken = intent.getParcelableExtra("resendToken")!!

        binding.verifyOtpBtn.setOnClickListener {
            val otp = binding.otpEditText.text.toString().trim()
            if (otp.isNotEmpty() && otp.length == 6) {
                verifyPhoneNumberWithCode(storedVerificationId, otp)
            } else {
                binding.otpEditText.error = "Invalid OTP"
            }
        }

        binding.resendOtpBtn.setOnClickListener {
            if (resendEnabled) {
                resendVerificationCode(phoneNumber)
            } else {
                Toast.makeText(this, "Please wait before resending OTP", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun verifyPhoneNumberWithCode(verificationId: String, code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    createAccount(phoneNumber, password)
                } else {
                    Log.e("OtpVerificationActivity", "OTP verification failed", task.exception)
                    Toast.makeText(
                        applicationContext,
                        "OTP verification failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun createAccount(phoneNumber: String, password: String) {
        val email = "$phoneNumber@example.com"
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        applicationContext,
                        "Account created successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    navigateToMainActivity()
                } else {
                    Log.e("OtpVerificationActivity", "Account creation failed", task.exception)
                    Toast.makeText(
                        applicationContext,
                        "Account creation failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun navigateToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun resendVerificationCode(phoneNumber: String) {
        if (::resendToken.isInitialized && resendEnabled) {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91$phoneNumber", // Assuming Indian numbers, update this if needed
                60, // Timeout duration in seconds
                TimeUnit.SECONDS,
                this,
                object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        // This should not happen here, since we are resending
                    }

                    override fun onVerificationFailed(e: FirebaseException) {
                        Log.e("OtpVerificationActivity", "Resend verification failed", e)
                        Toast.makeText(
                            applicationContext,
                            "Resend verification failed: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun onCodeSent(
                        newVerificationId: String,
                        newToken: PhoneAuthProvider.ForceResendingToken
                    ) {
                        storedVerificationId = newVerificationId
                        resendToken = newToken
                        Toast.makeText(applicationContext, "OTP resent", Toast.LENGTH_SHORT).show()
                    }
                },
                resendToken
            )
            startResendTimer()
        } else {
            Toast.makeText(applicationContext, "Resend not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startResendTimer() {
        resendEnabled = false
        resendTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                binding.resendOtpBtn.text = "Resend OTP in $secondsRemaining seconds"
            }

            override fun onFinish() {
                resendEnabled = true
                binding.resendOtpBtn.text = "Resend OTP"
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        resendTimer?.cancel()
    }
}
