package edu.rutgers.knighttransporter.bottom_sheets

import com.google.gson.annotations.SerializedName

data class BuildingDetails (
    @SerializedName("city_id") val cityID: String,
    val zip: String,
    val departments: List<String>?
)
