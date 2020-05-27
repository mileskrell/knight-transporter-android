package edu.rutgers.knighttransporter

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mapbox.geojson.Feature
import edu.rutgers.knighttransporter.feature_stuff.PlaceType
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

    var buildingItems = emptyList<RutgersPlacesSearchAdapter.AdapterPlaceItem>()
    var parkingLotItems = emptyList<RutgersPlacesSearchAdapter.AdapterPlaceItem>()
    var stopItems = emptyList<RutgersPlacesSearchAdapter.AdapterPlaceItem>()
    var vehicleItems = emptyList<RutgersPlacesSearchAdapter.AdapterPlaceItem>()
    lateinit var searchAdapter: RutgersPlacesSearchAdapter

    var selectedFeature: Feature? = null
    var selectedPlaceType: PlaceType? = null

    var vehiclesHaveBeenAdded = false
    var previousVehicleFeatures = emptyList<Feature>()
    var latestVehicleFeatures = emptyList<Feature>()
    var interpolatedVehicleFeatures = MutableLiveData(emptyList<Feature>())
    var animatorSet = AnimatorSet()


    private val repository = Repository { newRoutes ->
        _routes.postValue(newRoutes)
    }

    val mapInstanceState = Bundle()

    suspend fun getWalkways() = repository.getWalkways()

    suspend fun getParkingLots() = repository.getParkingLots()

    suspend fun getBuildings() = repository.getBuildings()

    suspend fun getBuildingArcGISDetails(buildingNumber: Int) =
        repository.getBuildingArcGISDetails(buildingNumber)

    suspend fun getBuildingCloudStorageDetails(buildingNumber: Int) =
        repository.getBuildingCloudStorageDetails(buildingNumber)
}
