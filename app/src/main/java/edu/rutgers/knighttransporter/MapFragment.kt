package edu.rutgers.knighttransporter

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commitNow
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.leinardi.android.speeddial.SpeedDialView
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression.get
import com.mapbox.mapboxsdk.style.layers.*
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils
import com.miguelcatalan.materialsearchview.MaterialSearchView
import edu.rutgers.knighttransporter.bottom_sheets.BuildingFragment
import edu.rutgers.knighttransporter.bottom_sheets.ParkingLotFragment
import edu.rutgers.knighttransporter.bottom_sheets.StopFragment
import edu.rutgers.knighttransporter.bottom_sheets.VehicleFragment
import edu.rutgers.knighttransporter.feature_stuff.*
import edu.rutgers.knighttransporter.for_transloc.StopMarkerData
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.coroutines.launch
import java.net.URI

class MapFragment : Fragment() {

    companion object {
        const val MIN_ZOOM = 7.0
        const val LAT_NORTH = 41.36
        const val LNG_EAST = -73.89
        const val LAT_SOUTH = 38.92
        const val LNG_WEST = -75.56
        const val WALKWAYS_SOURCE = "rWalkways-source"
        const val WALKWAYS_LAYER = "rWalkways-layer"
        const val PARKING_LOTS_SOURCE = "rParkingLots-source"
        const val PARKING_LOTS_LAYER = "rParkingLots-layer"
        const val BUILDINGS_SOURCE = "rBuildings-source"
        const val BUILDINGS_LAYER = "rBuildings-layer"
        const val STOPS_SOURCE = "rStops-source"
        const val STOPS_LAYER = "rStops-layer"
        const val VEHICLES_SOURCE = "rVehicles-source"
        const val VEHICLES_LAYER = "rVehicles-layer"
        const val SELECTED_PLACE_SOURCE = "rSelectedPlace-source"
        const val SELECTED_PLACE_LAYER = "rSelectedPlace-layer"
        const val RUTGERS_BUS_ICON = "rutgers-bus-icon"
        const val RUTGERS_STOP_ICON = "rutgers-stop-icon"
        const val BOTTOM_SHEET_FRAGMENT = "bottom sheet fragment"
    }

    private lateinit var toolbar: Toolbar
    private lateinit var searchView: MaterialSearchView

    private val mapViewModel: MapViewModel by activityViewModels()

    private lateinit var mapView: MapView
    private lateinit var mapboxMap: MapboxMap
    private var style: Style? = null

    private var parkingLayer: FillLayer? = null
    private var buildingLayer: FillLayer? = null

    private var speedDialWasClosedBecauseSearchViewWasOpened = false

    private val permissionsManager = PermissionsManager(object : PermissionsListener {
        override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
            // TODO: The code here runs at the same time that the permission is re-requested.
            //  Meaning that this message won't be what the user is looking at.
            //  I think Mapbox messed up.
            Toast.makeText(
                context,
                "You need to grant that permission!",
                Toast.LENGTH_LONG
            ).show()
        }

        override fun onPermissionResult(granted: Boolean) {
            if (granted) {
                mapboxMap.getStyle { style ->
                    enableLocationComponent(style)
                }
            } else {
                Toast.makeText(
                    context,
                    "Location permission denied; not showing location",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Mapbox.getInstance(requireContext(), mapboxToken)
        toolbar = (requireActivity() as MainActivity).toolbar
        searchView = (requireActivity() as MainActivity).search_view
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    private fun enableLocationComponent(style: Style) {
        if (PermissionsManager.areLocationPermissionsGranted(requireContext())) {
            mapboxMap.locationComponent.apply {
                activateLocationComponent(
                    LocationComponentActivationOptions.builder(requireContext(), style).build()
                )
                isLocationComponentEnabled = true
                cameraMode = CameraMode.TRACKING
                renderMode = RenderMode.COMPASS
            }
            fab_my_location.setOnClickListener {
                if (!::mapboxMap.isInitialized) return@setOnClickListener
                val last = mapboxMap.locationComponent.lastKnownLocation
                if (last != null) {
                    mapboxMap.animateCamera {
                        CameraUpdateFactory.newLatLng(LatLng(last.latitude, last.longitude))
                            .getCameraPosition(it)
                    }
                }
            }
            // We would call show(), but we want this to be instant. Cast to suppress warning.
            (fab_my_location as View).visibility = View.VISIBLE
        } else {
            // TODO: Communicate between fragment and activity in a cleaner way?
            (activity as MainActivity).permissionsManager = permissionsManager
            permissionsManager.requestLocationPermissions(activity)
        }
    }

    /**
     * Clear selected place and select a new one
     */
    private fun setSelectedPlace(placeType: PlaceType, feature: Feature) {
        style?.let { style ->
            style.removeLayer(SELECTED_PLACE_LAYER)
            style.removeSource(SELECTED_PLACE_SOURCE)
            style.addSource(GeoJsonSource(SELECTED_PLACE_SOURCE, feature))
            when (placeType) {
                PlaceType.STOP -> {
                    SymbolLayer(SELECTED_PLACE_LAYER, SELECTED_PLACE_SOURCE).withProperties(
                        PropertyFactory.iconColor(0xFFFF00FF.toInt()),
                        PropertyFactory.iconImage(RUTGERS_STOP_ICON),
                        PropertyFactory.iconAllowOverlap(true)
                    ).run {
                        // We definitely have a stops layer
                        style.addLayerAbove(this, STOPS_LAYER)
                    }
                }
                // TODO: Update selected layer as vehicle moves
                PlaceType.VEHICLE -> {
                    SymbolLayer(SELECTED_PLACE_LAYER, SELECTED_PLACE_SOURCE).withProperties(
                        PropertyFactory.iconColor(0xFFFF00FF.toInt()),
                        PropertyFactory.iconImage(RUTGERS_BUS_ICON),
                        PropertyFactory.iconRotate(get(HEADING)),
                        PropertyFactory.iconSize(1.5f),
                        PropertyFactory.iconAllowOverlap(true)
                    ).run {
                        // We definitely have a vehicles layer
                        style.addLayerAbove(this, VEHICLES_LAYER)
                    }
                }
                else -> {
                    LineLayer(SELECTED_PLACE_LAYER, SELECTED_PLACE_SOURCE).withProperties(
                        PropertyFactory.lineColor(0xFFFF00FF.toInt()),
                        PropertyFactory.lineWidth(5f)
                    ).run {
                        // Add layer as high as possible
                        style.addLayerAbove(
                            this, when {
                                style.getLayer(VEHICLES_LAYER) != null -> VEHICLES_LAYER
                                style.getLayer(STOPS_LAYER) != null -> STOPS_LAYER
                                style.getLayer(BUILDINGS_LAYER) != null -> BUILDINGS_LAYER
                                else -> mapViewModel.firstLabelLayerId
                            }
                        )
                    }
                }
            }
            childFragmentManager.commitNow {
                replace(
                    R.id.map_bottom_sheet,
                    when (placeType) {
                        PlaceType.WALKWAY -> throw IllegalStateException("Bottom sheet should not be created for walkways")
                        PlaceType.PARKING_LOT -> ParkingLotFragment.newInstance(
                            feature.toJson()
                        )
                        PlaceType.BUILDING -> BuildingFragment.newInstance(feature.toJson())
                        PlaceType.STOP -> StopFragment.newInstance(feature.toJson())
                        PlaceType.VEHICLE -> VehicleFragment.newInstance(feature.toJson())
                    },
                    BOTTOM_SHEET_FRAGMENT
                )
            }

            BottomSheetBehavior.from(map_bottom_sheet).state = BottomSheetBehavior.STATE_HALF_EXPANDED
        }
    }

    /**
     * Clear selected place (if any)
     * @return whether a place was deselected
     */
    fun clearSelectedPlace(): Boolean {
        return style?.let { style ->
            val removedLayer = style.removeLayer(SELECTED_PLACE_LAYER)
            style.removeSource(SELECTED_PLACE_SOURCE)
            BottomSheetBehavior.from(map_bottom_sheet).state = BottomSheetBehavior.STATE_HIDDEN
            childFragmentManager.findFragmentByTag(BOTTOM_SHEET_FRAGMENT)?.let {
                childFragmentManager.commitNow { remove(it) }
            }
            removedLayer
        } ?: false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        BottomSheetBehavior.from(map_bottom_sheet).state = BottomSheetBehavior.STATE_HIDDEN
        // Don't let gestures pass through bottom sheet to MapView
        map_bottom_sheet.setOnTouchListener { _, _ -> true }

        // We're doing this because we can't use synthetic properties in (at least) onDestroy()
        mapView = view.findViewById(R.id.map_view)

        map_view.onCreate(mapViewModel.mapInstanceState)
        map_view.getMapAsync { mapboxMap ->
            this.mapboxMap = mapboxMap
            mapboxMap.addOnMoveListener(object : MapboxMap.OnMoveListener {
                override fun onMoveBegin(detector: MoveGestureDetector) {
                    routes_speed_dial.close()
                }

                override fun onMove(detector: MoveGestureDetector) {}
                override fun onMoveEnd(detector: MoveGestureDetector) {}
            })
            mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
                this.style = style
                // This is my attempt to add the layers for the polygons at the right position -
                // above the base map, streets, etc., but below any text labels.
                // TODO: Is this the best way to do this? Does it work with all map styles?
                mapViewModel.firstLabelLayerId = style.layers.first { it.id.contains("label") }.id

                // add bus marker
                style.addImage(
                    RUTGERS_BUS_ICON,
                    BitmapUtils.getBitmapFromDrawable(
                        resources.getDrawable(R.drawable.ic_navigation_black_24dp, null)
                    )!!,
                    true // This lets us change its color
                )
                style.addImage(
                    RUTGERS_STOP_ICON,
                    BitmapUtils.getBitmapFromDrawable(
                        resources.getDrawable(R.drawable.circle, null)
                    )!!,
                    true // This lets us change its color
                )
                mapboxMap.addOnMapClickListener { latLng ->
                    routes_speed_dial.close()
                    searchView.closeSearch()
                    val point = mapboxMap.projection.toScreenLocation(latLng)

                    val tappedVehicles = mapboxMap.queryRenderedFeatures(point, VEHICLES_LAYER)
                    if (tappedVehicles.isNotEmpty()) {
                        setSelectedPlace(PlaceType.VEHICLE, tappedVehicles.first())
                        return@addOnMapClickListener true
                    }

                    val tappedStops = mapboxMap.queryRenderedFeatures(point, STOPS_LAYER)
                    if (tappedStops.isNotEmpty()) {
                        setSelectedPlace(PlaceType.STOP, tappedStops.first())
                        return@addOnMapClickListener true
                    }

                    val tappedBuildings = mapboxMap.queryRenderedFeatures(point, BUILDINGS_LAYER)
                    if (tappedBuildings.isNotEmpty()) {
                        setSelectedPlace(PlaceType.BUILDING, tappedBuildings.first())
                        return@addOnMapClickListener true
                    }

                    val tappedParkingLots =
                        mapboxMap.queryRenderedFeatures(point, PARKING_LOTS_LAYER)
                    if (tappedParkingLots.isNotEmpty()) {
                        setSelectedPlace(PlaceType.PARKING_LOT, tappedParkingLots.first())
                        return@addOnMapClickListener true
                    }

                    clearSelectedPlace()
                    false
                }

                mapboxMap.setLatLngBoundsForCameraTarget(
                    LatLngBounds.from(LAT_NORTH, LNG_EAST, LAT_SOUTH, LNG_WEST)
                )
                mapboxMap.setMinZoomPreference(MIN_ZOOM)
                enableLocationComponent(style)
                mapboxMap.uiSettings.run {
                    isRotateGesturesEnabled = false
                    isTiltGesturesEnabled = false
                }

                // TODO: Try to animate to the new vehicle positions.
                mapViewModel.routes.observe(viewLifecycleOwner, Observer { routes ->
                    val newStopIdToMarkerDataMap = mutableMapOf<Int, StopMarkerData>()
                    for (route in routes) {
                        for (stop in route.stops) {
                            if (newStopIdToMarkerDataMap[stop.stopId] == null) {
                                newStopIdToMarkerDataMap[stop.stopId] = StopMarkerData(stop)
                            }
                            newStopIdToMarkerDataMap[stop.stopId]!!.run {
                                associatedRoutes.add(route)
                                arrivalEstimates.addAll(
                                    route.vehicles.flatMap { it.arrivalEstimates }
                                        .filter { it.stopId == stop.stopId }
                                )
                            }
                        }
                    }

                    // Just for performance; we don't need to remove and re-add this layer every few seconds
                    val shouldAddLayer =
                        mapViewModel.stopIdToMarkerDataMap.value!!.isEmpty() && newStopIdToMarkerDataMap.isNotEmpty()

                    // Any open StopFragment will observe this data so it can update while open
                    mapViewModel.stopIdToMarkerDataMap.value = newStopIdToMarkerDataMap

                    if (shouldAddLayer) {
                        val busStopFeatures =
                            newStopIdToMarkerDataMap.values.map { stopMarkerData ->
                                Feature.fromGeometry(
                                    Point.fromLngLat(
                                        stopMarkerData.stop.location.lng,
                                        stopMarkerData.stop.location.lat
                                    )
                                ).apply {
                                    addStringProperty(
                                        STOP_MARKER_DATA_JSON,
                                        Gson().toJson(stopMarkerData, StopMarkerData::class.java)
                                    )
                                }
                            }
                        style.removeLayer(STOPS_LAYER)
                        style.removeSource(STOPS_SOURCE)
                        style.addSource(
                            GeoJsonSource(
                                STOPS_SOURCE,
                                FeatureCollection.fromFeatures(busStopFeatures)
                            )
                        )
                        SymbolLayer(STOPS_LAYER, STOPS_SOURCE).withProperties(
                            PropertyFactory.iconColor("#ffa500"),
                            PropertyFactory.iconImage(RUTGERS_STOP_ICON),
                            PropertyFactory.iconAllowOverlap(true)
                        ).run {
                            style.addLayerAbove(
                                this, when {
                                    style.getLayer(BUILDINGS_LAYER) != null -> BUILDINGS_LAYER
                                    else -> mapViewModel.firstLabelLayerId
                                }
                            )
                        }
                    }

                    style.removeLayer(VEHICLES_LAYER)
                    style.removeSource(VEHICLES_SOURCE)
                    val vehicleFeatures = mutableListOf<Feature>()
                    routes.forEach { route ->
                        route.vehicles.forEach { vehicle ->
                            vehicleFeatures.add(
                                Feature.fromGeometry(
                                    Point.fromLngLat(vehicle.location.lng, vehicle.location.lat)
                                ).apply {
                                    addStringProperty(ROUTE_NAME, route.longName)
                                    addNumberProperty(VEHICLE_ID, vehicle.vehicleId)
                                    addNumberProperty(HEADING, vehicle.heading)
                                    addStringProperty(COLOR, "#${route.color}")
                                }
                            )
                        }
                    }

                    style.addSource(
                        GeoJsonSource(
                            VEHICLES_SOURCE,
                            FeatureCollection.fromFeatures(vehicleFeatures)
                        )
                    )
                    SymbolLayer(VEHICLES_LAYER, VEHICLES_SOURCE).withProperties(
                        PropertyFactory.iconImage(RUTGERS_BUS_ICON),
                        PropertyFactory.iconRotate(get(HEADING)),
                        PropertyFactory.iconSize(1.5f),
                        PropertyFactory.iconAllowOverlap(true)
                    ).run {
                        style.addLayerAbove(
                            this, when {
                                style.getLayer(STOPS_LAYER) != null -> STOPS_LAYER
                                style.getLayer(BUILDINGS_LAYER) != null -> BUILDINGS_LAYER
                                else -> mapViewModel.firstLabelLayerId
                            }
                        )
                    }
                })

                mapViewModel.viewModelScope.launch {
                    // TODO: Make sure these fail gracefully
//                    mapViewModel.getWalkways() // TODO: Do I have any use for the walkway data here?
                    val parkingLots = mapViewModel.getParkingLots()
                    val buildings = mapViewModel.getBuildings()

                    // Set up search suggestions

                    val buildingItems = buildings.features()
                        ?.map {
                            RutgersPlacesSearchAdapter.AdapterPlaceItem(
                                resources.getDrawable(R.drawable.building, null),
                                PlaceType.BUILDING,
                                it
                            )
                        } ?: emptyList()

                    val parkingLotItems = parkingLots.features()
                        ?.map {
                            RutgersPlacesSearchAdapter.AdapterPlaceItem(
                                resources.getDrawable(R.drawable.ic_local_parking_black_24dp, null),
                                PlaceType.PARKING_LOT,
                                it
                            )
                        } ?: emptyList()
                    val adapter =
                        RutgersPlacesSearchAdapter(
                            requireContext(),
                            buildingItems.plus(parkingLotItems).toTypedArray()
                        )
                    searchView.setAdapter(adapter)

                    searchView.setOnItemClickListener { _, _, position, _ ->
                        searchView.closeSearch()
                        mapboxMap.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                adapter.getItem(position).latLng, 16.0
                            )
                        )
                        val placeItem = adapter.getItem(position)
                        setSelectedPlace(placeItem.placeType, placeItem.feature)
                    }

                    activity?.runOnUiThread {
                        Toast.makeText(
                            context,
                            "Fetched data needed for searching",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                style.addSource(GeoJsonSource(WALKWAYS_SOURCE, URI(walkwaysUrl)))
                style.addSource(GeoJsonSource(BUILDINGS_SOURCE, URI(buildingsUrl)))
                style.addSource(GeoJsonSource(PARKING_LOTS_SOURCE, URI(parkingLotsUrl)))

                FillLayer(WALKWAYS_LAYER, WALKWAYS_SOURCE)
                    .withProperties(PropertyFactory.fillColor(0x88964b00.toInt())).run {
                        style.addLayerBelow(this, mapViewModel.firstLabelLayerId)
                    }
                parkingLayer = FillLayer(PARKING_LOTS_LAYER, PARKING_LOTS_SOURCE)
                    .withProperties(PropertyFactory.fillColor(0x88888888.toInt())).apply {
                        style.addLayerBelow(this, mapViewModel.firstLabelLayerId)
                    }
                buildingLayer = FillLayer(BUILDINGS_LAYER, BUILDINGS_SOURCE)
                    .withProperties(PropertyFactory.fillColor(Color.BLACK)).apply {
                        style.addLayerBelow(this, mapViewModel.firstLabelLayerId)
                    }

                // Remove Mapbox Streets building stuff
                style.removeLayer("building")
                style.removeLayer("building-outline")
                style.removeLayer("building-number-label")
                style.removeLayer("poi-label")

                // Remove Mapbox Streets pedestrian paths (we're showing our own walkway data)
                style.removeLayer("road-path-bg")
                style.removeLayer("road-path")

                // Remove Mapbox Streets stairs
                style.removeLayer("road-steps")
                style.removeLayer("road-steps-bg")

                // Remove Mapbox Streets bus stop icons
                style.removeLayer("transit-label")
            }
        }
        routes_speed_dial.inflate(R.menu.routes_speed_dial_menu)
        routes_speed_dial.setOnChangeListener(object : SpeedDialView.OnChangeListener {
            override fun onMainActionSelected() = false

            override fun onToggleChanged(isOpen: Boolean) {
                // Normally, we close the search when the speed dial is tapped. But we also close
                // the speed dial when we open the search, and if that's why this method was called,
                // then we shouldn't close the search.
                if (isOpen || !speedDialWasClosedBecauseSearchViewWasOpened) {
                    searchView.closeSearch()
                }

                speedDialWasClosedBecauseSearchViewWasOpened = false
            }
        })
        routes_speed_dial.setOnActionSelectedListener {
            Toast.makeText(context, "You tapped a route", Toast.LENGTH_SHORT).show()
            false
        }

        searchView.setOnSearchViewListener(object : MaterialSearchView.SearchViewListener {
            override fun onSearchViewClosed() {}

            override fun onSearchViewShown() {
                speedDialWasClosedBecauseSearchViewWasOpened = true
                routes_speed_dial.close()
            }
        })

        // TODO: When you tap the search box, any old suggestions reappear. Make this not happen.
        searchView.setOnQueryTextListener(object : MaterialSearchView.OnQueryTextListener {
            // Don't close when submit button is tapped
            override fun onQueryTextSubmit(query: String) = true

            // It doesn't seem like the value for this one matters
            override fun onQueryTextChange(newText: String) = false
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // If we don't save this, the map will be reset if we leave and come back.
        // Since onSaveInstanceState() doesn't get called when we go to a different page,
        // we need to save it here instead.
        mapViewModel.mapInstanceState.clear()
        mapView.onSaveInstanceState(mapViewModel.mapInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_overflow_menu, menu)
        searchView.setMenuItem(menu.findItem(R.id.menu_item_search))
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.menu_item_settings -> {
            findNavController().navigate(R.id.action_map_fragment_to_settings_fragment)
            true
        }
        R.id.menu_item_toggle -> {
            if (buildingLayer?.visibility?.value == Property.VISIBLE) {
                buildingLayer?.setProperties(visibility(Property.NONE))
                parkingLayer?.setProperties(visibility(Property.NONE))
                item.setTitle(R.string.show_buildings_lots)
            } else {
                buildingLayer?.setProperties(visibility(Property.VISIBLE))
                parkingLayer?.setProperties(visibility(Property.VISIBLE))
                item.setTitle(R.string.hide_buildings_lots)
            }
            true
        }
        else -> false
    }

    override fun onPause() {
        super.onPause()
        map_view.onPause()
    }

    override fun onResume() {
        super.onResume()
        map_view.onResume()
    }

    override fun onStop() {
        super.onStop()
        // Don't resize or move anything when the keyboard appears.
        // Most importantly, this prevents the soft keyboard from causing the map to resize.
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED)
        map_view.onStop()
    }

    override fun onStart() {
        super.onStart()
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        map_view.onStart()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Idk if this is necessary, since I'm already saving the state in onDestroyView()
        mapView.onSaveInstanceState(outState)
    }
}
