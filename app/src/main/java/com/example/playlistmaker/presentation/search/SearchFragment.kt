// com/example/playlistmaker/presentation/search/SearchFragment.kt
package com.example.playlistmaker.presentation.search

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.presentation.player.PlayerActivity
import com.example.playlistmaker.presentation.search.adapters.TrackAdapter
import com.google.gson.Gson
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {

    companion object {
        const val NAME_TRACK = "name"
    }

    private val viewModel: SearchViewModel by viewModel()

    private lateinit var inputText: EditText
    private lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var trackAdapter: TrackAdapter
    private lateinit var nothingFound: LinearLayout
    private lateinit var connectionProblem: LinearLayout
    private lateinit var reloadButton: Button
    private lateinit var progressBarLayout: FrameLayout
    private lateinit var searchHistoryLayout: LinearLayout
    private lateinit var historyRecyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var clearHistoryButton: Button

    private var lastClickTime: Long = 0
    private var searchText: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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

        inputText.addTextChangedListener { s ->
            searchText = s.toString()
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

        reloadButton.setOnClickListener {
            viewModel.onReloadClicked()
        }

        if (savedInstanceState != null) {
            searchText = savedInstanceState.getString("SEARCH_QUERY", "")
            inputText.setText(searchText)
        }

        viewModel.getState().observe(viewLifecycleOwner) { state ->
            renderState(state, historyAdapter)
        }

        viewModel.init()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("SEARCH_QUERY", searchText)
    }

    private fun hideKeyboard(view: View) {
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun updateClearButtonVisibility() {
        if (inputText.text.isNotEmpty()) {
            inputText.setCompoundDrawablesWithIntrinsicBounds(
                requireContext().getDrawable(R.drawable.search), null,
                requireContext().getDrawable(R.drawable.clear), null
            )
        } else {
            inputText.setCompoundDrawablesWithIntrinsicBounds(
                requireContext().getDrawable(R.drawable.search), null,
                null, null
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
        val displayIntent = Intent(requireContext(), PlayerActivity::class.java)
        val strTrack = Gson().toJson(track)
        displayIntent.putExtra(NAME_TRACK, strTrack)
        startActivity(displayIntent)
    }
}
