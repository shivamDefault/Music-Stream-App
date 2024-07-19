package com.example.musicstream

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.bumptech.glide.Glide
import com.example.musicstream.databinding.ActivityPlayerBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class PlayerActivity : AppCompatActivity() {
    lateinit var binding: ActivityPlayerBinding
    lateinit var exoPlayer: ExoPlayer

    // Add the @OptIn annotation to your playerListener
    @OptIn(UnstableApi::class)
    var playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            showGif(isPlaying)
        }

        // Show interstitial ad after playback state changes to STATE_IDLE
        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            if (playbackState == Player.STATE_IDLE) { // State after song finishes
                // Add a small delay
                Handler(Looper.getMainLooper()).postDelayed({
                    showInterstitialAd()
                }, 2000) // 2000 milliseconds (2 seconds)
            }
        }
    }

    // Add AdMob related variables
    private lateinit var adView: AdView
    private var interstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize AdMob
        initializeAdMob()

        // Load Banner Ad
        loadBannerAd()

        // Load Interstitial Ad (if needed)
        loadInterstitialAd()

        MyExoplayer.getCurrentSong()?.apply {
            binding.songTitleTextView.text = title
            binding.songSubtitleTextView.text = subtitle
            Glide.with(binding.songCoverImageView)
                .load(coverUrl)
                .circleCrop().into(binding.songCoverImageView)
            Glide.with(binding.songGifImageView)
                .load(R.drawable.media_player)
                .circleCrop().into(binding.songGifImageView)
            exoPlayer = MyExoplayer.getInstance()!!
            binding.playerView.player = exoPlayer
            binding.playerView.showController()
            exoPlayer.addListener(playerListener)
        }
    }

    private fun initializeAdMob() {
        MobileAds.initialize(this) {
            // Initialize AdMob
        }
    }

    private fun loadBannerAd() {
        adView = findViewById(R.id.banner_ad)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    // Load Interstitial Ad (optional)
    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this, "ca-app-pub-3609560605219883/7197878174", // Use YOUR interstitial Ad Unit ID
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    Toast.makeText(this@PlayerActivity, "Interstitial ad loaded", Toast.LENGTH_SHORT).show()
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    // Handle the error
                    Toast.makeText(this@PlayerActivity, "Interstitial ad failed to load: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // Show the interstitial ad (optional)
    private fun showInterstitialAd() {
        if (interstitialAd != null) {
            // Only show the ad if it's not null (meaning it was loaded successfully)
            interstitialAd?.show(this)
            interstitialAd = null // Clear the reference after showing
        } else {
            // Handle the case where the interstitial ad is not available
            Toast.makeText(this@PlayerActivity, "Interstitial ad is not ready yet.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.removeListener(playerListener)
        adView.destroy() // Destroy the banner ad view when the activity is destroyed
    }

    fun showGif(show: Boolean) {
        if (show)
            binding.songGifImageView.visibility = View.VISIBLE
        else
            binding.songGifImageView.visibility = View.INVISIBLE
    }
}