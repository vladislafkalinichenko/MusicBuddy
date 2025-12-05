package com.example.musicbuddy

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.musicbuddy.data.TrackRepository
import com.example.musicbuddy.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {

    private lateinit var b: ActivityPlayerBinding
    private var mediaPlayer: MediaPlayer? = null
    private var currentTrackId: String? = null
    private val handler = Handler(Looper.getMainLooper())

    private val tick = object : Runnable {
        override fun run() {
            val mp = mediaPlayer ?: run { handler.removeCallbacks(this); return }
            if (mp.isPlaying) {
                val p = mp.currentPosition
                b.seekBar.progress = p
                b.tvCurrent.text = fmt(p)
            }
            handler.postDelayed(this, 500)
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val lang = Preferences.loadLang(newBase)
        super.attachBaseContext(LocaleHelper.wrap(newBase, lang))
    }

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        b = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(b.root)
        currentTrackId = intent.getStringExtra("track_id")
        initUi()
        loadTrack(currentTrackId)
        icon(false)
    }

    private fun initUi() {
        b.btnPlayPause.setOnClickListener { mediaPlayer?.let { if (it.isPlaying) stopPlay() else startPlay() } ?: startPlay() }
        b.btnNext.setOnClickListener { changeTo(TrackRepository.getNext(this, currentTrackId ?: "")) }
        b.btnPrev.setOnClickListener { changeTo(TrackRepository.getPrev(this, currentTrackId ?: "")) }
        b.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            var dragged = false
            override fun onProgressChanged(sb: SeekBar?, pos: Int, fromUser: Boolean) { if (fromUser) b.tvCurrent.text = fmt(pos) }
            override fun onStartTrackingTouch(sb: SeekBar?) { dragged = true }
            override fun onStopTrackingTouch(sb: SeekBar?) { dragged = false; try { mediaPlayer?.seekTo(sb?.progress ?: 0) } catch (_: Throwable) {} }
        })
    }

    private fun changeTo(t: com.example.musicbuddy.data.Track?) {
        if (t == null) {
            Toast.makeText(this, getString(R.string.next_track_not_found), Toast.LENGTH_SHORT).show()
            return
        }
        currentTrackId = t.id
        release()
        bind(t)
        prepareAndPlay(t.url)
    }

    private fun loadTrack(id: String?) {
        val t = id?.let { TrackRepository.findById(this, it) } ?: TrackRepository.loadFromAssets(this).firstOrNull()
        if (t == null) {
            Toast.makeText(this, getString(R.string.no_tracks), Toast.LENGTH_SHORT).show()
            return
        }
        bind(t)
        prepareAndPlay(t.url)
    }

    private fun bind(t: com.example.musicbuddy.data.Track) {
        b.tvTitle.text = t.title
        b.tvArtist.text = t.artist
    }

    private fun prepareAndPlay(url: String) {
        if (url.isBlank()) {
            Toast.makeText(this, getString(R.string.track_url_empty), Toast.LENGTH_SHORT).show()
            return
        }
        release()
        mediaPlayer = MediaPlayer()
        try {
            mediaPlayer?.apply {
                setAudioStreamType(AudioManager.STREAM_MUSIC)
                try { setDataSource(url) } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@PlayerActivity, "${getString(R.string.load_error)}: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                    release(); mediaPlayer = null; icon(false); return
                }
                isLooping = false
                setOnPreparedListener { mp ->
                    b.seekBar.max = mp.duration
                    b.tvDuration.text = fmt(mp.duration)
                    startPlayInternal()
                }
                setOnCompletionListener {
                    changeTo(TrackRepository.getNext(this@PlayerActivity, currentTrackId ?: ""))
                }
                setOnErrorListener { _, what, extra ->
                    runOnUiThread {
                        Toast.makeText(this@PlayerActivity, getString(R.string.playback_error) + " ($what,$extra)", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "${getString(R.string.player_error)}: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            mediaPlayer?.release()
            mediaPlayer = null
            icon(false)
        }
    }

    private fun startPlayInternal() {
        try { mediaPlayer?.start() } catch (_: Throwable) {}
        icon(true); handler.removeCallbacks(tick); handler.post(tick)
    }

    private fun startPlay() {
        try {
            mediaPlayer?.start() ?: run {
                val t = currentTrackId?.let { TrackRepository.findById(this, it) } ?: TrackRepository.loadFromAssets(this).firstOrNull()
                t?.let { prepareAndPlay(it.url); return }
                Toast.makeText(this, getString(R.string.no_tracks), Toast.LENGTH_SHORT).show()
                return
            }
            icon(true); handler.removeCallbacks(tick); handler.post(tick)
        } catch (_: Throwable) {
            Toast.makeText(this, getString(R.string.cant_start), Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopPlay() {
        try { mediaPlayer?.pause() } catch (_: Throwable) {}
        icon(false); handler.removeCallbacks(tick)
    }

    private fun icon(on: Boolean) {
        if (on) {
            b.btnPlayPause.setImageResource(android.R.drawable.ic_media_pause)
            b.btnPlayPause.contentDescription = getString(R.string.pause)
        } else {
            b.btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
            b.btnPlayPause.contentDescription = getString(R.string.play)
        }
    }

    private fun release() {
        handler.removeCallbacks(tick)
        try {
            mediaPlayer?.setOnPreparedListener(null)
            mediaPlayer?.setOnCompletionListener(null)
            mediaPlayer?.setOnErrorListener(null)
            mediaPlayer?.release()
        } catch (_: Throwable) {}
        mediaPlayer = null
        icon(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        release()
    }

    private fun fmt(ms: Int): String {
        val s = ms / 1000
        val m = s / 60
        val sec = s % 60
        return String.format("%02d:%02d", m, sec)
    }
}