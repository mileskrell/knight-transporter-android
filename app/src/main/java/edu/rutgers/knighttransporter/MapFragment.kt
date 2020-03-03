package edu.rutgers.knighttransporter

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.android.synthetic.main.fragment_map.*
import java.net.URI

class MapFragment : Fragment() {

    private val mapViewModel: MapViewModel by viewModels()

    private lateinit var mapView: MapView
    private lateinit var mapboxMap: MapboxMap

    var parkingLayer: FillLayer? = null
    var buildingLayer: FillLayer? = null

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
        Mapbox.getInstance(context!!, mapboxToken)
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    fun enableLocationComponent(style: Style) {
        if (PermissionsManager.areLocationPermissionsGranted(context!!)) {
            mapboxMap.locationComponent.apply {
                activateLocationComponent(
                    LocationComponentActivationOptions.builder(context!!, style).build()
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
        } else {
            // TODO: Communicate between fragment and activity in a cleaner way?
            (activity as MainActivity).permissionsManager = permissionsManager
            permissionsManager.requestLocationPermissions(activity)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        // We're doing this because we can't use synthetic properties in (at least) onDestroy()
        mapView = view.findViewById(R.id.map_view)

        mapView.onCreate(mapViewModel.mapInstanceState)
        mapView.getMapAsync { mapboxMap ->
            mapboxMap.addOnMapClickListener { latLng ->
                val point = mapboxMap.projection.toScreenLocation(latLng)
                val features =
                    mapboxMap.queryRenderedFeatures(point, "rBuildings-layer", "rParkingLots-layer")
                if (features.isNotEmpty()) {
                    if (features.size > 1) {
                        Toast.makeText(
                            context,
                            "${features.size} items at this point",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    val name = features.first()?.properties()?.get("BldgName")?.asString
                        ?: features.first()?.properties()?.get("Lot_Name")?.asString ?: "No name"
                    AlertDialog.Builder(context!!).setMessage(name).show()
                }
                features.isNotEmpty()
            }
            this.mapboxMap = mapboxMap
            mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
                enableLocationComponent(style)

                style.addSource(GeoJsonSource("rWalkways-source", URI(walkwaysUrl)))
                style.addSource(GeoJsonSource("rBuildings-source", URI(buildingsUrl)))
                style.addSource(GeoJsonSource("rParkingLots-source", URI(parkingLotsUrl)))

                // This is my attempt to add the layers for the polygons at the right position -
                // above the base map, streets, etc., but below any text labels.
                // TODO: Is this the best way to do this? Does it work with all map styles?
                val firstLabelLayerId = style.layers.first { it.id.contains("label") }.id

                FillLayer("rWalkways-layer", "rWalkways-source").apply {
                    setProperties(PropertyFactory.fillColor(0x88964b00.toInt()))
                    style.addLayerBelow(this, firstLabelLayerId)
                }
                parkingLayer = FillLayer("rParkingLots-layer", "rParkingLots-source").apply {
                    setProperties(PropertyFactory.fillColor(0x88888888.toInt()))
                    style.addLayerBelow(this, firstLabelLayerId)
                }
                buildingLayer = FillLayer("rBuildings-layer", "rBuildings-source").apply {
                    setProperties(PropertyFactory.fillColor(Color.BLACK))
                    style.addLayerBelow(this, firstLabelLayerId)
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
                item.setIcon(R.drawable.ic_visibility_black_24dp)
                item.setTitle(R.string.show_buildings_lots)
            } else {
                buildingLayer?.setProperties(visibility(Property.VISIBLE))
                parkingLayer?.setProperties(visibility(Property.VISIBLE))
                item.setIcon(R.drawable.ic_visibility_off_black_24dp)
                item.setTitle(R.string.hide_buildings_lots)
            }
            true
        }
        else -> false
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
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
