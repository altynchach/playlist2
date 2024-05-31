package com.example.playlistmaker
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView

class ActivitySettings : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)

        val arrow = findViewById<ImageView>(R.id.back_button)
        arrow.setOnClickListener {
            finish()
        }
    }
}
