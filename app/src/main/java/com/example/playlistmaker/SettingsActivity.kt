package com.example.playlistmaker

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.*

class SettingsActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("com.example.playlistmaker.PREFERENCES", MODE_PRIVATE)

        if (!sharedPreferences.contains("DARK_MODE")) {
            val isSystemInDarkMode = (resources.configuration.uiMode
                    and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
            sharedPreferences.edit().putBoolean("DARK_MODE", isSystemInDarkMode).apply()
        }

        val isDarkMode = sharedPreferences.getBoolean("DARK_MODE", false)
        applyTheme(isDarkMode)

        setContentView(R.layout.activity_settings)

        val arrow = findViewById<ImageView>(R.id.back_button)
        arrow.setOnClickListener {
            finish()
        }

        val themeSwitch = findViewById<Switch>(R.id.switch_theme)
        themeSwitch.isChecked = isDarkMode

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            applyTheme(isChecked)
            sharedPreferences.edit().putBoolean("DARK_MODE", isChecked).apply()
        }

        val shareAppButton = findViewById<TextView>(R.id.tvShareApp)
        val shareAppImage = findViewById<ImageView>(R.id.iv_share_app)
        val shareAppClickListener = {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message))
            }
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_app)))
        }
        shareAppButton.setOnClickListener { shareAppClickListener.invoke() }
        shareAppImage.setOnClickListener { shareAppClickListener.invoke() }

        val supportButton = findViewById<TextView>(R.id.tvSupport)
        val supportImage = findViewById<ImageView>(R.id.ivSupport)
        val supportClickListener = {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.support_email)))
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_email_subject))
                putExtra(Intent.EXTRA_TEXT, getString(R.string.support_email_body))
            }
            startActivity(emailIntent)
        }
        supportButton.setOnClickListener { supportClickListener.invoke() }
        supportImage.setOnClickListener { supportClickListener.invoke() }

        val userAgreementButton = findViewById<TextView>(R.id.tvUserAgreement)
        val userAgreementImage = findViewById<ImageView>(R.id.ivUserAgreement)
        val userAgreementClickListener = {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.practicum_offer_url)))
            startActivity(browserIntent)
        }
        userAgreementButton.setOnClickListener { userAgreementClickListener.invoke() }
        userAgreementImage.setOnClickListener { userAgreementClickListener.invoke() }
    }

    private fun applyTheme(isDarkMode: Boolean) {
        if (isDarkMode) {
            setDefaultNightMode(MODE_NIGHT_YES)
        } else {
            setDefaultNightMode(MODE_NIGHT_NO)
        }
    }
}