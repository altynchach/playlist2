package com.example.playlistmaker.presentation.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.presentation.states.SettingsScreenState
import com.example.playlistmaker.presentation.utils.ThemeManager
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsActivity : AppCompatActivity() {

    private val viewModel: SettingsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val arrow = findViewById<ImageView>(R.id.back_button)
        arrow.setOnClickListener {
            finish()
        }

        val themeSwitch = findViewById<Switch>(R.id.switch_theme)

        val shareAppButton = findViewById<TextView>(R.id.tvShareApp)
        val shareAppImage = findViewById<ImageView>(R.id.iv_share_app)
        val supportButton = findViewById<TextView>(R.id.tvSupport)
        val supportImage = findViewById<ImageView>(R.id.ivSupport)
        val userAgreementButton = findViewById<TextView>(R.id.tvUserAgreement)
        val userAgreementImage = findViewById<ImageView>(R.id.ivUserAgreement)

        shareAppButton.setOnClickListener { shareApp() }
        shareAppImage.setOnClickListener { shareApp() }

        supportButton.setOnClickListener { supportMail() }
        supportImage.setOnClickListener { supportMail() }

        userAgreementButton.setOnClickListener { openUserAgreement() }
        userAgreementImage.setOnClickListener { openUserAgreement() }

        viewModel.state.observe(this) { state ->
            renderState(state)
        }

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onThemeSwitchChanged(isChecked)
            ThemeManager.applyTheme(isChecked)
            delegate.applyDayNight()
        }

        viewModel.init()
    }

    private fun renderState(state: SettingsScreenState) {
        val themeSwitch = findViewById<Switch>(R.id.switch_theme)
        themeSwitch.isChecked = state.isDarkMode
    }

    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message))
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_app)))
    }

    private fun supportMail() {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.support_email)))
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_email_subject))
            putExtra(Intent.EXTRA_TEXT, getString(R.string.support_email_body))
        }
        startActivity(emailIntent)
    }

    private fun openUserAgreement() {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.practicum_offer_url)))
        startActivity(browserIntent)
    }
}
