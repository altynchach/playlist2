package com.example.playlistmaker.presentation.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.presentation.utils.ThemeManager
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment() {

    private val viewModel: SettingsViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val themeSwitch = view.findViewById<Switch>(R.id.switch_theme)
        val shareAppButton = view.findViewById<TextView>(R.id.tvShareApp)
        val shareAppImage = view.findViewById<ImageView>(R.id.iv_share_app)
        val supportButton = view.findViewById<TextView>(R.id.tvSupport)
        val supportImage = view.findViewById<ImageView>(R.id.ivSupport)
        val userAgreementButton = view.findViewById<TextView>(R.id.tvUserAgreement)
        val userAgreementImage = view.findViewById<ImageView>(R.id.ivUserAgreement)

        shareAppButton.setOnClickListener { shareApp() }
        shareAppImage.setOnClickListener { shareApp() }

        supportButton.setOnClickListener { supportMail() }
        supportImage.setOnClickListener { supportMail() }

        userAgreementButton.setOnClickListener { openUserAgreement() }
        userAgreementImage.setOnClickListener { openUserAgreement() }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            renderState(state, themeSwitch)
        }

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onThemeSwitchChanged(isChecked)
            ThemeManager.applyTheme(isChecked)
            (requireActivity() as AppCompatActivity).delegate.applyDayNight()
        }

        viewModel.init()
    }

    private fun renderState(state: SettingsScreenState, themeSwitch: Switch) {
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
