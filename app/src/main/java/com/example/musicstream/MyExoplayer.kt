package com.example.musicstream

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.musicstream.models.SongModel
import com.google.firebase.firestore.FirebaseFirestore

object MyExoplayer {
    private var exoPlayer: ExoPlayer? = null
    private var currentSong: SongModel? = null
    fun getCurrentSong(): SongModel? {
        return currentSong
    }

    fun getInstance(): ExoPlayer? {
        return exoPlayer
    }

    fun startPlaying(context: Context, song: SongModel) {
        if (exoPlayer == null)
            exoPlayer = ExoPlayer.Builder(context).build()
        if (currentSong != song) {
            //its a new song so start playing
            currentSong = song
            updateCountViews()
            currentSong?.url?.apply {
                val mediaItem = MediaItem.fromUri(this)
                exoPlayer?.setMediaItem(mediaItem)
                exoPlayer?.prepare()
                exoPlayer?.play()
            }

        }
    }

    fun updateCountViews() {
        currentSong?.id?.let { id ->
            FirebaseFirestore.getInstance().collection("songs").document(id).get()
                .addOnSuccessListener {
                    var latestCount = it.getLong("count")
                    if (latestCount == null) {
                        latestCount = 1L
                    } else {
                        latestCount += 1
                    }
                    FirebaseFirestore.getInstance().collection("songs")
                        .document(id)
                        .update(mapOf("count" to latestCount))
                }
        }
    }

}