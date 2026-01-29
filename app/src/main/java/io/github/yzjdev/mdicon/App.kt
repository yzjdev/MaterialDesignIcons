package io.github.yzjdev.mdicon

import android.app.Application
import android.content.Context
import com.google.android.material.color.DynamicColors

class App : Application() {
    companion object {
        lateinit var instance: App
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}