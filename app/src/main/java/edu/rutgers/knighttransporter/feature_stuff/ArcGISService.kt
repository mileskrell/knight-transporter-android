package edu.rutgers.knighttransporter.feature_stuff

import retrofit2.http.GET

interface ArcGISService {
    @GET(walkwaysPath)
    suspend fun getWalkways(): String

    @GET(parkingLotsPath)
    suspend fun getParkingLots(): String

    @GET(buildingsPath)
    suspend fun getBuildings(): String

    @GET(popularDestinationsPath)
    suspend fun getPopularDestinations(): String

    @GET(buildingArcGISDetailsPath)
    suspend fun getBuildingsArcGISDetails(): String
}
