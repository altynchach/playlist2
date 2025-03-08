package com.example.playlistmaker.presentation.main

import android.graphics.Rect
import android.os.Bundle
import android.view.View
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
        // Чтобы клавиатура «поднимала» контент, а не перекрывала его:
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        setContentView(R.layout.root_activity)

        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        // Инициализируем NavHost и NavController
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.rootFragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController

        // Связываем BottomNavigationView с NavController
        bottomNavigationView.setupWithNavController(navController)

        // Слушатель изменений маршрута: прячем BottomNav на экране плейлиста
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.playlistInfoFragment -> {
                    // Скрываем нижнюю панель на экране «PlaylistInfoFragment»
                    bottomNavigationView.visibility = View.GONE
                }
                else -> {
                    // На остальных экранах показываем
                    bottomNavigationView.visibility = View.VISIBLE
                }
            }
        }

        // Логика, которая скрывает BottomNav при открытии клавиатуры
        val rootView = findViewById<View>(android.R.id.content)
        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            rootView.getWindowVisibleDisplayFrame(rect)

            val screenHeight = rootView.rootView.height
            val keypadHeight = screenHeight - rect.bottom

            if (keypadHeight > 100) {
                // Если клавиатура открыта, прячем панель
                bottomNavigationView.visibility = View.GONE
            } else {
                // Иначе возвращаем видимость, но с учётом текущего экрана
                val currentDestId = navController.currentDestination?.id
                if (currentDestId == R.id.playlistInfoFragment) {
                    bottomNavigationView.visibility = View.GONE
                } else {
                    bottomNavigationView.visibility = View.VISIBLE
                }
            }
        }
    }
}
