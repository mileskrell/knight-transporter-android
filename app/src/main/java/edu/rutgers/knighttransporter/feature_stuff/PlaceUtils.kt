package edu.rutgers.knighttransporter.feature_stuff

import com.mapbox.geojson.Feature

enum class PlaceType {
    WALKWAY, PARKING_LOT, BUILDING, STOP, VEHICLE
}

fun Feature.getNameForPlaceType(placeType: PlaceType) = when (placeType) {
    PlaceType.WALKWAY -> throw IllegalStateException("This method shouldn't be called on walkways")
    PlaceType.PARKING_LOT -> getStringProperty(LOT_NAME) ?: "Unnamed parking lot"
    PlaceType.BUILDING -> getStringProperty(BUILDING_NAME) ?: "Unnamed building"
    PlaceType.STOP -> getStringProperty(STOP_NAME) ?: "Unnamed stop"
    PlaceType.VEHICLE -> (getStringProperty(ROUTE_NAME) ?: "Unnamed route") +
            // TODO: Should I use the call sign instead?
            "\n(vehicle ID " + (getNumberProperty(VEHICLE_ID) ?: "unknown") + ")"
}

// Feature properties

// Buildings and parking lots and stops and vehicles
const val LATITUDE = "Latitude"
const val LONGITUDE = "Longitude"

// Building ArcGIS details and parking lots
const val WEBSITE = "Website"

// Buildings and building ArcGIS details
const val BUILDING_NUMBER = "BldgNum"

// Building ArcGIS details
const val DESCRIPTION = "Description"
const val ALERT_LINKS = "AlertLinks"

// Buildings
const val BUILDING_NAME = "BldgName"
const val BUILDING_ADDRESS = "BldgAddr"
const val CITY = "City"
const val STATE = "State"
const val BUILDING_NUMBER_POP = "Building_Number" // not returned by Repository
const val CAMPUS = "Campus" // added from popular destinations
const val POPULAR_DESTINATION = "popular_destination" // added from popular destinations

// Parking lots
const val PARKING_ID = "Parking_ID"
const val LOT_NAME = "Lot_Name"
const val CONTACT = "Contact"

// Stops
const val STOP_MARKER_DATA_JSON = "stop marker data JSON"
const val STOP_NAME = "stop name"

// Vehicles
const val ROUTE_NAME = "route name"
const val VEHICLE_ID = "vehicle ID"
const val HEADING = "heading"

// Vehicles and routes
const val COLOR = "color"

// Routes
const val NAME = "name"

// Keys for storing GeoJSON in SharedPreferences
const val WALKWAYS_KEY = "walkways"
const val ROUTE_POLYLINES_KEY = "route_polylines"
const val PARKING_LOTS_KEY = "parking_lots"
const val GEO_BUILDINGS_KEY = "geo_buildings"
const val POP_BUILDINGS_KEY = "pop_buildings"
