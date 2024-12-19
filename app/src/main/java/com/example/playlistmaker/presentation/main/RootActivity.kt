package com.example.playlistmaker.presentation.main

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.playlistmaker.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class RootActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "RootActivityLog"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Activity is being created")

        setContentView(R.layout.root_activity)
        Log.d(TAG, "onCreate: setContentView completed")

        val navController = findNavController(R.id.rootFragmentContainerView)
        Log.d(TAG, "onCreate: navController obtained successfully")

        val bottomNavView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        Log.d(TAG, "onCreate: bottomNavView obtained successfully")

        bottomNavView.setupWithNavController(navController)
        Log.d(TAG, "onCreate: bottomNavView setup with navController completed")
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: Activity is visible")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: Activity is in the foreground")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: Activity is going into the background")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: Activity is no longer visible")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: Activity is being destroyed")
    }
}
