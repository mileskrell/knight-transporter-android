package edu.rutgers.knighttransporter.bottom_sheets

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.mapbox.geojson.Feature
import edu.rutgers.knighttransporter.R
import edu.rutgers.knighttransporter.feature_stuff.ROUTE_NAME
import edu.rutgers.knighttransporter.feature_stuff.VEHICLE_ID
import kotlinx.android.synthetic.main.fragment_place_sheet_vehicle.*

class VehicleFragment : Fragment(R.layout.fragment_place_sheet_vehicle) {
    companion object {
        @JvmStatic
        fun newInstance(feature: String) =
            VehicleFragment()
                .apply {
                    arguments = Bundle().apply {
                        putString(ARG_FEATURE, feature)
                    }
                }
    }

    private lateinit var vehicleFeature: Feature

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            vehicleFeature = Feature.fromJson(it.getString(ARG_FEATURE)!!)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        place_sheet_vehicle_route_name.text = vehicleFeature.getStringProperty(ROUTE_NAME)
        place_sheet_vehicle_id.text =
            "Vehicle ID: ${vehicleFeature.getNumberProperty(VEHICLE_ID).toInt()}"
    }
}
