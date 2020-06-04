package edu.rutgers.knighttransporter

import android.app.Application
import com.mapbox.mapboxsdk.Mapbox

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Mapbox.getInstance(applicationContext, mapboxToken)
    }
}
