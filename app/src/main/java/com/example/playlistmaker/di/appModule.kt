package com.example.playlistmaker.di

import android.content.Context
import android.content.SharedPreferences
import org.koin.dsl.module

private const val PREFERENCES = "com.example.playlistmaker.PREFERENCES"

val appModule = module {
    single<SharedPreferences> {
        get<Context>().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
    }
}
