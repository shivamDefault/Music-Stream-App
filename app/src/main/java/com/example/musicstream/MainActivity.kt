package com.example.musicstream

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.musicstream.adapter.CategoryAdapter
import com.example.musicstream.adapter.SectionSongListAdapter
import com.example.musicstream.databinding.ActivityMainBinding
import com.example.musicstream.models.CategoryModel
import com.example.musicstream.models.SongModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObjects

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var categoryAdapter: CategoryAdapter
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Check if user is logged in
        if (auth.currentUser == null) {
            // User is not logged in, start the LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
            finish() // Close MainActivity
            return // Don't continue with the rest of onCreate
        } else {
            // User is logged in, continue with setup
            getCategories()
            setupSection(
                "section_1",
                binding.section1MainLayout,
                binding.section1Title,
                binding.section1RecyclerView
            )
            setupSection(
                "section_2",
                binding.section2MainLayout,
                binding.section2Title,
                binding.section2RecyclerView
            )
            setupSection(
                "section_3",
                binding.section3MainLayout,
                binding.section3Title,
                binding.section3RecyclerView
            )
            setupSection(
                "section_4",
                binding.section4MainLayout,
                binding.section4Title,
                binding.section4RecyclerView
            )
            setupSection(
                "section_5",
                binding.section5MainLayout,
                binding.section5Title,
                binding.section5RecyclerView
            )
            setupSection(
                "section_6",
                binding.section6MainLayout,
                binding.section6Title,
                binding.section6RecyclerView
            )
            setupMostlyPlayed(
                "mostly_played",
                binding.mostlyPlayedMainLayout,
                binding.mostlyPlayedTitle,
                binding.mostlyPlayedRecyclerView
            )

            binding.optionBtn.setOnClickListener {
                showPopupMenu()
            }
        }
    }

    fun showPopupMenu() {
        val popupMenu = PopupMenu(this, binding.optionBtn)
        val inflator = popupMenu.menuInflater
        inflator.inflate(R.menu.option_menu, popupMenu.menu)
        popupMenu.show()
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.logout -> {
                    logout()
                    true
                }
            }
            false
        }
    }

    fun logout() {
        MyExoplayer.getInstance()?.release()
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onResume() {
        super.onResume()
        showPlayerView()
    }

    fun showPlayerView() {
        binding.playerView.setOnClickListener {
            startActivity(Intent(this, PlayerActivity::class.java))
        }
        MyExoplayer.getCurrentSong()?.let {
            binding.playerView.visibility = View.VISIBLE
            binding.songTitleTextView.text = "Now Playing: " + it.title
            Glide.with(binding.songCoverImageView).load(it.coverUrl)
                .apply(RequestOptions().transform(RoundedCorners(32)))
                .into(binding.songCoverImageView)
        } ?: run {
            binding.playerView.visibility = View.GONE
        }
    }

    // category
    fun getCategories() {
        FirebaseFirestore.getInstance()
            .collection("category")
            .get().addOnSuccessListener {
                val categoryList = it.toObjects(CategoryModel::class.java)
                setupCategoryRecyclerView(categoryList)
            }
    }

    fun setupCategoryRecyclerView(categoryList: List<CategoryModel>) {
        categoryAdapter = CategoryAdapter(categoryList)
        binding.categoriesRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.categoriesRecyclerView.adapter = categoryAdapter
    }

    // section
    fun setupSection(
        id: String,
        mainLayout: RelativeLayout,
        titleView: TextView,
        recyclerView: RecyclerView
    ) {
        FirebaseFirestore.getInstance().collection("sections").document(id).get()
            .addOnSuccessListener {
                val section = it.toObject(CategoryModel::class.java)
                section?.apply {
                    mainLayout.visibility = View.VISIBLE
                    titleView.text = name
                    recyclerView.layoutManager = LinearLayoutManager(
                        this@MainActivity, LinearLayoutManager.HORIZONTAL, false
                    )
                    recyclerView.adapter = SectionSongListAdapter(songs)
                    mainLayout.setOnClickListener {
                        SongsListActivity.category = section
                        startActivity(Intent(this@MainActivity, SongsListActivity::class.java))
                    }
                }
            }
    }

    fun setupMostlyPlayed(
        id: String,
        mainLayout: RelativeLayout,
        titleView: TextView,
        recyclerView: RecyclerView
    ) {
        FirebaseFirestore.getInstance().collection("sections").document(id).get()
            .addOnSuccessListener {
                // we will add mostly played song over here
                FirebaseFirestore.getInstance().collection("songs")
                    .orderBy("count", Query.Direction.DESCENDING)
                    .limit(5)
                    .get().addOnSuccessListener { songListSnapshot ->
                        val songModelList = songListSnapshot.toObjects<SongModel>()
                        val songIdList = songModelList.map {
                            it.id
                        }.toList()
                        val section = it.toObject(CategoryModel::class.java)
                        section?.apply {
                            section.songs = songIdList
                            mainLayout.visibility = View.VISIBLE
                            titleView.text = name
                            recyclerView.layoutManager = LinearLayoutManager(
                                this@MainActivity, LinearLayoutManager.HORIZONTAL, false
                            )
                            recyclerView.adapter = SectionSongListAdapter(songs)
                            mainLayout.setOnClickListener {
                                SongsListActivity.category = section
                                startActivity(
                                    Intent(
                                        this@MainActivity,
                                        SongsListActivity::class.java
                                    )
                                )
                            }
                        }
                    }
            }

    }
}