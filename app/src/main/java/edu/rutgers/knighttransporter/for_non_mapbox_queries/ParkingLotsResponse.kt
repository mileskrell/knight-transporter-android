package edu.rutgers.knighttransporter.for_non_mapbox_queries

import com.google.gson.annotations.SerializedName

data class ParkingLotsResponse(
    val type: String,
    val crs: CRS,
    val features: List<Feature>
) {
    data class CRS(
        val type: String,
        val properties: CRSProperties
    )

    data class CRSProperties(
        val name: String
    )

    data class Feature(
        val type: String,
        val geometry: Geometry,
        val properties: FeatureProperties
    )

    data class Geometry(
        val type: String,
        // the Any is either a Double or List<Double>, depending on
        // whether this is a Polygon or MultiPolygon
        val coordinates: List<List<List<Any>>>
    )

    data class FeatureProperties(
        @SerializedName("Parking_ID") val parkingID: String,
        @SerializedName("Lot_Name") val lotName: String,
        @SerializedName("Latitude") val latitude: Double,
        @SerializedName("Longitude") val longitude: Double
    )
}
