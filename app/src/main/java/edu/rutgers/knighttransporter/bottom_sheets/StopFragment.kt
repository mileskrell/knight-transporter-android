package edu.rutgers.knighttransporter.bottom_sheets

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.mapbox.geojson.Feature
import edu.rutgers.knighttransporter.R
import edu.rutgers.knighttransporter.feature_stuff.PlaceType
import edu.rutgers.knighttransporter.feature_stuff.getNameForPlaceType
import kotlinx.android.synthetic.main.fragment_place_sheet_stop.*

class StopFragment : Fragment(R.layout.fragment_place_sheet_stop) {
    companion object {
        @JvmStatic
        fun newInstance(feature: String) =
            StopFragment()
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
        place_sheet_stop_name.text = feature.getNameForPlaceType(PlaceType.STOP)
    }
}
