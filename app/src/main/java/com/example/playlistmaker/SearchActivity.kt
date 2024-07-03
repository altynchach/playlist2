package com.example.playlistmaker

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SearchActivity : AppCompatActivity() {

    companion object {
        const val SEARCH_QUERY_KEY = "SEARCH_QUERY"
    }

    private lateinit var inputText: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var trackAdapter: TrackAdapter
    private var searchText: String = ""

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        inputText = findViewById(R.id.inputEditText)
        recyclerView = findViewById(R.id.recyclerView)

        inputText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                searchText = s.toString()
                updateClearButtonVisibility()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        inputText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showKeyboard()
            }
        }

        updateClearButtonVisibility()

        inputText.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = 2 // Index of the drawableEnd
                if (inputText.compoundDrawables[drawableEnd] != null && event.rawX >= (inputText.right - inputText.compoundDrawables[drawableEnd].bounds.width())) {
                    inputText.text.clear()
                    hideKeyboard(v)
                    updateClearButtonVisibility()
                    return@setOnTouchListener true
                }
            }
            false
        }

        if (savedInstanceState != null) {
            searchText = savedInstanceState.getString(SEARCH_QUERY_KEY, "")
            inputText.setText(searchText)
        }

        val backButton2 = findViewById<ImageView>(R.id.back_button2)
        backButton2.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Mock data
        val tracks = listOf(
            Track("Smells Like Teen Spirit", "Nirvana", "5:01", "https://is5-ssl.mzstatic.com/image/thumb/Music115/v4/7b/58/c2/7b58c21a-2b51-2bb2-e59a-9bb9b96ad8c3/00602567924166.rgb.jpg/100x100bb.jpg"),
            Track("Billie Jean", "Michael Jackson", "4:35", "https://is5-ssl.mzstatic.com/image/thumb/Music125/v4/3d/9d/38/3d9d3811-71f0-3a0e-1ada-3004e56ff852/827969428726.jpg/100x100bb.jpg"),
            Track("Stayin' Alive", "Bee Gees", "4:10", "https://is4-ssl.mzstatic.com/image/thumb/Music115/v4/1f/80/1f/1f801fc1-8c0f-ea3e-d3e5-387c6619619e/16UMGIM86640.rgb.jpg/100x100bb.jpg"),
            Track("Whole Lotta Love", "Led Zeppelin", "5:33", "https://is2-ssl.mzstatic.com/image/thumb/Music62/v4/7e/17/e3/7e17e33f-2efa-2a36-e916-7f808576cf6b/mzm.fyigqcbs.jpg/100x100bb.jpg"),
            Track("Sweet Child O'Mine", "Guns N' Roses", "5:03", "https://is5-ssl.mzstatic.com/image/thumb/Music125/v4/a0/4d/c4/a04dc484-03cc-02aa-fa82-5334fcb4bc16/18UMGIM24878.rgb.jpg/100x100bb.jpg")
        )

        // RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        trackAdapter = TrackAdapter(tracks)
        recyclerView.adapter = trackAdapter
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_QUERY_KEY, searchText)
    }

    private fun showKeyboard() {
        inputText.requestFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(inputText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun updateClearButtonVisibility() {
        if (inputText.text.isNotEmpty()) {
            inputText.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(this, R.drawable.search), null,
                ContextCompat.getDrawable(this, R.drawable.clear), null
            )
        } else {
            inputText.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(this, R.drawable.search), null,
                null, null
            )
        }
    }
}
