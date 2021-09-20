package edu.rutgers.knighttransporter.bottom_sheets

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.rutgers.knighttransporter.databinding.RouteItemBinding
import edu.rutgers.knighttransporter.for_transloc.StopMarkerData
import java.text.SimpleDateFormat
import java.util.*

class RoutesAdapter(private var stopMarkerData: StopMarkerData) :
    RecyclerView.Adapter<RoutesAdapter.RouteViewHolder>() {

    fun updateStopMarkerData(newStopMarkerData: StopMarkerData) {
        stopMarkerData = newStopMarkerData
        notifyDataSetChanged()
        Log.d(TAG, "Updated data shown in bottom sheet for stop ${stopMarkerData.stop.name}")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = RouteViewHolder(
        RouteItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun getItemCount() = stopMarkerData.associatedRoutes.size

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        holder.binding.routeItemRouteName.run {
            text = stopMarkerData.associatedRoutes[position].longName
            setTextColor(Color.parseColor("#${stopMarkerData.associatedRoutes[position].color}"))
        }

        holder.binding.routeItemPredictions.text = stopMarkerData.arrivalEstimates.filter {
            it.routeId == stopMarkerData.associatedRoutes[position].routeId
        }.map {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).parse(it.arrivalAt)!!
        }.sorted().take(3).map {
            (it.time - Date().time) / 1000
        }.run {
            if (isEmpty()) {
                "No arrivals"
            } else joinToString("\n") { seconds ->
                when {
                    seconds <= 0 -> "Now" // e.g. it said "5 seconds" in the data, but it's negative by the time we open it
                    seconds < 60 -> "<1 min"
                    else -> "${seconds / 60} min"
                    // TODO: Does TransLoc ever give us arrival times that are already in the past
                    //  at the time of sending? If so, we'd want to not show those at all.
                }
            }
        }
    }

    class RouteViewHolder(val binding: RouteItemBinding) : RecyclerView.ViewHolder(binding.root)

    companion object {
        const val TAG = "RoutesAdapter"
    }
}
