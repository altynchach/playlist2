package com.example.playlistmaker.presentation.search

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.presentation.adapters.TrackAdapter
import com.example.playlistmaker.presentation.player.PlayerActivity
import com.example.playlistmaker.presentation.states.SearchScreenState
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_search.*

class SearchActivity : AppCompatActivity() {

    companion object {
        const val NAME_TRACK = "name"
    }

    private val viewModel: SearchViewModel by viewModels { SearchViewModel.Factory(applicationContext) }

    private lateinit var trackAdapter: TrackAdapter
    private lateinit var historyAdapter: TrackAdapter

    private var lastClickTime: Long = 0

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        recyclerView.layoutManager = LinearLayoutManager(this)
        trackAdapter = TrackAdapter(arrayListOf())
        recyclerView.adapter = trackAdapter

        search_history_recycler.layoutManager = LinearLayoutManager(this)
        historyAdapter = TrackAdapter(arrayListOf())
        search_history_recycler.adapter = historyAdapter

        clear_history_button.setOnClickListener {
            viewModel.clearHistory()
        }

        trackAdapter.setOnTrackClickListener { track ->
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime > 2000) {
                lastClickTime = currentTime
                viewModel.saveTrackToHistory(track)
                openPlayerActivity(track)
            }
        }

        historyAdapter.setOnTrackClickListener { track ->
            openPlayerActivity(track)
        }

        inputEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.onSearchQueryChanged(s.toString())
                updateClearButtonVisibility()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        inputEditText.setOnFocusChangeListener { _, hasFocus ->
            viewModel.onFocusChanged(hasFocus)
        }

        inputEditText.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = 2
                if (inputEditText.compoundDrawables[drawableEnd] != null &&
                    event.rawX >= (inputEditText.right - inputEditText.compoundDrawables[drawableEnd].bounds.width())
                ) {
                    inputEditText.text.clear()
                    hideKeyboard(v)
                    return@setOnTouchListener true
                }
            }
            false
        }

        back_button2.setOnClickListener {
            finish()
        }

        reload_button.setOnClickListener {
            viewModel.onReloadClicked()
        }

        viewModel.state.observe(this) { state ->
            renderState(state)
        }

        // Инициализируем состояние при старте
        viewModel.init()
    }

    private fun renderState(state: SearchScreenState) {
        progress_bar_layout.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        recyclerView.visibility = if (state.showResults) View.VISIBLE else View.GONE
        nothingFound.visibility = if (state.showNothingFound) View.VISIBLE else View.GONE
        connectionProblem.visibility = if (state.showError) View.VISIBLE else View.GONE
        search_history_layout.visibility = if (state.showHistory) View.VISIBLE else View.GONE

        if (state.results.isNotEmpty()) {
            trackAdapter.updateTracks(state.results)
        }

        if (state.history.isNotEmpty()) {
            historyAdapter.updateTracks(state.history)
        }

        if (inputEditText.hasFocus() && inputEditText.text.isEmpty()) {
            search_history_layout.visibility = View.VISIBLE
        }
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun updateClearButtonVisibility() {
        if (inputEditText.text.isNotEmpty()) {
            inputEditText.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(this, R.drawable.search), null,
                ContextCompat.getDrawable(this, R.drawable.clear), null
            )
        } else {
            inputEditText.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(this, R.drawable.search), null,
                null, null
            )
        }
    }

    private fun openPlayerActivity(track: Track) {
        val displayIntent = Intent(this, PlayerActivity::class.java)
        val strTrack = Gson().toJson(track)
        displayIntent.putExtra(NAME_TRACK, strTrack)
        startActivity(displayIntent)
    }
}
