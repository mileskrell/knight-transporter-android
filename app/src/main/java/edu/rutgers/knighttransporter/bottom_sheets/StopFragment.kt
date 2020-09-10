package edu.rutgers.knighttransporter.bottom_sheets

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.mapbox.geojson.Feature
import edu.rutgers.knighttransporter.MapViewModel
import edu.rutgers.knighttransporter.R
import edu.rutgers.knighttransporter.feature_stuff.STOP_MARKER_DATA_JSON
import edu.rutgers.knighttransporter.for_transloc.StopMarkerData
import kotlinx.android.synthetic.main.fragment_place_sheet_stop.*

class StopFragment : Fragment(R.layout.fragment_place_sheet_stop) {
    companion object {
        const val TAG = "StopFragment"

        @JvmStatic
        fun newInstance(feature: String) =
            StopFragment()
                .apply {
                    arguments = Bundle().apply {
                        putString(ARG_FEATURE, feature)
                    }
                }
    }

    private lateinit var initialFeature: Feature
    private lateinit var initialStopMarkerData: StopMarkerData
    private val mapViewModel: MapViewModel by activityViewModels()

    private lateinit var stopMarkersObserver: Observer<MutableMap<Int, StopMarkerData>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            initialFeature = Feature.fromJson(it.getString(ARG_FEATURE)!!)
            initialStopMarkerData = Gson().fromJson(
                initialFeature.getStringProperty(STOP_MARKER_DATA_JSON),
                StopMarkerData::class.java
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        place_sheet_stop_name.text = initialStopMarkerData.stop.name

        val routesAdapter = RoutesAdapter(initialStopMarkerData)

        place_sheet_stop_recycler_view.run {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = routesAdapter
        }

        stopMarkersObserver = Observer { stopIdToMarkerDataMap ->
            routesAdapter.updateStopMarkerData(stopIdToMarkerDataMap[initialStopMarkerData.stop.stopId]!!)
        }
    }

    override fun onResume() {
        super.onResume()
        mapViewModel.stopIdToMarkerDataMap.observe(viewLifecycleOwner, stopMarkersObserver)
        Log.d(TAG, "Added observer in bottom sheet for stop ${initialStopMarkerData.stop.name}")
    }

    override fun onPause() {
        super.onPause()
        mapViewModel.stopIdToMarkerDataMap.removeObserver(stopMarkersObserver)
        Log.d(TAG, "Removed observer in bottom sheet for stop ${initialStopMarkerData.stop.name}")
    }
}
