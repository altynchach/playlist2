package com.example.playlistmaker

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat

class ActivitySettings : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)

        val arrow = findViewById<ImageView>(R.id.back_button)
        arrow.setOnClickListener {
            finish()
        }

        //обработчик кнопки "Поделиться приложением"
        val shareAppButton = findViewById<TextView>(R.id.tvShareApp)
        shareAppButton.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message))
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_app)))
        }

        //обработчик кнопки "Написать в поддержку"
        val supportButton = findViewById<TextView>(R.id.tvSupport)
        supportButton.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf("your_email@example.com"))
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_email_subject))
                putExtra(Intent.EXTRA_TEXT, getString(R.string.support_email_body))
            }
            startActivity(emailIntent)
        }

        //обработчик кнопки "Пользовательское соглашение"
        val userAgreementButton = findViewById<TextView>(R.id.tvUserAgreement)
        userAgreementButton.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://yandex.ru/legal/practicum_offer/"))
            startActivity(browserIntent)
        }
    }
}
