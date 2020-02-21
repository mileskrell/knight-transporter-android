package edu.rutgers.knighttransporter

import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import java.net.URI

class MapFragment : Fragment() {

    private val mapViewModel: MapViewModel by viewModels()

    private lateinit var mapView: MapView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Mapbox.getInstance(context!!, mapboxToken)
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        // We're doing this because we can't use synthetic properties in (at least) onDestroy()
        mapView = view.findViewById(R.id.map_view)

        mapView.onCreate(mapViewModel.mapInstanceState)
        mapView.getMapAsync { mapboxMap ->
            mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
                style.addSource(GeoJsonSource("rBuildings-source", URI(buildingsUrl)))
                style.addSource(GeoJsonSource("rParkingLots-source", URI(parkingLotsUrl)))

                FillLayer("rBuildings-layer", "rBuildings-source").run {
                    setProperties(PropertyFactory.fillColor(Color.GRAY))
                    style.addLayer(this)
                }
                FillLayer("rParkingLots-layer", "rParkingLots-source").run {
                    setProperties(PropertyFactory.fillColor(Color.BLACK))
                    style.addLayer(this)
                }
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
