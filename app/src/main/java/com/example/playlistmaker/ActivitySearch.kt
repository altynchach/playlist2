package com.example.playlistmaker

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class ActivitySearch : AppCompatActivity() {

    private lateinit var inputText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        inputText = findViewById(R.id.inputEditText)

        inputText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
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

        inputText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = 2 // Index of the drawableEnd
                if (inputText.compoundDrawables[drawableEnd] != null && event.rawX >= (inputText.right - inputText.compoundDrawables[drawableEnd].bounds.width())) {
                    inputText.text.clear()
                    hideKeyboard()
                    updateClearButtonVisibility()
                    return@setOnTouchListener true
                }
            }
            false
        }

        if (savedInstanceState != null) {
            val restoredText = savedInstanceState.getString("SEARCH_QUERY", "")
            inputText.setText(restoredText)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("SEARCH_QUERY", inputText.text.toString())
    }

    private fun showKeyboard() {
        inputText.requestFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(inputText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(inputText.windowToken, 0)
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
