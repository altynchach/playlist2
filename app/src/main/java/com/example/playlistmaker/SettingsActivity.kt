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

class SettingsActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize SharedPreferences to store and retrieve theme settings
        sharedPreferences = getSharedPreferences("com.example.playlistmaker.PREFERENCES", MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean("DARK_MODE", false)
        setTheme(isDarkMode)

        setContentView(R.layout.activity_settings)

        // Back button functionality
        val arrow = findViewById<ImageView>(R.id.back_button)
        arrow.setOnClickListener {
            finish()  // Close the settings activity
        }

        // Theme switcher logic
        val themeSwitch = findViewById<Switch>(R.id.switch_theme)
        themeSwitch.isChecked = isDarkMode

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            setTheme(isChecked)  // Update theme
            sharedPreferences.edit().putBoolean("DARK_MODE", isChecked).apply()  // Save preference
        }

        // Share app functionality
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

        // Support contact functionality
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

        // User agreement link functionality
        val userAgreementButton = findViewById<TextView>(R.id.tvUserAgreement)
        val userAgreementImage = findViewById<ImageView>(R.id.ivUserAgreement)
        val userAgreementClickListener = {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.practicum_offer_url)))
            startActivity(browserIntent)
        }
        userAgreementButton.setOnClickListener { userAgreementClickListener.invoke() }
        userAgreementImage.setOnClickListener { userAgreementClickListener.invoke() }
    }

    private fun setTheme(isDarkMode: Boolean) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}
