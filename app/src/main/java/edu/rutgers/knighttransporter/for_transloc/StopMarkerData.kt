package edu.rutgers.knighttransporter.for_transloc

class StopMarkerData(val stop: Route.Stop) {
    // All routes associated with this stop
    val associatedRoutes = mutableListOf<Route>()

    // Arrival estimates for this stop
    val arrivalEstimates = mutableListOf<Route.Vehicle.ArrivalEstimate>()
}
