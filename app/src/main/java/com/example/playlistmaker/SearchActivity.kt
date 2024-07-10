package com.example.playlistmaker

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.retrofit.ITunesApi
import com.example.playlistmaker.retrofit.TracksResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchActivity : AppCompatActivity() {

    companion object {
        const val SEARCH_QUERY_KEY = "SEARCH_QUERY"
    }

    private lateinit var inputText: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var trackAdapter: TrackAdapter
    private lateinit var nothingFound: LinearLayout
    private lateinit var connectionProblem: LinearLayout
    private lateinit var reloadButton: Button
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

        recyclerView.layoutManager = LinearLayoutManager(this)
        trackAdapter = TrackAdapter(arrayListOf())
        recyclerView.adapter = trackAdapter

        inputText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                searchText = s.toString()
                updateClearButtonVisibility()
                filterTracks(searchText)
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
                val drawableEnd = 2
                if (inputText.compoundDrawables[drawableEnd] != null && event.rawX >= (inputText.right - inputText.compoundDrawables[drawableEnd].bounds.width())) {
                    inputText.text.clear()
                    hideKeyboard(v)
                    updateClearButtonVisibility()
                    filterTracks("")
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
        val retrofit = Retrofit.Builder()
            .baseUrl("https://itunes.apple.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ITunesApi::class.java)
        api.search(query).enqueue(object : Callback<TracksResponse> {
            override fun onResponse(call: Call<TracksResponse>, response: Response<TracksResponse>) {
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
                recyclerView.visibility = View.GONE
                nothingFound.visibility = View.GONE
                connectionProblem.visibility = View.VISIBLE
            }
        })
    }
}
