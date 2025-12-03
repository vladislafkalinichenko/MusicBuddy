package com.example.musicbuddy
import android.content.Context
object Preferences {
    private const val PREFS = "musicbuddy_prefs"
    private const val KEY_THEME = "pref_theme"
    private const val KEY_LANG = "pref_lang"
    fun saveTheme(ctx: Context, value: String) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_THEME, value).apply()
    }
    fun loadTheme(ctx: Context): String {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_THEME, "system") ?: "system"
    }
    fun saveLang(ctx: Context, value: String) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_LANG, value).apply()
    }
    fun loadLang(ctx: Context): String {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_LANG, "ru") ?: "ru"
    }
}