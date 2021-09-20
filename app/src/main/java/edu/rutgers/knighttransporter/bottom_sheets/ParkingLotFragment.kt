package edu.rutgers.knighttransporter.bottom_sheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mapbox.geojson.Feature
import edu.rutgers.knighttransporter.createRutgersMarkwon
import edu.rutgers.knighttransporter.databinding.FragmentPlaceSheetParkingLotBinding
import edu.rutgers.knighttransporter.feature_stuff.CONTACT
import edu.rutgers.knighttransporter.feature_stuff.LOT_NAME
import edu.rutgers.knighttransporter.feature_stuff.WEBSITE

class ParkingLotFragment : Fragment() {

    private lateinit var feature: Feature

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            feature = Feature.fromJson(it.getString(ARG_FEATURE)!!)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPlaceSheetParkingLotBinding.inflate(inflater, container, false)

        val markwon = createRutgersMarkwon(requireContext())

        binding.placeSheetParkingLotName.text = feature.getStringProperty(LOT_NAME)

        val websiteText = "Website: <${feature.getStringProperty(WEBSITE)}>"
        markwon.setMarkdown(binding.placeSheetParkingLotWebsite, websiteText)

        val contactNumber = feature.getStringProperty(CONTACT)
        val contactText = "Contact number: [$contactNumber](tel:$contactNumber)"
        markwon.setMarkdown(binding.placeSheetParkingLotContact, contactText)

        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(feature: String) =
            ParkingLotFragment()
                .apply {
                    arguments = Bundle().apply {
                        putString(ARG_FEATURE, feature)
                    }
                }
    }
}
