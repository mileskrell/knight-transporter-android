package edu.rutgers.knighttransporter

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import edu.rutgers.knighttransporter.for_transloc.Route
import edu.rutgers.knighttransporter.for_transloc.StopMarkerData

class MapViewModel : ViewModel() {
    private val _routes = MutableLiveData<List<Route>>(emptyList())
    val routes: LiveData<List<Route>>
        get() = _routes

    // From stop ID to StopMarkerData
    // TODO: Once data is over some age, display some kind of warning
    val stopIdToMarkerDataMap = MutableLiveData(mutableMapOf<Int, StopMarkerData>())

    // Initialized as soon as the map's style is loaded
    lateinit var firstLabelLayerId: String

    private val repository = Repository { newRoutes ->
        _routes.postValue(newRoutes)
    }

    val mapInstanceState = Bundle()

    suspend fun getWalkways() = repository.getWalkways()

    suspend fun getParkingLots() = repository.getParkingLots()

    suspend fun getBuildings() = repository.getBuildings()

    suspend fun getBuildingCloudStorageDetails(buildingCode: Int) = repository.getBuildingCloudStorageDetails(buildingCode)
}
