package edu.rutgers.knighttransporter.for_non_mapbox_queries

import edu.rutgers.knighttransporter.buildingsPath
import edu.rutgers.knighttransporter.parkingLotsPath
import edu.rutgers.knighttransporter.walkwaysPath
import retrofit2.http.GET

interface GeoJSONService {
    @GET(walkwaysPath)
    suspend fun getWalkways(): String

    @GET(parkingLotsPath)
    suspend fun getParkingLots(): String

    @GET(buildingsPath)
    suspend fun getBuildings(): String
}
