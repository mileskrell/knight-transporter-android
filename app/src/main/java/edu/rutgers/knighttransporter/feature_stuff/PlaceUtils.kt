package edu.rutgers.knighttransporter.feature_stuff

import com.mapbox.geojson.Feature

enum class PlaceType {
    WALKWAY, PARKING_LOT, BUILDING, STOP
}

fun Feature.getNameForPlaceType(placeType: PlaceType) = when (placeType) {
    PlaceType.WALKWAY -> throw IllegalStateException("This method shouldn't be called on walkways")
    PlaceType.PARKING_LOT -> getStringProperty(LOT_NAME) ?: "Unnamed parking lot"
    PlaceType.BUILDING -> getStringProperty(BUILDING_NAME) ?: "Unnamed building"
    PlaceType.STOP -> getStringProperty(STOP_NAME) ?: "Unnamed stop"
}

// Feature properties

// Buildings and parking lots
const val LATITUDE = "Latitude"
const val LONGITUDE = "Longitude"

// Buildings
const val BUILDING_NAME = "BldgName"
const val BUILDING_NUMBER = "BldgNum"
const val BUILDING_ADDRESS = "BldgAddr"
const val CITY = "City"

// Parking lots
const val PARKING_ID = "Parking_ID"
const val LOT_NAME = "Lot_Name"

// Stops
const val STOP_NAME = "stop name"
