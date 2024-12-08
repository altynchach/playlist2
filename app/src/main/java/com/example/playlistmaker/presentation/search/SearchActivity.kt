package com.example.playlistmaker.presentation.search

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.presentation.adapters.TrackAdapter
import com.example.playlistmaker.presentation.player.PlayerActivity
import com.example.playlistmaker.presentation.states.SearchScreenState
import com.google.gson.Gson

class SearchActivity : AppCompatActivity() {

    companion object {
        const val NAME_TRACK = "name"
        const val SEARCH_QUERY_KEY = "SEARCH_QUERY"
    }

    private val viewModel: SearchViewModel by viewModels { SearchViewModel.Factory(applicationContext) }

    private lateinit var inputText: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var trackAdapter: TrackAdapter
    private lateinit var nothingFound: LinearLayout
    private lateinit var connectionProblem: LinearLayout
    private lateinit var reloadButton: Button
    private lateinit var progressBarLayout: FrameLayout
    private lateinit var searchHistoryLayout: LinearLayout
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var clearHistoryButton: Button
    private lateinit var historyAdapter: TrackAdapter
    private lateinit var backButton2: ImageView

    private var lastClickTime: Long = 0
    private var searchText: String = ""

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        inputText = findViewById(R.id.inputEditText)
        recyclerView = findViewById(R.id.recyclerView)
        nothingFound = findViewById(R.id.nothingFound)
        connectionProblem = findViewById(R.id.connectionProblem)
        reloadButton = findViewById(R.id.reload_button)
        progressBarLayout = findViewById(R.id.progress_bar_layout)
        searchHistoryLayout = findViewById(R.id.search_history_layout)
        historyRecyclerView = findViewById(R.id.search_history_recycler)
        clearHistoryButton = findViewById(R.id.clear_history_button)
        backButton2 = findViewById(R.id.back_button2)

        recyclerView.layoutManager = LinearLayoutManager(this)
        trackAdapter = TrackAdapter(arrayListOf())
        recyclerView.adapter = trackAdapter

        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyAdapter = TrackAdapter(arrayListOf())
        historyRecyclerView.adapter = historyAdapter

        clearHistoryButton.setOnClickListener {
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

        inputText.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                searchText = s.toString()
                viewModel.onSearchQueryChanged(searchText)
                updateClearButtonVisibility()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        inputText.setOnFocusChangeListener { _, hasFocus ->
            viewModel.onFocusChanged(hasFocus)
        }

        inputText.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = 2
                val drawable = inputText.compoundDrawables[drawableEnd]
                if (drawable != null &&
                    event.rawX >= (inputText.right - drawable.bounds.width())
                ) {
                    inputText.text.clear()
                    hideKeyboard(v)
                    return@setOnTouchListener true
                }
            }
            false
        }

        backButton2.setOnClickListener {
            finish()
        }

        reloadButton.setOnClickListener {
            viewModel.onReloadClicked()
        }

        if (savedInstanceState != null) {
            searchText = savedInstanceState.getString(SEARCH_QUERY_KEY, "")
            inputText.setText(searchText)
        }

        viewModel.getState().observe(this) { state ->
            renderState(state)
        }

        viewModel.init()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_QUERY_KEY, searchText)
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun updateClearButtonVisibility() {
        if (inputText.text.isNotEmpty()) {
            inputText.setCompoundDrawablesWithIntrinsicBounds(
                getDrawable(R.drawable.search), null,
                getDrawable(R.drawable.clear), null
            )
        } else {
            inputText.setCompoundDrawablesWithIntrinsicBounds(
                getDrawable(R.drawable.search), null,
                null, null
            )
        }
    }

    private fun renderState(state: SearchScreenState) {
        progressBarLayout.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        recyclerView.visibility = if (state.showResults) View.VISIBLE else View.GONE
        nothingFound.visibility = if (state.showNothingFound) View.VISIBLE else View.GONE
        connectionProblem.visibility = if (state.showError) View.VISIBLE else View.GONE
        searchHistoryLayout.visibility = if (state.showHistory) View.VISIBLE else View.GONE

        if (state.results.isNotEmpty()) {
            trackAdapter.updateTracks(state.results)
        }

        if (state.history.isNotEmpty()) {
            historyAdapter.updateTracks(state.history)
        }
    }

    private fun openPlayerActivity(track: Track) {
        val displayIntent = Intent(this, PlayerActivity::class.java)
        val strTrack = Gson().toJson(track)
        displayIntent.putExtra(NAME_TRACK, strTrack)
        startActivity(displayIntent)
    }
}
