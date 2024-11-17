package com.litdev.orbimaze

import android.app.Application

class ApplicationClass: Application() {
    var firstTime: Boolean = true
    var level: Int = 1

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
        val editor = sharedPreferences.edit()
        editor.putInt("version", version)
        editor.putInt("level", level)
        editor.apply()
    }

    fun load() {
        val sharedPreferences = getSharedPreferences("Stored", MODE_PRIVATE)
        if (sharedPreferences.contains("version")) {
            firstTime = false
        }
        val saved_version = sharedPreferences.getInt("version", 0)
        if (saved_version > version) {
            TODO("Version too recent")
        }
        level = sharedPreferences.getInt("level", 1)
    }
}