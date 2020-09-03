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
    companion object {
        const val TAG = "Repository"
    }

    private var walkways: FeatureCollection? = null
    private var parkingLots: FeatureCollection? = null
    private var buildings: FeatureCollection? = null
    private var buildingArcGISDetailsList: List<BuildingArcGISDetails>? = null

    private val translocSocket = IO.socket(busServerUrl).connect().apply {
        on(Socket.EVENT_CONNECT) {
            Log.d(TAG, "Connected")
        }.on(Socket.EVENT_CONNECTING) {
            Log.d(TAG, "Connecting")
        }.on(Socket.EVENT_CONNECT_TIMEOUT) {
            Log.d(TAG, "Connect timeout")
        }.on(Socket.EVENT_CONNECT_ERROR) { args ->
            Log.d(TAG, "Connect error (with the following ${args.size} args)")
            args.forEach {
                Log.d(TAG, it?.toString() ?: "(null)")
            }
        }.on("data") {
            Log.d(TAG, "Received data")
            val routes = Gson().fromJson<List<Route>>(
                (it[0] as JSONArray).toString(),
                object : TypeToken<List<Route>>() {}.type
            )
            onRoutesUpdated(routes)
        }.on(Socket.EVENT_DISCONNECT) {
            Log.d(TAG, "Disconnected")
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

    suspend fun getWalkways(sharedPrefs: SharedPreferences): FeatureCollection {
        walkways?.let { return it }

        val walkwaysJson = sharedPrefs.getString(WALKWAYS_KEY, null)
            ?: arcGISService.getWalkways().also {
                sharedPrefs.edit().putString(WALKWAYS_KEY, it).apply()
            }

        return FeatureCollection.fromJson(walkwaysJson)
            .also { walkways = it }
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
}
