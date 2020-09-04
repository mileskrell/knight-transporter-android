package edu.rutgers.knighttransporter

import android.animation.AnimatorSet
import android.app.Application
import android.content.SharedPreferences
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.mapbox.geojson.Feature
import com.mapbox.mapboxsdk.geometry.LatLng
import edu.rutgers.knighttransporter.feature_stuff.PlaceType
import edu.rutgers.knighttransporter.for_transloc.Route
import edu.rutgers.knighttransporter.for_transloc.StopMarkerData

class MapViewModel(app: Application) : AndroidViewModel(app) {
    private val _routes = MutableLiveData<List<Route>>(emptyList())
    val routes: LiveData<List<Route>>
        get() = _routes

    private val sharedPreferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(app.applicationContext)

    // From stop ID to StopMarkerData
    // TODO: Once data is over some age, display some kind of warning
    val stopIdToMarkerDataMap = MutableLiveData(mutableMapOf<Int, StopMarkerData>())

    // Initialized as soon as the map's style is loaded
    lateinit var firstLabelLayerId: String

    var buildingItems = emptyList<RutgersPlacesSearchAdapter.AdapterPlaceItem>()
    var parkingLotItems = emptyList<RutgersPlacesSearchAdapter.AdapterPlaceItem>()
    var stopItems = emptyList<RutgersPlacesSearchAdapter.AdapterPlaceItem>()
    var vehicleItems = emptyList<RutgersPlacesSearchAdapter.AdapterPlaceItem>()
    lateinit var searchAdapter: RutgersPlacesSearchAdapter

    var selectedFeature: Feature? = null
    var selectedPlaceType: PlaceType? = null
    var tappedLatLng: LatLng? = null

    // The starting place for the vehicle animations
    var previousVehicleFeatures = emptyList<Feature>()

    // This lets us cancel the vehicle animations if we get new data before they finish
    var vehiclesAnimatorSet = AnimatorSet()

    private val repository = Repository { newRoutes ->
        _routes.postValue(newRoutes)
    }

    val mapInstanceState = Bundle()

    suspend fun getWalkways() = repository.getWalkways(sharedPreferences)

    suspend fun getRoutePolylines() = repository.getRoutePolylines(sharedPreferences)

    suspend fun getParkingLots() = repository.getParkingLots(sharedPreferences)

    suspend fun getBuildings() = repository.getBuildings(sharedPreferences)

    suspend fun getBuildingArcGISDetails(buildingNumber: Int) =
        repository.getBuildingArcGISDetails(buildingNumber)

    suspend fun getBuildingCloudStorageDetails(buildingNumber: Int) =
        repository.getBuildingCloudStorageDetails(buildingNumber)
}
