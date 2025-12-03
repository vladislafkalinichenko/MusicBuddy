package com.example.musicbuddy
import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.musicbuddy.databinding.ActivitySettingsBinding
import java.util.Locale
class SettingsActivity : AppCompatActivity() {
    private lateinit var b: ActivitySettingsBinding

    override fun attachBaseContext(newBase: android.content.Context) {
        val lang = Preferences.loadLang(newBase) ?: "ru"
        super.attachBaseContext(LocaleHelper.wrap(newBase, lang))
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(b.root)
        loadUi()
        b.btnSave.setOnClickListener { applyAndClose() }
    }
    private fun loadUi() {
        when (Preferences.loadTheme(this)) {
            "light" -> b.rgTheme.check(b.rbLight.id)
            "dark" -> b.rgTheme.check(b.rbDark.id)
            else -> b.rgTheme.check(b.rbSystem.id)
        }
        when (Preferences.loadLang(this)) {
            "ru" -> b.rgLang.check(b.rbRu.id)
            "en" -> b.rgLang.check(b.rbEn.id)
            else -> b.rgLang.check(b.rbRu.id)
        }
    }
    private fun applyAndClose() {
        val themeChoice = findViewById<RadioButton>(b.rgTheme.checkedRadioButtonId).tag.toString()
        val langChoice = findViewById<RadioButton>(b.rgLang.checkedRadioButtonId).tag.toString()
        Preferences.saveTheme(this, themeChoice)
        Preferences.saveLang(this, langChoice)
        when (themeChoice) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
        val i = Intent(this, MainActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(i)
        Toast.makeText(this, getString(R.string.settings_applied), Toast.LENGTH_SHORT).show()
        finish()
    }
}