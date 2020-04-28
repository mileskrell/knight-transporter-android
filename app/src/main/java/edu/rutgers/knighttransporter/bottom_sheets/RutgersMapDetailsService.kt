package edu.rutgers.knighttransporter.bottom_sheets

import retrofit2.http.GET
import retrofit2.http.Path

interface RutgersMapDetailsService {
    companion object {
        const val BASE_URL = "https://storage.googleapis.com/rutgers-campus-map-public-data-prod/"
    }

    @GET("archibus-data/building-details/{code}.json")
    suspend fun getBuildingDetails(@Path("code") buildingCode: Int): BuildingDetails
}
