package edu.rutgers.knighttransporter.bottom_sheets

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.mapbox.geojson.Feature
import edu.rutgers.knighttransporter.R
import edu.rutgers.knighttransporter.for_non_mapbox_queries.BUILDING_NUMBER
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

    private lateinit var building: Feature

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            building = Feature.fromJson(it.getString(ARG_FEATURE)!!)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        place_sheet_building_name.text = building.getNameForPlaceType(PlaceType.BUILDING)

        val buildingNumber = building.getNumberProperty(BUILDING_NUMBER)

        // TODO: Get placeholder to work
//        val progressDrawable = CircularProgressDrawable(requireContext()).apply {
//            strokeWidth = 5f
//            centerRadius = 30f
//            start()
//        }

        Glide.with(this)
            .load("https://storage.googleapis.com/rutgers-campus-map-building-images-prod/$buildingNumber/00.jpg")
//            .placeholder(progressDrawable)
            .into(place_sheet_building_image)

    }
}
