package edu.rutgers.knighttransporter.for_transloc

import com.google.gson.annotations.SerializedName

/**
 * A route as received from the Socket.IO server (https://github.com/RidhwaanDev/rutgersql)
 */
data class Route(
    @SerializedName("agency_id") val agencyId: Int,
    val color: String,
    val description: String,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("is_hidden") val isHidden: Boolean,
    @SerializedName("long_name") val longName: String,
    @SerializedName("route_id") val routeId: Int,
    @SerializedName("short_name") val shortName: String,
    val stops: List<Stop>,
    @SerializedName("text_color") val textColor: String,
    val type: String,
    val url: String,
    val vehicles: List<Vehicle>
) {
    data class Stop(
        @SerializedName("agency_ids") val agencyIds: List<Int>,
        val arrivals: List<Arrival>,
        val code: Int,
        val description: String,
        val location: Location,
        @SerializedName("location_type") val locationType: String,
        val name: String,
        @SerializedName("parent_station_id") val parentStationId: Any?,
        val routes: List<String>,
        @SerializedName("station_id") val stationId: Any?,
        @SerializedName("stop_id") val stopId: Int,
        val url: String
    ) {
        data class Arrival(
            @SerializedName("arrival_at") val arrivalAt: String,
            @SerializedName("route_id") val routeId: Int,
            val type: String,
            @SerializedName("vehicle_id") val vehicleId: Int
        )
    }

    data class Vehicle(
        @SerializedName("arrival_estimates") val arrivalEstimates: List<ArrivalEstimate>,
        @SerializedName("call_name") val callName: String,
        val description: Any?,
        val heading: Int,
        @SerializedName("last_updated_on") val lastUpdatedOn: String,
        val location: Location,
        @SerializedName("passenger_load") val passengerLoad: Double,
        @SerializedName("route_id") val routeId: Int,
        @SerializedName("seating_capacity") val seatingCapacity: Any?,
        @SerializedName("segment_id") val segmentId: Int,
        val speed: Double,
        @SerializedName("standing_capacity") val standingCapacity: Any?,
        @SerializedName("tracking_status") val trackingStatus: String,
        @SerializedName("vehicle_id") val vehicleId: Int
    ) {
        data class ArrivalEstimate(
            @SerializedName("arrival_at") val arrivalAt: String,
            val name: String,
            @SerializedName("route_id") val routeId: Int,
            @SerializedName("stop_id") val stopId: Int
        )
    }

    data class Location(
        val lat: Double,
        val lng: Double
    )
}
