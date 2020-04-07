package edu.rutgers.knighttransporter.for_non_mapbox_queries

import edu.rutgers.knighttransporter.baseUrl
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Repository {
    var walkways: WalkwaysResponse? = null
    var parkingLots: ParkingLotsResponse? = null
    var buildings: BuildingsResponse? = null

    val service = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GeoJSONService::class.java)

    suspend fun getWalkways() = walkways ?: service.getWalkways().also {
        walkways = it
    }

    suspend fun getParkingLots() = parkingLots ?: service.getParkingLots().also {
        parkingLots = it
    }

    suspend fun getBuildings() = buildings ?: service.getBuildings().also {
        buildings = it
    }
}
