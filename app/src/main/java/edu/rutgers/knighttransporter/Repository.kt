package edu.rutgers.knighttransporter

import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mapbox.geojson.FeatureCollection
import edu.rutgers.knighttransporter.bottom_sheets.BuildingArcGISDetailsFeatureCollection
import edu.rutgers.knighttransporter.bottom_sheets.BuildingArcGISDetailsFeatureCollection.Feature.BuildingArcGISDetails
import edu.rutgers.knighttransporter.bottom_sheets.RutgersCloudStorageService
import edu.rutgers.knighttransporter.feature_stuff.*
import edu.rutgers.knighttransporter.for_transloc.Route
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONArray
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class Repository(val onRoutesUpdated: (routes: List<Route>) -> Unit) {

    private var walkways: FeatureCollection? = null
    private var routePolylines: FeatureCollection? = null
    private var parkingLots: FeatureCollection? = null
    private var buildings: FeatureCollection? = null
    private var buildingArcGISDetailsList: List<BuildingArcGISDetails>? = null

    private fun logEvent(eventName: String, args: Array<out Any?>?) {
        var message = "Socket.IO: $eventName"
        if (args?.size ?: 0 > 0) {
            message += " with ${args!!.size} args:"
        }

        Log.d(TAG, message)
        args?.forEachIndexed { index, value ->
            Log.d(TAG, "           ${index + 1}. " + (value?.toString() ?: "(null)"))
        }
    }

    private val translocSocket = IO.socket(busServerUrl, IO.Options().apply {
        timeout = -1 // Maybe this makes it work more often? Probably not, though
    }).connect().apply {
        on(Socket.EVENT_CONNECT) { args ->
            logEvent("Connect", args)
        }.on(Socket.EVENT_CONNECTING) { args ->
            logEvent("Connecting", args)
        }.on(Socket.EVENT_DISCONNECT) { args ->
            logEvent("Disconnected", args)
        }.on(Socket.EVENT_ERROR) { args ->
            logEvent("Error", args)
        }.on(Socket.EVENT_MESSAGE) { args ->
            logEvent("Message", args)
        }.on(Socket.EVENT_CONNECT_ERROR) { args ->
            logEvent("Connect error", args)
        }.on(Socket.EVENT_CONNECT_TIMEOUT) { args ->
            logEvent("Connect timeout", args)
        }.on(Socket.EVENT_RECONNECT) { args ->
            logEvent("Reconnect", args)
        }.on(Socket.EVENT_RECONNECT_ERROR) { args ->
            logEvent("Reconnect error", args)
        }.on(Socket.EVENT_RECONNECT_FAILED) { args ->
            logEvent("Reconnect failed", args)
        }.on(Socket.EVENT_RECONNECT_ATTEMPT) { args ->
            logEvent("Reconnect attempt", args)
        }.on(Socket.EVENT_RECONNECTING) { args ->
            logEvent("Reconnecting", args)
        }.on(Socket.EVENT_PING) { args ->
            logEvent("Ping", args)
        }.on(Socket.EVENT_PONG) { args ->
            logEvent("Pong", args)
        }.on("data") {
            Log.d(TAG, "Socket.IO: Received data")
            val routes = Gson().fromJson<List<Route>>(
                (it[0] as JSONArray).toString(),
                object : TypeToken<List<Route>>() {}.type
            )
            onRoutesUpdated(routes)
        }
    }

    val arcGISService = Retrofit.Builder()
        .baseUrl(arcGISbaseUrl)
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()
        .create(ArcGISService::class.java)

    val rutgersCloudStorageService = Retrofit.Builder()
        .baseUrl(RutgersCloudStorageService.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(RutgersCloudStorageService::class.java)

    private val routePolylineService = Retrofit.Builder()
        .baseUrl(routesBaseUrl)
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()
        .create(RoutePolylineService::class.java)

    suspend fun getWalkways(sharedPrefs: SharedPreferences): FeatureCollection {
        walkways?.let { return it }

        val walkwaysJson = sharedPrefs.getString(WALKWAYS_KEY, null)
            ?: arcGISService.getWalkways().also {
                sharedPrefs.edit().putString(WALKWAYS_KEY, it).apply()
            }

        return FeatureCollection.fromJson(walkwaysJson)
            .also { walkways = it }
    }

    suspend fun getRoutePolylines(sharedPrefs: SharedPreferences): FeatureCollection {
        routePolylines?.let { return it }

        val routePolylinesJson = sharedPrefs.getString(ROUTE_POLYLINES_KEY, null)
            ?: routePolylineService.getRoutePolylines().also {
                sharedPrefs.edit().putString(ROUTE_POLYLINES_KEY, it).apply()
            }

        return FeatureCollection.fromJson(routePolylinesJson)
            .also { routePolylines = it }
    }

    suspend fun getParkingLots(sharedPrefs: SharedPreferences): FeatureCollection {
        parkingLots?.let { return it }

        val parkingLotsJson = sharedPrefs.getString(PARKING_LOTS_KEY, null)
            ?: arcGISService.getParkingLots().also {
                sharedPrefs.edit().putString(PARKING_LOTS_KEY, it).apply()
            }

        return FeatureCollection.fromJson(parkingLotsJson)
            .also { walkways = it }
    }

    suspend fun getBuildings(sharedPrefs: SharedPreferences): FeatureCollection {
        buildings?.let { return it }

        val geoBuildingsJson = sharedPrefs.getString(GEO_BUILDINGS_KEY, null)
            ?: arcGISService.getBuildings().also {
                sharedPrefs.edit().putString(GEO_BUILDINGS_KEY, it).apply()
            }

        val popBuildingsJson = sharedPrefs.getString(POP_BUILDINGS_KEY, null)
            ?: arcGISService.getPopularDestinations().also {
                sharedPrefs.edit().putString(POP_BUILDINGS_KEY, it).apply()
            }

        val geoBuildings = FeatureCollection.fromJson(geoBuildingsJson)
        val popBuildings = FeatureCollection.fromJson(popBuildingsJson)

        popBuildings.features()?.forEach { popBuilding ->
            // For each "popular destination" building, find the corresponding building
            // that contains the geometry and other properties
            geoBuildings.features()?.firstOrNull { geoBuilding ->
                geoBuilding.getNumberProperty(BUILDING_NUMBER).toInt() ==
                        popBuilding.getNumberProperty(BUILDING_NUMBER_POP).toInt()
            }?.run {
                // Add the properties from the "popular destination" to the corresponding building
                addStringProperty(CAMPUS, popBuilding.getStringProperty(CAMPUS))
                addStringProperty(
                    POPULAR_DESTINATION, popBuilding.getStringProperty(POPULAR_DESTINATION)
                )
            }
        }

        return geoBuildings.also { buildings = it }
    }

    // TODO: Fetch this beforehand, because it *does* have information that's useful before
    //  having selected a building: the building categories (which we're not fetching yet).
    //  It's probably best to fetch this along with the other building data in getBuildings().
    suspend fun getBuildingArcGISDetails(buildingNumber: Int): BuildingArcGISDetails? {
        if (buildingArcGISDetailsList == null) {
            buildingArcGISDetailsList = Gson().fromJson(
                arcGISService.getBuildingsArcGISDetails(),
                BuildingArcGISDetailsFeatureCollection::class.java
            ).features.map { it.properties }
        }
        // There should really always be a building with the provided number,
        // but we're using firstOrNull() just to be safe
        return buildingArcGISDetailsList!!.firstOrNull { it.bldgNum == buildingNumber }
    }

    suspend fun getBuildingCloudStorageDetails(buildingNumber: Int) =
        rutgersCloudStorageService.getBuildingCloudStorageDetails(buildingNumber)

    companion object {
        const val TAG = "Repository"
    }
}
