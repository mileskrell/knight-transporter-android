package edu.rutgers.knighttransporter.for_non_mapbox_queries

data class WalkwaysResponse(
    val type: String,
    val crs: CRS,
    val features: List<Feature>
) {
    data class CRS(
        val type: String,
        val properties: Properties
    )

    data class Properties(
        val name: String
    )

    data class Feature(
        val type: String,
        val geometry: Geometry,
        val properties: FeatureProperties
    )

    data class Geometry(
        val type: String,
        val coordinates: List<List<List<List<Double>>>>
    )

    class FeatureProperties
}
