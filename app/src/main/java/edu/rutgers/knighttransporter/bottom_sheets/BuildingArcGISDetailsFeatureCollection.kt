package edu.rutgers.knighttransporter.bottom_sheets

import com.google.gson.annotations.SerializedName

data class BuildingArcGISDetailsFeatureCollection(
    val features: List<Feature>,
    val type: String
) {
    data class Feature(
        // This should crash if the returned geometry is ever not null, which will immediately tell
        // us that we can start using the Completed_Rutgers_Building_List endpoint as a full
        // replacement for Rutgers_University_Buildings! (Sure I could check this more nicely, but
        // this isn't a production app yet)
        val geometry: Nothing?,
        val properties: BuildingArcGISDetails,
        val type: String
    ) {
        data class BuildingArcGISDetails(
            @SerializedName("AlertLinks")
            val alertLinks: String?,
            @SerializedName("BldgNum")
            val bldgNum: Int,
            @SerializedName("Description")
            val description: String?,
            @SerializedName("Website")
            val website: String?
        )
    }
}
