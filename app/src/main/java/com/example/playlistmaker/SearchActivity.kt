package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class SearchActivity : AppCompatActivity() {

    companion object {
        const val SEARCH_QUERY_KEY = "SEARCH_QUERY"
    }

    private lateinit var searchText: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val inputEditText = findViewById<EditText>(R.id.inputEditText)
        inputEditText.setTextColor(ContextCompat.getColor(this, R.color.search_text))
        inputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    inputEditText.hint = getString(R.string.search)
                    inputEditText.setHintTextColor(ContextCompat.getColor(this@SearchActivity, R.color.icon_search))
                } else {
                    inputEditText.hint = ""
                }
            }
        })

        if (savedInstanceState != null) {
            searchText = savedInstanceState.getString(SEARCH_QUERY_KEY, "")
        }

        // Set click listener for back_button2 to navigate to MainActivity
        val backButton2 = findViewById<ImageView>(R.id.back_button2)
        backButton2.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val inputEditText = findViewById<EditText>(R.id.inputEditText)
        outState.putString(SEARCH_QUERY_KEY, inputEditText.text.toString())
    }
}
