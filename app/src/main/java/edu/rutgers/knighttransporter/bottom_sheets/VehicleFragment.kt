package edu.rutgers.knighttransporter.bottom_sheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mapbox.geojson.Feature
import edu.rutgers.knighttransporter.databinding.FragmentPlaceSheetVehicleBinding
import edu.rutgers.knighttransporter.feature_stuff.ROUTE_NAME
import edu.rutgers.knighttransporter.feature_stuff.VEHICLE_ID

class VehicleFragment : Fragment() {
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPlaceSheetVehicleBinding.inflate(inflater, container, false)

        binding.placeSheetVehicleRouteName.text = vehicleFeature.getStringProperty(ROUTE_NAME)
        binding.placeSheetVehicleId.text =
            "Vehicle ID: ${vehicleFeature.getNumberProperty(VEHICLE_ID).toInt()}"

        return binding.root
    }
}
