package edu.rutgers.knighttransporter.bottom_sheets

import com.google.gson.annotations.SerializedName

data class BuildingCloudStorageDetails(
    @SerializedName("city_id") val cityID: String,
    val zip: String,
    val departments: List<String>?
)
