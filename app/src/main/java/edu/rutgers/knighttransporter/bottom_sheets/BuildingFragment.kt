package edu.rutgers.knighttransporter.bottom_sheets

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewModelScope
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.mapbox.geojson.Feature
import edu.rutgers.knighttransporter.MapViewModel
import edu.rutgers.knighttransporter.R
import edu.rutgers.knighttransporter.createRutgersMarkwon
import edu.rutgers.knighttransporter.feature_stuff.BUILDING_ADDRESS
import edu.rutgers.knighttransporter.feature_stuff.BUILDING_NAME
import edu.rutgers.knighttransporter.feature_stuff.BUILDING_NUMBER
import edu.rutgers.knighttransporter.feature_stuff.CITY
import edu.rutgers.knighttransporter.feature_stuff.STATE
import kotlinx.android.synthetic.main.fragment_place_sheet_building.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    val mapViewModel: MapViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            building = Feature.fromJson(it.getString(ARG_FEATURE)!!)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val markwon = createRutgersMarkwon(requireContext())

        building.getStringProperty(BUILDING_NAME).let { buildingName ->
            place_sheet_building_name.text = buildingName
            place_sheet_building_image.contentDescription = "Photo of $buildingName"
        }

        val buildingNumber = building.getNumberProperty(BUILDING_NUMBER)

        place_sheet_building_number.text = "Building number: $buildingNumber"

        place_sheet_building_address.text = "${building.getStringProperty(BUILDING_ADDRESS)}\n" +
                "${building.getStringProperty(CITY)}, ${building.getStringProperty(STATE)}"

        val progressDrawable = CircularProgressDrawable(requireContext()).apply {
            setStyle(CircularProgressDrawable.LARGE)
            start()
        }

        Glide.with(this)
            .load("https://storage.googleapis.com/rutgers-campus-map-building-images-prod/$buildingNumber/00.jpg")
            .centerCrop()
            // TODO: Display something for both "image not found" (e.g. building 3519) and for
            //  "some other error fetching image"
            .placeholder(progressDrawable)
            .into(place_sheet_building_image)

        mapViewModel.viewModelScope.launch(context = Dispatchers.Main) {
            val details = withContext(Dispatchers.Default) {
                mapViewModel.getBuildingDetails(building.getNumberProperty(BUILDING_NUMBER).toInt())
            }

            if (!details.departments.isNullOrEmpty()) {
                val departmentsList =
                    details.departments.joinToString(prefix = "- ", separator = "\n- ")
                place_sheet_building_departments.run {
                    markwon.setMarkdown(this, "### Departments\n$departmentsList")
                    visibility = View.VISIBLE
                }
            }
            place_sheet_building_address.append(" ${details.zip}")
        }
    }
}
