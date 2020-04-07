package edu.rutgers.knighttransporter

import android.os.Bundle
import androidx.lifecycle.ViewModel
import edu.rutgers.knighttransporter.for_non_mapbox_queries.Repository

class MapViewModel : ViewModel() {
    val repository = Repository()
    val mapInstanceState = Bundle()

    suspend fun getWalkways() = repository.getWalkways()

    suspend fun getParkingLots() = repository.getParkingLots()

    suspend fun getBuildings() = repository.getBuildings()
}
