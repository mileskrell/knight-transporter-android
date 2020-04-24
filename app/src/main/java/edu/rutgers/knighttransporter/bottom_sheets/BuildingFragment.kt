package edu.rutgers.knighttransporter.bottom_sheets

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.mapbox.geojson.Feature
import edu.rutgers.knighttransporter.R
import edu.rutgers.knighttransporter.for_non_mapbox_queries.PlaceType
import edu.rutgers.knighttransporter.for_non_mapbox_queries.getNameForPlaceType
import kotlinx.android.synthetic.main.fragment_place_sheet_building.*

class BuildingFragment : Fragment(R.layout.fragment_place_sheet_building) {
    companion object {
        @JvmStatic
        fun newInstance(feature: String) =
            BuildingFragment()
                .apply {
                    arguments = Bundle().apply {
                        putString(ARG_FEATURE, feature)
                    }
                }
    }

    private lateinit var feature: Feature

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            feature = Feature.fromJson(it.getString(ARG_FEATURE)!!)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        place_sheet_building_name.text = feature.getNameForPlaceType(PlaceType.BUILDING)
    }
}