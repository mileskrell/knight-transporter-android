package edu.rutgers.knighttransporter.for_non_mapbox_queries

import com.mapbox.geojson.FeatureCollection
import edu.rutgers.knighttransporter.baseUrl
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

class Repository {
    var walkways: FeatureCollection? = null
    var parkingLots: FeatureCollection? = null
    var buildings: FeatureCollection? = null

    val service = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()
        .create(GeoJSONService::class.java)

    suspend fun getWalkways() = walkways ?: service.getWalkways().let {
        return FeatureCollection.fromJson(it)
    }

    suspend fun getParkingLots() = parkingLots ?: service.getParkingLots().let {
        return FeatureCollection.fromJson(it)
    }

    suspend fun getBuildings() = buildings ?: service.getBuildings().let {
        return FeatureCollection.fromJson(it)
    }
}
