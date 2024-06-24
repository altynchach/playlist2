package com.example.playlistmaker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView


class ActivitySearch : AppCompatActivity() {

    private lateinit var inputText: EditText
    private var restoredText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        inputText = findViewById(R.id.inputEditText)
        if (savedInstanceState != null) {
            inputText.setText(restoredText)
        }

        clearButton = findViewById(R.id.clearIcon)

    }
}
