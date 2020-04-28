package edu.rutgers.knighttransporter.feature_stuff

private const val xMin = "-75.314648"
private const val yMin = "39.844001"
private const val xMax = "-73.963602"
private const val yMax = "40.837580"
private const val commonParams = "f=geojson&geometry=%7B%22xmin%22%3A$xMin%2C%22ymin%22%3A$yMin%2C%22xmax%22%3A$xMax%2C%22ymax%22%3A$yMax%7D"

const val arcGISbaseUrl = "https://services1.arcgis.com/ze0XBzU1FXj94DJq/arcgis/rest/services/"

// Used by Retrofit
const val walkwaysPath = "Rutgers_University_Walkways/FeatureServer/0/query?$commonParams&outFields=" // outFields=Site_ID%2CDistrict or whatever we want to use
const val buildingsPath = "Rutgers_University_Buildings/FeatureServer/0/query?$commonParams&outFields=$BUILDING_NAME%2C$BUILDING_NUMBER%2C$BUILDING_ADDRESS%2C$CITY%2C$LATITUDE%2C$LONGITUDE"
const val parkingLotsPath = "Rutgers_University_Parking/FeatureServer/0/query?$commonParams&outFields=$PARKING_ID%2C$LOT_NAME%2C$LATITUDE%2C$LONGITUDE"

// Used by Mapbox
const val walkwaysUrl = arcGISbaseUrl + walkwaysPath
const val buildingsUrl = arcGISbaseUrl + buildingsPath
const val parkingLotsUrl = arcGISbaseUrl + parkingLotsPath

const val translocUrl = "http://192.168.1.11:3000/"
