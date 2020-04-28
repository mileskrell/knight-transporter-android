package edu.rutgers.knighttransporter.feature_stuff

const val xMin = "-75.314648"
const val yMin = "39.844001"
const val xMax = "-73.963602"
const val yMax = "40.837580"
const val commonParams = "f=geojson&geometry=%7B%22xmin%22%3A+$xMin%2C+%22ymin%22%3A+$yMin%2C+%22xmax%22%3A+$xMax%2C+%22ymax%22%3A+$yMax%7D&inSR=4326&outSR=4326&geometryType=esriGeometryEnvelope&spatialRel=esriSpatialRelIntersects&returnDistinctValues=true"

const val arcGISbaseUrl = "https://services1.arcgis.com/ze0XBzU1FXj94DJq/arcgis/rest/services/"

// Also used by Retrofit
const val walkwaysPath = "Rutgers_University_Walkways/FeatureServer/0/query?$commonParams&outFields=" // outFields=Site_ID%2CDistrict or whatever we want to use
const val buildingsPath = "Rutgers_University_Buildings/FeatureServer/0/query?$commonParams&resultType=none&distance=0.0&units=esriSRUnit_Meter&returnGeodetic=false&outFields=$BUILDING_NAME%2C+$BUILDING_NUMBER%2C+$LATITUDE%2C+$LONGITUDE&returnGeometry=true&returnCentroid=false&featureEncoding=esriDefault&multipatchOption=xyFootprint&applyVCSProjection=false&returnIdsOnly=false&returnUniqueIdsOnly=false&returnCountOnly=false&returnExtentOnly=false&returnQueryGeometry=false&cacheHint=false&returnZ=false&returnM=false&returnExceededLimitFeatures=true&sqlFormat=none"
const val parkingLotsPath = "Rutgers_University_Parking/FeatureServer/0/query?$commonParams&outFields=$PARKING_ID%2C%20$LOT_NAME%2C%20$LATITUDE%2C%20$LONGITUDE&where=1%3D1"

const val walkwaysUrl = arcGISbaseUrl + walkwaysPath
const val buildingsUrl = arcGISbaseUrl + buildingsPath
const val parkingLotsUrl = arcGISbaseUrl + parkingLotsPath

const val translocUrl = "http://192.168.1.11:3000/"
