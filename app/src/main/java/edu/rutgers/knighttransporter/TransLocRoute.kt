package edu.rutgers.knighttransporter

enum class TransLocRoute {
    ALL_CAMPUSES,
    A,
    B,
    C,
    EE,
    F,
    H,
    LX,
    ;

    fun getVehicleDrawableInt() = when (this) {
        ALL_CAMPUSES -> R.drawable.ic_bus_all_campuses
        A -> R.drawable.ic_bus_a
        B -> R.drawable.ic_bus_b
        C -> R.drawable.ic_bus_c
        EE -> R.drawable.ic_bus_ee
        F -> R.drawable.ic_bus_f
        H -> R.drawable.ic_bus_h
        LX -> R.drawable.ic_bus_lx
    }

    // I'm using these as map icon keys, because I'm guessing they're more stable than the route names
    fun getRouteId() = when (this) {
        ALL_CAMPUSES -> 4012654
        else -> TODO("Go look up the route ID for route ${this.name} and add it here")
    }
}
