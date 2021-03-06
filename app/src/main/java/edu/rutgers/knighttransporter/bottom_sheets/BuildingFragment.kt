package edu.rutgers.knighttransporter.bottom_sheets

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewModelScope
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.mapbox.geojson.Feature
import edu.rutgers.knighttransporter.MapViewModel
import edu.rutgers.knighttransporter.R
import edu.rutgers.knighttransporter.createRutgersMarkwon
import edu.rutgers.knighttransporter.feature_stuff.*
import kotlinx.android.synthetic.main.fragment_place_sheet_building.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.min

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
        val markwon = createRutgersMarkwon(requireContext())

        building.getStringProperty(BUILDING_NAME).let { buildingName ->
            place_sheet_building_name.text = buildingName
            place_sheet_building_image.contentDescription = "Photo of $buildingName"
        }

        val buildingNumber = building.getNumberProperty(BUILDING_NUMBER).toInt()

        place_sheet_building_number.text = "Building number: $buildingNumber"

        place_sheet_building_address.text = "${building.getStringProperty(BUILDING_ADDRESS)}\n" +
                "${building.getStringProperty(CITY)}, ${building.getStringProperty(STATE)}"

        val progressDrawable = CircularProgressDrawable(requireContext()).apply {
            setStyle(CircularProgressDrawable.LARGE)
            start()
        }

        val fetchSize = DisplayMetrics().run {
            requireActivity().windowManager.defaultDisplay.getMetrics(this)
            min(widthPixels, heightPixels)
        }

        Glide.with(this)
            .load("https://storage.googleapis.com/rutgers-campus-map-building-images-prod/$buildingNumber/00.jpg")
            .override(fetchSize)
            .centerCrop()
            .placeholder(progressDrawable)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    // TODO: Surely there's a cleaner way to check this
                    if (e?.message?.contains("FileNotFound") == true) {
                        // e.g. building 3519
                        place_sheet_building_image_error.text = getString(R.string.no_photo_found)
                    } else {
                        place_sheet_building_image_error.text =
                            getString(R.string.error_loading_photo)
                    }
                    place_sheet_building_image.visibility = View.INVISIBLE
                    place_sheet_building_image_error.visibility = View.VISIBLE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    if (resource != null) {
                        place_sheet_building_image.setOnClickListener {
                            AlertDialog.Builder(requireContext())
                                .setView(R.layout.dialog_photo)
                                .setNeutralButton(R.string.close) { _, _ -> }
                                .show().run {
                                    findViewById<ImageView>(R.id.expanded_image_view)?.setImageDrawable(
                                        resource
                                    )
                                }
                        }
                    }
                    return false
                }
            })
            .into(place_sheet_building_image)

        // TODO: If these fail due to lack of an Internet connection,
        //  we should automatically retry them when possible.

        mapViewModel.viewModelScope.launch(CoroutineExceptionHandler { _, _ ->
            Toast.makeText(
                requireContext(),
                "Error fetching description, website, and alerts for this building",
                Toast.LENGTH_SHORT
            ).show()
        }) {
            val autoLinkMarkwon = createRutgersMarkwon(requireContext(), true)
            val arcGISDetails = withContext(Dispatchers.Default) {
                mapViewModel.getBuildingArcGISDetails(buildingNumber)
            }

            if (arcGISDetails?.alertLinks != null) {
                place_sheet_building_alert.run {
                    autoLinkMarkwon.setMarkdown(this, "**Alert:** ${arcGISDetails.alertLinks}")
                    visibility = View.VISIBLE
                }
            }
            if (arcGISDetails?.description != null) {
                place_sheet_building_description.run {
                    autoLinkMarkwon.setMarkdown(this, arcGISDetails.description)
                    visibility = View.VISIBLE
                }
            }
            if (arcGISDetails?.website?.isNotBlank() == true) { // there's >600 null, >100 blank
                place_sheet_building_website.run {
                    markwon.setMarkdown(this, "**Website:** <${arcGISDetails.website}>")
                    visibility = View.VISIBLE
                }
            }
        }

        mapViewModel.viewModelScope.launch(CoroutineExceptionHandler { _, _ ->
            Toast.makeText(
                requireContext(),
                "Error fetching department list and ZIP code for this building",
                Toast.LENGTH_SHORT
            ).show()
        }) {
            val cloudStorageDetails = withContext(Dispatchers.Default) {
                mapViewModel.getBuildingCloudStorageDetails(buildingNumber)
            }

            if (!cloudStorageDetails.departments.isNullOrEmpty()) {
                val departmentsList =
                    cloudStorageDetails.departments.joinToString(prefix = "- ", separator = "\n- ")
                place_sheet_building_departments.run {
                    // TODO: Occasional crash - fragment not attached to a context, and then the
                    //  call to setMarkdown makes it crash
                    markwon.setMarkdown(this, "### Departments\n$departmentsList")
                    visibility = View.VISIBLE
                }
            }
            place_sheet_building_address.append(" ${cloudStorageDetails.zip}")
        }
    }
}
