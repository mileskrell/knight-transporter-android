package edu.rutgers.knighttransporter.bottom_sheets

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.mapbox.geojson.Feature
import edu.rutgers.knighttransporter.R
import edu.rutgers.knighttransporter.feature_stuff.BUILDING_ADDRESS
import edu.rutgers.knighttransporter.feature_stuff.BUILDING_NAME
import edu.rutgers.knighttransporter.feature_stuff.BUILDING_NUMBER
import edu.rutgers.knighttransporter.feature_stuff.CITY
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
        building.getStringProperty(BUILDING_NAME).let { buildingName ->
            place_sheet_building_name.text = buildingName
            place_sheet_building_image.contentDescription = "Photo of $buildingName"
        }

        place_sheet_building_address.text = "${building.getStringProperty(BUILDING_ADDRESS)}, ${building.getStringProperty(CITY)}"

        val buildingNumber = building.getNumberProperty(BUILDING_NUMBER)

        val progressDrawable = CircularProgressDrawable(requireContext()).apply {
            setStyle(CircularProgressDrawable.LARGE)
            start()
        }

        Glide.with(this)
            .load("https://storage.googleapis.com/rutgers-campus-map-building-images-prod/$buildingNumber/00.jpg")
            .centerCrop()
            .placeholder(progressDrawable)
            .into(place_sheet_building_image)

    }
}
