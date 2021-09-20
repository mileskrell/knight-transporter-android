package edu.rutgers.knighttransporter.bottom_sheets

import retrofit2.http.GET
import retrofit2.http.Path

interface RutgersCloudStorageService {

    @GET("archibus-data/building-details/{num}.json")
    suspend fun getBuildingCloudStorageDetails(@Path("num") buildingNumber: Int): BuildingCloudStorageDetails

    companion object {
        const val BASE_URL = "https://storage.googleapis.com/rutgers-campus-map-public-data-prod/"
    }
}
