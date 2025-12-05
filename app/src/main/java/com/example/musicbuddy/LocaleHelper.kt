package com.example.musicbuddy
import android.content.Context
import android.content.res.Configuration
import java.util.Locale
object LocaleHelper {
    fun wrap(ctx: Context, lang: String): Context {
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val cfg = Configuration(ctx.resources.configuration)
        cfg.setLocale(locale)
        return ctx.createConfigurationContext(cfg)
    }
}