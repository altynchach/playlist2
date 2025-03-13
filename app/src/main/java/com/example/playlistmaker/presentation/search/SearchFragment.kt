package com.example.playlistmaker.presentation.search

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.presentation.player.PlayerActivity
import com.example.playlistmaker.presentation.search.adapters.TrackAdapter
import com.example.playlistmaker.presentation.search.SearchScreenState
import com.google.gson.Gson
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {

    companion object {
        const val NAME_TRACK = "TRACK_DATA"
        const val SEARCH_QUERY_KEY = "SEARCH_QUERY"
    }

    private val viewModel: SearchViewModel by viewModel()

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

    private var searchText: String = ""
    private var clickJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_search, container, false)

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val rootContainer = view.findViewById<View>(R.id.searchRootContainer)

        inputText = view.findViewById(R.id.inputEditText)
        recyclerView = view.findViewById(R.id.recyclerView)
        nothingFound = view.findViewById(R.id.nothingFound)
        connectionProblem = view.findViewById(R.id.connectionProblem)
        reloadButton = view.findViewById(R.id.reload_button)
        progressBarLayout = view.findViewById(R.id.progress_bar_layout)
        searchHistoryLayout = view.findViewById(R.id.search_history_layout)
        historyRecyclerView = view.findViewById(R.id.search_history_recycler)
        clearHistoryButton = view.findViewById(R.id.clear_history_button)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        trackAdapter = TrackAdapter(arrayListOf())
        recyclerView.adapter = trackAdapter

        historyRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        val historyAdapter = TrackAdapter(arrayListOf())
        historyRecyclerView.adapter = historyAdapter

        clearHistoryButton.setOnClickListener {
            viewModel.clearHistory()
        }

        trackAdapter.setOnTrackClickListener { track ->
            if (clickJob?.isActive == true) return@setOnTrackClickListener
            clickJob = viewLifecycleOwner.lifecycleScope.launch {
                viewModel.saveTrackToHistory(track)
                openPlayerActivity(track)
                delay(2000)
            }
        }

        historyAdapter.setOnTrackClickListener { track ->
            openPlayerActivity(track)
        }

        inputText.addTextChangedListener {
            searchText = it.toString()
            viewModel.onSearchQueryChanged(searchText)
            updateClearButtonVisibility()
        }

        inputText.setOnFocusChangeListener { _, hasFocus ->
            viewModel.onFocusChanged(hasFocus)
        }

        inputText.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = 2
                val drawable = inputText.compoundDrawables[drawableEnd]
                if (drawable != null && event.rawX >= (inputText.right - drawable.bounds.width())) {
                    inputText.text.clear()
                    hideKeyboard(v)
                    return@setOnTouchListener true
                }
            }
            false
        }

        reloadButton.setOnClickListener {
            viewModel.onReloadClicked()
        }

        if (savedInstanceState != null) {
            searchText = savedInstanceState.getString(SEARCH_QUERY_KEY, "")
            inputText.setText(searchText)
        }

        viewModel.getState().observe(viewLifecycleOwner) { state ->
            renderState(state, historyAdapter)
        }
        viewModel.init()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_QUERY_KEY, searchText)
    }

    private fun hideKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun updateClearButtonVisibility() {
        if (inputText.text.isNotEmpty()) {
            inputText.setCompoundDrawablesWithIntrinsicBounds(
                requireContext().getDrawable(R.drawable.search),
                null,
                requireContext().getDrawable(R.drawable.clear),
                null
            )
        } else {
            inputText.setCompoundDrawablesWithIntrinsicBounds(
                requireContext().getDrawable(R.drawable.search),
                null,
                null,
                null
            )
        }
    }

    private fun renderState(state: SearchScreenState, historyAdapter: TrackAdapter) {
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
        val ctx = context ?: return
        val displayIntent = Intent(ctx, PlayerActivity::class.java)
        displayIntent.putExtra(NAME_TRACK, Gson().toJson(track))
        startActivity(displayIntent)
    }
}
