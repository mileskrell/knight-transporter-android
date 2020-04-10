package edu.rutgers.knighttransporter.for_non_mapbox_queries

import com.mapbox.geojson.Feature

enum class PlaceType {
    BUILDING, PARKING_LOT, WALKWAY
}

fun Feature.getNameForPlaceType(placeType: PlaceType) = when (placeType) {
    PlaceType.BUILDING -> getStringProperty(BUILDING_NAME) ?: "No name"
    PlaceType.PARKING_LOT -> getStringProperty(LOT_NAME) ?: "No name"
    PlaceType.WALKWAY -> throw IllegalStateException("This method shouldn't be called on walkways")
}

// Properties

// Buildings and parking lots
const val LATITUDE = "Latitude"
const val LONGITUDE = "Longitude"

// Buildings
const val BUILDING_NAME = "BldgName"
const val BUILDING_NUMBER = "BldgNum"

// Parking lots
const val PARKING_ID = "Parking_ID"
const val LOT_NAME = "Lot_Name"
