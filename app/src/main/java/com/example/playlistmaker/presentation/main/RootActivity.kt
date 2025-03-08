// RootActivity.kt
package com.example.playlistmaker.presentation.main

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.playlistmaker.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class RootActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Здесь используем ADJUST_PAN: клавиатура поднимается поверх экрана,
        // нижняя панель при этом остается на месте (будет под клавиатурой, если клавиатура выше).
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        setContentView(R.layout.root_activity)

        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.rootFragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController

        bottomNavigationView.setupWithNavController(navController)

        // Показываем нижнюю панель всегда, кроме экрана PlaylistInfoFragment
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.playlistInfoFragment -> bottomNavigationView.visibility = android.view.View.GONE
                else -> bottomNavigationView.visibility = android.view.View.VISIBLE
            }
        }
    }
}
