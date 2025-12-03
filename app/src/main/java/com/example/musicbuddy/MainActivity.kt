package com.example.musicbuddy
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicbuddy.data.TrackRepository
import com.example.musicbuddy.databinding.ActivityMainBinding
import com.example.musicbuddy.ui.TrackAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
class MainActivity : AppCompatActivity() {
    private lateinit var b: ActivityMainBinding
    private lateinit var adapter: TrackAdapter
    private var allTracks = listOf<com.example.musicbuddy.data.Track>()

    override fun attachBaseContext(newBase: Context) {
        val lang = Preferences.loadLang(newBase)
        super.attachBaseContext(LocaleHelper.wrap(newBase, lang))
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)
        setupUi()
        loadTracks()
    }
    private fun setupUi() {
        adapter = TrackAdapter(emptyList()) { track ->
            val i = Intent(this, PlayerActivity::class.java)
            i.putExtra("track_id", track.id)
            startActivity(i)
        }
        b.recycler.layoutManager = LinearLayoutManager(this)
        b.recycler.adapter = adapter

        b.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        b.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { filter(s?.toString() ?: "") }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
        })
    }
    private fun loadTracks() {
        CoroutineScope(Dispatchers.IO).launch {
            val list = TrackRepository.loadFromAssets(applicationContext)
            allTracks = list
            CoroutineScope(Dispatchers.Main).launch {
                adapter.updateList(list)
            }
        }
    }
    private fun filter(q: String) {
        val low = q.trim().lowercase()
        if (low.isEmpty()) {
            adapter.updateList(allTracks)
            return
        }
        val filtered = allTracks.filter {
            it.title.lowercase().contains(low) || it.artist.lowercase().contains(low)
        }
        adapter.updateList(filtered)
    }
}