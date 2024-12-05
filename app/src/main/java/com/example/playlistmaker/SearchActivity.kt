package com.example.playlistmaker

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
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.presentation.main.MainActivity
import com.example.playlistmaker.recyclerView.Track
import com.example.playlistmaker.recyclerView.TrackAdapter
import com.example.playlistmaker.data.network.ITunesApi
import com.example.playlistmaker.retrofit.TracksResponse
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchActivity : AppCompatActivity() {

    companion object {
        const val SEARCH_QUERY_KEY = "SEARCH_QUERY"
        const val ITUNES_URL = "https://itunes.apple.com"
        const val NAME_TRACK = "name"
    }

    private lateinit var inputText: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var trackAdapter: TrackAdapter
    private lateinit var nothingFound: LinearLayout
    private lateinit var connectionProblem: LinearLayout
    private lateinit var reloadButton: Button
    private lateinit var progressBarLayout: FrameLayout
    private var searchText: String = ""

    private lateinit var searchHistory: SearchHistory
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

        val sharedPreferences = getSharedPreferences("com.example.playlistmaker.PREFERENCES", MODE_PRIVATE)
        searchHistory = SearchHistory(sharedPreferences)

        inputText = findViewById(R.id.inputEditText)
        recyclerView = findViewById(R.id.recyclerView)
        nothingFound = findViewById(R.id.nothingFound)
        connectionProblem = findViewById(R.id.connectionProblem)
        reloadButton = findViewById(R.id.reload_button)
        progressBarLayout = findViewById(R.id.progress_bar_layout)

        recyclerView.layoutManager = LinearLayoutManager(this)
        trackAdapter = TrackAdapter(arrayListOf())
        recyclerView.adapter = trackAdapter

        historyRecyclerView = findViewById(R.id.search_history_recycler)
        searchHistoryLayout = findViewById(R.id.search_history_layout)
        clearHistoryButton = findViewById(R.id.clear_history_button)

        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyAdapter = TrackAdapter(searchHistory.getHistory().toMutableList() as ArrayList<Track>)
        historyRecyclerView.adapter = historyAdapter

        clearHistoryButton.setOnClickListener {
            searchHistory.clearHistory()
            displaySearchHistory()
        }

        trackAdapter.setOnTrackClickListener { track ->
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime > 2000) {  // Защита от многократных нажатий
                lastClickTime = currentTime
                searchHistory.saveTrack(track)
                val displayIntent = Intent(this, PlayerActivity::class.java)
                val strTrack = Gson().toJson(track)
                displayIntent.putExtra(NAME_TRACK, strTrack)
                startActivity(displayIntent)
            }
        }

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
                        filterTracks(searchText)
                    }
                }
                handler.postDelayed(searchRunnable!!, 2000) // Задержка 2 секунды
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        inputText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && inputText.text.isEmpty()) {
                displaySearchHistory()
            } else {
                searchHistoryLayout.visibility = View.GONE
            }
        }

        updateClearButtonVisibility()

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
                    displaySearchHistory()
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

        reloadButton.setOnClickListener {
            filterTracks(searchText)
        }

        displaySearchHistory()
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

    private fun filterTracks(query: String) {
        if (query.isEmpty()) {
            recyclerView.visibility = View.GONE
            nothingFound.visibility = View.GONE
            connectionProblem.visibility = View.GONE
            displaySearchHistory()
            return
        }

        // Отображение контейнера прогресс-бара перед началом поиска
        progressBarLayout.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE

        val retrofit = Retrofit.Builder()
            .baseUrl(ITUNES_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(ITunesApi::class.java)
        api.search(query).enqueue(object : Callback<TracksResponse> {
            override fun onResponse(call: Call<TracksResponse>, response: Response<TracksResponse>) {
                // Скрываем контейнер прогресс-бара после завершения запроса
                progressBarLayout.visibility = View.GONE
                if (response.isSuccessful && response.body()?.results?.isNotEmpty() == true) {
                    val tracks = response.body()!!.results
                    trackAdapter.updateTracks(tracks)
                    recyclerView.visibility = View.VISIBLE
                    nothingFound.visibility = View.GONE
                    connectionProblem.visibility = View.GONE
                } else {
                    recyclerView.visibility = View.GONE
                    nothingFound.visibility = View.VISIBLE
                    connectionProblem.visibility = View.GONE
                }
            }

            override fun onFailure(call: Call<TracksResponse>, t: Throwable) {
                // Скрываем контейнер прогресс-бара при ошибке запроса
                progressBarLayout.visibility = View.GONE
                recyclerView.visibility = View.GONE
                nothingFound.visibility = View.GONE
                connectionProblem.visibility = View.VISIBLE
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun displaySearchHistory() {
        val history = searchHistory.getHistory()
        if (inputText.hasFocus() && inputText.text.isEmpty() && history.isNotEmpty()) {
            searchHistoryLayout.visibility = View.VISIBLE
            historyAdapter.updateTracks(history.toMutableList() as ArrayList<Track>)
            historyAdapter.notifyDataSetChanged()

            historyAdapter.setOnTrackClickListener { track ->
                val displayIntent = Intent(this, PlayerActivity::class.java)
                val strTrack = Gson().toJson(track)
                displayIntent.putExtra(NAME_TRACK, strTrack)
                startActivity(displayIntent)
            }
        } else {
            searchHistoryLayout.visibility = View.GONE
        }
    }
}
