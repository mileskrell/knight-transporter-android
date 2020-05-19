package edu.rutgers.knighttransporter

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mapbox.geojson.FeatureCollection
import edu.rutgers.knighttransporter.bottom_sheets.RutgersCloudStorageService
import edu.rutgers.knighttransporter.feature_stuff.ArcGISService
import edu.rutgers.knighttransporter.feature_stuff.arcGISbaseUrl
import edu.rutgers.knighttransporter.feature_stuff.translocUrl
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

    private val translocSocket = IO.socket(translocUrl).connect().apply {
        on(Socket.EVENT_CONNECT) {
            Log.d(TAG, "Connected")
        }.on(Socket.EVENT_CONNECTING) {
            Log.d(TAG, "Connecting")
        }.on(Socket.EVENT_CONNECT_TIMEOUT) {
            Log.d(TAG, "Connect timeout")
        }.on(Socket.EVENT_CONNECT_ERROR) {
            Log.d(TAG, "Connect error")
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

    suspend fun getWalkways() = walkways ?: arcGISService.getWalkways().let {
        return FeatureCollection.fromJson(it)
    }

    suspend fun getParkingLots() = parkingLots ?: arcGISService.getParkingLots().let {
        return FeatureCollection.fromJson(it)
    }

    suspend fun getBuildings() = buildings ?: arcGISService.getBuildings().let {
        return FeatureCollection.fromJson(it)
    }

    suspend fun getBuildingCloudStorageDetails(buildingCode: Int) = rutgersCloudStorageService.getBuildingCloudStorageDetails(buildingCode)
}
