package edu.rutgers.knighttransporter

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mapbox.mapboxsdk.plugins.annotation.Symbol
import edu.rutgers.knighttransporter.for_transloc.Route
import edu.rutgers.knighttransporter.for_transloc.StopMarkerData

class MapViewModel : ViewModel() {
    private val _routes = MutableLiveData<List<Route>>(emptyList())
    val routes: LiveData<List<Route>>
        get() = _routes

    val stopMarkers = mutableMapOf<Long, StopMarkerData>()
    var selectedStopMarker: Symbol? = null

    private val repository = Repository { newRoutes ->
        _routes.postValue(newRoutes)
    }

    val mapInstanceState = Bundle()

    suspend fun getWalkways() = repository.getWalkways()

    suspend fun getParkingLots() = repository.getParkingLots()

    suspend fun getBuildings() = repository.getBuildings()
}
