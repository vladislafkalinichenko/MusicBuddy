package com.example.musicbuddy.data

import android.content.Context
import org.json.JSONArray

object TrackRepository {
    private var cached: List<Track>? = null

    fun loadFromAssets(context: Context): List<Track> {
        cached?.let { return it }
        val text = context.assets.open("tracks.json").bufferedReader().use { it.readText() }
        val arr = JSONArray(text)
        val list = mutableListOf<Track>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val t = Track(
                id = obj.optString("id", i.toString()),
                title = obj.optString("title", "Unknown"),
                artist = obj.optString("artist", "Unknown"),
                url = obj.optString("url", "")
            )
            list.add(t)
        }
        cached = list
        return list
    }

    fun findById(context: Context, id: String): Track? {
        return loadFromAssets(context).find { it.id == id }
    }

    fun indexOf(context: Context, id: String): Int {
        return loadFromAssets(context).indexOfFirst { it.id == id }
    }

    fun getNext(context: Context, currentId: String): Track? {
        val list = loadFromAssets(context)
        val idx = list.indexOfFirst { it.id == currentId }
        if (idx == -1) return list.firstOrNull()
        return list.getOrNull((idx + 1) % list.size)
    }

    fun getPrev(context: Context, currentId: String): Track? {
        val list = loadFromAssets(context)
        val idx = list.indexOfFirst { it.id == currentId }
        if (idx == -1) return list.firstOrNull()
        val prev = if (idx - 1 < 0) list.size - 1 else idx - 1
        return list.getOrNull(prev)
    }
}