package edu.rutgers.knighttransporter.bottom_sheets

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.mapbox.geojson.Feature
import edu.rutgers.knighttransporter.MapViewModel
import edu.rutgers.knighttransporter.databinding.FragmentPlaceSheetStopBinding
import edu.rutgers.knighttransporter.feature_stuff.STOP_MARKER_DATA_JSON
import edu.rutgers.knighttransporter.for_transloc.StopMarkerData

class StopFragment : Fragment() {
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPlaceSheetStopBinding.inflate(inflater, container, false)

        binding.placeSheetStopName.text = initialStopMarkerData.stop.name

        val routesAdapter = RoutesAdapter(initialStopMarkerData)

        binding.placeSheetStopRecyclerView.run {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = routesAdapter
        }

        stopMarkersObserver = Observer { stopIdToMarkerDataMap ->
            routesAdapter.updateStopMarkerData(stopIdToMarkerDataMap[initialStopMarkerData.stop.stopId]!!)
        }

        return binding.root
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
