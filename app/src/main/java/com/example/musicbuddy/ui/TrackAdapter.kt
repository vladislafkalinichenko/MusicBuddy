package com.example.musicbuddy.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.musicbuddy.R
import com.example.musicbuddy.data.Track

class TrackAdapter(
    private var items: List<Track>,
    private val onPlay: (Track) -> Unit
) : RecyclerView.Adapter<TrackAdapter.Holder>() {

    fun updateList(newList: List<Track>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_track, parent, false)
        return Holder(v)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class Holder(v: View) : RecyclerView.ViewHolder(v) {
        private val title = v.findViewById<TextView>(R.id.tvTitle)
        private val artist = v.findViewById<TextView>(R.id.tvArtist)
        private val playBtn = v.findViewById<View>(R.id.btnPlay)
        private val img = v.findViewById<ImageView>(R.id.ivCover)

        fun bind(t: Track) {
            title.text = t.title
            artist.text = t.artist
            playBtn.setOnClickListener { onPlay(t) }
            img.setImageResource(R.drawable.ic_music_placeholder)
        }
    }
}