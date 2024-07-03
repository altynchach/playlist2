package com.example.playlistmaker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)

        val arrow = findViewById<ImageView>(R.id.back_button)
        arrow.setOnClickListener {
            finish()
        }

        val shareAppButton = findViewById<TextView>(R.id.tvShareApp)
        val shareAppImage = findViewById<ImageView>(R.id.iv_share_app)
        val shareAppClickListener = {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message))
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
}
