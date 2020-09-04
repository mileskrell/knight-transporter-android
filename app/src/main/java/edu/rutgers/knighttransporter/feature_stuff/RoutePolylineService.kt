package edu.rutgers.knighttransporter.feature_stuff

import retrofit2.http.GET

/**
 * This Retrofit interface will be changed (or maybe removed) once we're getting the routes from somewhere else
 */
interface RoutePolylineService {
    @GET("routes.geojson")
    suspend fun getRoutePolylines(): String
}
