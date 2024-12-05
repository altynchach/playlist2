package com.example.playlistmaker.presentation.search

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.Creator
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.interactor.SearchInteractor
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.presentation.adapters.TrackAdapter
import com.example.playlistmaker.presentation.player.PlayerActivity
import com.google.gson.Gson

class SearchActivity : AppCompatActivity() {

    companion object {
        private const val SEARCH_QUERY_KEY = "SEARCH_QUERY"
        private const val NAME_TRACK = "name"
    }

    private lateinit var inputText: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var trackAdapter: TrackAdapter
    private lateinit var nothingFound: LinearLayout
    private lateinit var connectionProblem: LinearLayout
    private lateinit var reloadButton: Button
    private lateinit var progressBarLayout: FrameLayout
    private var searchText: String = ""

    private lateinit var searchInteractor: SearchInteractor
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var clearHistoryButton: Button
    private lateinit var searchHistoryLayout: LinearLayout
    private lateinit var historyAdapter: TrackAdapter

    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private var lastClickTime: Long = 0

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // Initialize the SearchInteractor
        searchInteractor = Creator.provideSearchInteractor(applicationContext)

        // Initialize UI elements
        inputText = findViewById(R.id.inputEditText)
        recyclerView = findViewById(R.id.recyclerView)
        nothingFound = findViewById(R.id.nothingFound)
        connectionProblem = findViewById(R.id.connectionProblem)
        reloadButton = findViewById(R.id.reload_button)
        progressBarLayout = findViewById(R.id.progress_bar_layout)

        // Setup RecyclerView for search results
        recyclerView.layoutManager = LinearLayoutManager(this)
        trackAdapter = TrackAdapter(arrayListOf())
        recyclerView.adapter = trackAdapter

        // Setup RecyclerView for search history
        historyRecyclerView = findViewById(R.id.search_history_recycler)
        searchHistoryLayout = findViewById(R.id.search_history_layout)
        clearHistoryButton = findViewById(R.id.clear_history_button)

        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyAdapter = TrackAdapter(arrayListOf())
        historyRecyclerView.adapter = historyAdapter

        // Clear history button click listener
        clearHistoryButton.setOnClickListener {
            searchInteractor.clearSearchHistory()
            displaySearchHistory()
        }

        // Track item click listener for search results
        trackAdapter.setOnTrackClickListener { track ->
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime > 2000) {  // Prevent multiple rapid clicks
                lastClickTime = currentTime
                searchInteractor.saveTrackToHistory(track)
                val displayIntent = Intent(this, PlayerActivity::class.java)
                val strTrack = Gson().toJson(track)
                displayIntent.putExtra(NAME_TRACK, strTrack)
                startActivity(displayIntent)
            }
        }

        // Track item click listener for history items
        historyAdapter.setOnTrackClickListener { track ->
            val displayIntent = Intent(this, PlayerActivity::class.java)
            val strTrack = Gson().toJson(track)
            displayIntent.putExtra(NAME_TRACK, strTrack)
            startActivity(displayIntent)
        }

        // Text change listener for search input
        inputText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                searchText = s.toString()
                updateClearButtonVisibility()
                searchRunnable?.let { handler.removeCallbacks(it) }
                searchRunnable = Runnable {
                    if (searchText.isEmpty()) {
                        displaySearchHistory()
                    } else {
                        searchHistoryLayout.visibility = View.GONE
                        searchTracks(searchText)
                    }
                }
                handler.postDelayed(searchRunnable!!, 2000) // Delay of 2 seconds
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Focus change listener for search input
        inputText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && inputText.text.isEmpty()) {
                displaySearchHistory()
            } else {
                searchHistoryLayout.visibility = View.GONE
            }
        }

        updateClearButtonVisibility()

        // Clear button functionality in the search input
        inputText.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = 2
                if (inputText.compoundDrawables[drawableEnd] != null &&
                    event.rawX >= (inputText.right - inputText.compoundDrawables[drawableEnd].bounds.width())
                ) {
                    inputText.text.clear()
                    hideKeyboard(v)
                    updateClearButtonVisibility()
                    recyclerView.visibility = View.GONE
                    nothingFound.visibility = View.GONE
                    connectionProblem.visibility = View.GONE
                    displaySearchHistory()
                    return@setOnTouchListener true
                }
            }
            false
        }

        // Restore search query on configuration change
        if (savedInstanceState != null) {
            searchText = savedInstanceState.getString(SEARCH_QUERY_KEY, "")
            inputText.setText(searchText)
        }

        // Back button functionality
        val backButton2 = findViewById<ImageView>(R.id.back_button2)
        backButton2.setOnClickListener {
            finish()
        }

        // Reload button functionality
        reloadButton.setOnClickListener {
            searchTracks(searchText)
        }

        displaySearchHistory()
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

    private fun searchTracks(query: String) {
        if (query.isEmpty()) {
            recyclerView.visibility = View.GONE
            nothingFound.visibility = View.GONE
            connectionProblem.visibility = View.GONE
            displaySearchHistory()
            return
        }

        progressBarLayout.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        nothingFound.visibility = View.GONE
        connectionProblem.visibility = View.GONE

        searchInteractor.searchTracks(query, { tracks ->
            progressBarLayout.visibility = View.GONE
            if (tracks.isNotEmpty()) {
                trackAdapter.updateTracks(ArrayList(tracks))
                recyclerView.visibility = View.VISIBLE
                nothingFound.visibility = View.GONE
            } else {
                recyclerView.visibility = View.GONE
                nothingFound.visibility = View.VISIBLE
            }
        }, {
            progressBarLayout.visibility = View.GONE
            recyclerView.visibility = View.GONE
            connectionProblem.visibility = View.VISIBLE
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun displaySearchHistory() {
        val history = searchInteractor.getSearchHistory()
        if (inputText.hasFocus() && inputText.text.isEmpty() && history.isNotEmpty()) {
            searchHistoryLayout.visibility = View.VISIBLE
            historyAdapter.updateTracks(ArrayList(history))
            historyAdapter.notifyDataSetChanged()
        } else {
            searchHistoryLayout.visibility = View.GONE
        }
    }
}
