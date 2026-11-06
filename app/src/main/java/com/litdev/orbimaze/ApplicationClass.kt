package com.litdev.orbimaze

import android.app.Application
import androidx.core.content.edit

class ApplicationClass: Application() {
    var firstTime: Boolean = true
    var gameCompleted: Boolean = false
    var level: Int = 1
    val maxLevel = 10

    val version: Int = 1

    companion object {
        lateinit var instance: ApplicationClass
            private set
    }

    override fun onCreate() {
        super.onCreate()

        load()
        if (firstTime) {
            save()
        }
        firstTime = false
        instance = this
    }

    fun save() {
        val sharedPreferences = getSharedPreferences("Stored", MODE_PRIVATE)
        sharedPreferences.edit {
            putInt("version", version)
            putInt("level", level)
            putBoolean("gameCompleted", gameCompleted)
        }
    }

    fun load() {
        val sharedPreferences = getSharedPreferences("Stored", MODE_PRIVATE)
        if (sharedPreferences.contains("version")) {
            firstTime = false
        }
        val savedVersion = sharedPreferences.getInt("version", 0)
        if (savedVersion > version) {
            TODO("Version too recent")
        }
        level = sharedPreferences.getInt("level", 1)
        gameCompleted = sharedPreferences.getBoolean("gameCompleted", false)
    }
}